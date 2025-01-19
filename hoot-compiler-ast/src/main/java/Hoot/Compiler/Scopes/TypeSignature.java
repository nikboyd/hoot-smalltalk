package Hoot.Compiler.Scopes;

import java.util.*;
import Hoot.Runtime.Notes.*;
import Hoot.Runtime.Behaviors.*;
import Hoot.Runtime.Emissions.*;
import static Hoot.Runtime.Functions.Utils.*;
import static Hoot.Runtime.Emissions.Emission.*;
import static Hoot.Runtime.Names.Name.Metatype;

/**
 * A type definition signature.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2025 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class TypeSignature extends TypeHeritage implements ScopeSource {

    public TypeSignature() { super(); }
    @Override public boolean isInterface() { return true; }

    public static TypeSignature with(
            TypeList heritage, DetailedType subtype,
            NoteList notes, String keyword, String comment) {
        TypeSignature result = new TypeSignature();
        result.notes = notes;
        result.heritage = heritage;
        result.subtype = subtype;
        result.keyword = keyword;
        result.comment = comment;
        return result;
    }

    @Override public TypeSignature metaSignature() {
        TypeSignature result = new TypeSignature();
        result.heritage = TypeList.withDetails(heritage().listMetaTypes());
        result.subtype = DetailedType.MetaType;
        result.keyword = keyword;
        return result;
    }

    @Override public int hashCode() { return subType().hashCode(); }
    @Override public boolean equals(Object sign) {
        return hasAny(sign) && getClass() == sign.getClass() && falseOr(s -> this.equals(s), (TypeSignature) sign); }

    protected boolean equals(TypeSignature s) {
        if (!subType().equals(s.subType())) return false;
        if (hasNoHeritage()) return s.hasNoHeritage();
        return heritage().equals(s.heritage()); }

    @Override public boolean hasNoHeritage() { return heritage().isEmpty(); }
    @Override public List<Typified> typeHeritage() {
        Set<Typified> results = emptySet(Typified.class);
        heritage().list().forEach(type -> {
            Typified superType = faceNamed(type.simpleName());
            if (hasSome(superType)) {
                results.add(superType);
                results.addAll(superType.typeHeritage());
            }
        });
        return copyList(results);
    }

    public Typified faceNamed(String name) { return faceScope().faceNamed(name); }
    public Face faceScope() { return face(); }

    public List<String> decor() { return notes().typeDecor(); }
    @Override public String description() { return name() + " -> " + baseName(); }
    @Override public String name() { return subtypeName(); }
    @Override public String fullName() { return faceScope().fullName(); }
    @Override public String baseName() { 
        return !hasHeritage() ? Empty : heritage().list().get(0).toTypeName().fullName(); }

    @Override public List<String> knownTypes() {
        HashSet<String> results = emptySet(String.class);
        results.addAll(details().inferGenerics());
        results.addAll(heritage().inferGenerics());
        return copyList(results);
    }

    public Emission decorators() { return emitEmpty(); }
    public Emission emitDetails() { return details().emitItem(); }
    public Emission metaDecorators() { return emitItems(notes().metaNotesOnlyDecor()); }

    Emission emitHeritage() { return hasHeritage() ? heritage().emitItem() : NoValue; }
    @Override public Emission emitItem() {
        return emitTypeSignature(decorators(), subtypeName(), details().emitDetails(true), emitHeritage()); }

    Emission emitMetaHeritage() { return hasHeritage() ? heritage().emitMetaNames() : NoValue; }
    @Override public Emission emitMetaItem() {
        return emitTypeSignature(metaDecorators(), Metatype, details().emitDetails(true), emitMetaHeritage()); }

} // TypeSignature
