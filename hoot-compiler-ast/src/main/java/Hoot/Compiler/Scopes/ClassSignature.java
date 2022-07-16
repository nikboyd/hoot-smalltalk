package Hoot.Compiler.Scopes;

import java.util.*;
import Hoot.Runtime.Names.*;
import Hoot.Runtime.Notes.*;
import Hoot.Runtime.Behaviors.*;
import Hoot.Runtime.Emissions.*;
import static Hoot.Runtime.Names.Keyword.*;
import static Hoot.Runtime.Functions.Utils.*;
import static Hoot.Runtime.Emissions.Emission.*;
import static Hoot.Runtime.Names.TypeName.JavaRoot;

/**
 * A class definition signature.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class ClassSignature extends NamedItem implements ScopeSource {

    public ClassSignature() { super(Scope.current()); }
    public static ClassSignature with(Global superClass, Global subtype, String keyword) {
        return with(DetailedType.with(superClass), DetailedType.with(subtype), null, null, keyword, ""); }
    public static ClassSignature with(
            DetailedType superClass, DetailedType subtype, TypeList heritage,
            NoteList notes, String keyword, String comment) {
        ClassSignature result = new ClassSignature();
        result.notes = hasSome(notes)? notes: new NoteList();
        result.types = hasSome(heritage)? heritage: TypeList.withDetails();
        result.superClass = superClass;
        result.subclass = subtype;
        result.keyword = keyword;
        result.comment = comment;
        return result;
    }

    @Override public ClassSignature metaSignature() {
        ClassSignature result = new ClassSignature();
        if (faceScope().isMetaclassBase()) return result;
        result.types = TypeList.withDetails(types().listMetaTypes());
        result.superClass = superMetaType();
        result.subclass = DetailedType.MetaClass;
        result.keyword = keyword;
        return result;
    }

    @Override public int hashCode() { return subType().hashCode() ^ superHash(); }
    @Override public boolean equals(Object sign) {
        return hasAny(sign) && getClass() == sign.getClass() && falseOr(s -> this.equals(s), (ClassSignature) sign); }

    protected boolean equals(ClassSignature s) {
        if (!subType().equals(s.subType())) return false;
        if (!hasSuperclass()) return !s.hasSuperclass();
        return superType().equals(s.superType()); }

    protected String comment;
    @Override public String comment() { return this.comment; }

    protected String keyword;
    public String subtypeKeyword() { return this.keyword; }

    protected TypeList types;
    public TypeList types() { return this.types; }
    public Emission metaFaces() { return (types().isEmpty() ? null : types().emitMetaNames()); }
    public Emission faces() {
        List<Emission> faces = types().detailedTypesCode();
        return (faces.isEmpty() ? null : emitList(faces));
    }

    protected DetailedType subclass;
    public DetailedType subType() { return this.subclass; }
    public String subclassName() { return subType().typeName().name(); }
    public TypeList details() { return subType().details(); }

    @Override public String description() { return name() + " -> " + baseName(); }
    @Override public String name() { return subclassName(); }
    @Override public String shortName() { return name(); }
    @Override public String fullName() { return faceScope().fullName(); }
    @Override public String baseName() { return !hasSuperclass() ? Empty : superType().toTypeName().fullName(); }

    protected DetailedType superClass;
    public DetailedType superType() { return this.superClass; }
    public int superHash() { return hasSuperclass() ? superType().hashCode() : 0; }
    public boolean hasSuperclass() { return hasAny(superType()); }
    @Override public boolean hasNoHeritage() { return !hasSuperclass() && types().isEmpty(); }

    public Emission superClass() { return (hasSuperclass() ? superType().emitItem() : emitObject()); }
    public Emission superMetaClass() {
        if (!hasSuperclass()) return emitObject();
        if (Keyword.Object.equals(subclassName())) return emitItem(MetaclassBase);
        return superType().emitMetaName(false);
    }

    private DetailedType superMetaType() {
        if (!hasSuperclass()) return null;
        return JavaRoot.getName().equals(superClass.simpleName()) ?
                DetailedType.with(Global.with(MetaclassBase)) :
                DetailedType.with(Global.with(superClass.simpleName(), MetaclassType));
    }

    @Override public List<Typified> simpleHeritage() {
        ArrayList<Typified> results = emptyList(Typified.class);
        if (!hasSuperclass()) return results;

        Typified superior = superior();
        if (reportWhetherKnown("simpleHeritage", superior, baseName())) {
            results.add(superior);
            results.addAll(superior.simpleHeritage());
        }

        return results;
    }

    @Override public List<Typified> typeHeritage() {
        Set<Typified> results = emptySet(Typified.class);
        types().list().forEach(type -> {
            String typeName = type.toTypeName().fullName();
            Typified superType = faceNamed(typeName);
            if (reportWhetherKnown("typeHeritage", superType, typeName)) {
                results.add(superType);
                results.addAll(superType.typeHeritage());
            }
        });

        simpleHeritage().forEach(superType -> {
            String typeName = superType.fullName();
            Typified aType = faceNamed(typeName);
            if (reportWhetherKnown("typeHeritage", aType, typeName)) {
                results.addAll(aType.typeHeritage());
            }
        });

        return copyList(results);
    }

    public Face faceScope() { return Face.from(this); }
    public Typified faceNamed(String name) { return faceScope().faceNamed(name); }
    public Typified superior() { return faceScope().superclass(); }

    public List<String> decor() { return notes().classDecor(); }
    public Emission metaDecorators() { return emitItems(emitItem(Static.toLowerCase())); }
    public Emission decorators() { return emitItems(notes().classNotesOnlyDecor()); }

    @Override public List<String> knownTypes() {
        HashSet<String> results = emptySet(String.class);
        results.addAll(types().inferGenerics());
        results.addAll(details().inferGenerics());
        return copyList(results);
    }

    @Override public Emission emitItem() { return hasSuperclass() ? emitClassSignature() : emitNilSubclassSignature(); }
    @Override public Emission emitMetaItem() {
        return emitClassSignature(metaDecorators(), MetaclassType, NoValue, superMetaClass(), metaFaces()); }

    private Emission emitClassSignature() {
        return emitClassSignature(decorators(), subclassName(), details().emitDetails(true), superClass(), faces()); }

    private Emission emitNilSubclassSignature() {
        return emitNilSubclassSignature(decorators(), subclassName(), details().emitItem(), faces()); }

} // ClassSignature
