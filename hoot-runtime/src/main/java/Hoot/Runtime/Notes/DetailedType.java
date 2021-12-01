package Hoot.Runtime.Notes;

import java.util.*;
import java.lang.reflect.Type;

import Hoot.Runtime.Names.Global;
import Hoot.Runtime.Names.TypeName;
import Hoot.Runtime.Behaviors.Scope;
import Hoot.Runtime.Emissions.Emission;
import Hoot.Runtime.Emissions.NamedItem;
import static Hoot.Runtime.Emissions.Emission.*;
import static Hoot.Runtime.Names.Keyword.Behavior;
import static Hoot.Runtime.Names.Keyword.Object;
import static Hoot.Runtime.Names.Name.Metaclass;
import static Hoot.Runtime.Names.Name.Metatype;
import static Hoot.Runtime.Names.Name.removeTail;
import static Hoot.Runtime.Functions.Utils.*;

/**
 * A detailed type.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 * @see <a href="https://gitlab.com/hoot-smalltalk/hoot-smalltalk/tree/master/LICENSE.txt">LICENSE for more details</a>
 */
public class DetailedType extends NamedItem implements NoteSource {

    protected DetailedType() { super(Scope.current()); }
    protected DetailedType(Global g, TypeList list) { this(); this.global = g; this.details = list; }

    @Override public int hashCode() {
        return isEmpty() ? super.hashCode() : typeName().hashCode() ^ details().hashCode(); }

    protected boolean equals(DetailedType d) {
        return (isEmpty() && d.isEmpty()) ||
            typeName().equals(d.typeName()) && details().equals(d.details()); }

    @Override public boolean equals(Object type) {
        return hasAny(type) && getClass() == type.getClass() && falseOr(d -> this.equals(d), (DetailedType) type); }

    public static DetailedType from(Type aType) { return fromType(aType.getTypeName()); }

    static final String[] Angles = { "<", ">", ",", };
    public static DetailedType fromType(String type) {
        boolean arrayed = type.endsWith(Arrayed);
        if (arrayed) type = removeTail(type, Arrayed);

        if (type.endsWith(Angles[1])) {
            String name = type.substring(0, type.indexOf(Angles[0]));
            String tails = type.substring(1 + type.indexOf(Angles[0]), type.length() - 1);
            return fromTypes(name, tails.split(Angles[2])).makeArrayed(arrayed);
        }
        else {
            return named(type).makeArrayed(arrayed);
        }
    }

    public static DetailedType fromTypes(String name, String... details) {
        return with(Global.named(name), TypeList.withDetails(map(wrap(details), d -> DetailedType.fromType(d)))); }

    public static DetailedType from(TypeName type) { return with(type.toGlobal()); }
    public static DetailedType named(String name) { return with(TypeName.fromTail(name).toGlobal()); }
    public static DetailedType with(Global global) { return with(global, TypeList.withDetails()); }
    public static DetailedType with(Global global, DetailedType detail) { return with(global, TypeList.withDetails(detail)); }
    public static DetailedType with(Global global, TypeList details) { return new DetailedType(global, details); }

    protected Global global;
    public Global typeName() { return this.global; }
    public String simpleName() { return typeName().name(); }
    public boolean isGeneric() { return typeName().isGenericType(); }
    @Override public boolean isEmpty() { return false; }

    public TypeName toTypeName() { return typeName().typeResolver(); }
    public String metaName(boolean asType) { return typeName().metaName(asType); }
    public DetailedType metaType(boolean asType) { return named(metaName(asType)); }

    protected TypeList details;
    public TypeList details() { return this.details; }
    public DetailedType basicType() { return extendsBase() ? details().list().get(0) : null; }

    protected boolean extendsBase = false;
    public boolean extendsBase() { return this.extendsBase; }
    public DetailedType makeExtensive() { this.extendsBase = true; return this; }

    public boolean isArrayed() { return typeName().isArrayed(); }
    public DetailedType makeArrayed(boolean value) { typeName().makeArrayed(value); return this; }

    protected boolean argued = false;
    public DetailedType makeArgued(boolean value) { this.argued = value; return this; }

    public Map<String, Emission> extractGenerics(boolean wantsBases) {
        HashMap<String, Emission> results = emptyMap(Emission.class);
        if (this.isGeneric()) results.put(typeName().shortName(), emitCode(wantsBases));
        results.putAll(details().extractGenerics(wantsBases));
        return results; }

    public List<String> inferGenerics() {
        ArrayList<String> results = emptyList(String.class);
        results.addAll(details().inferGenerics());
        results.addAll(typeName().inferGenerics());
        return results; }

    public Emission emitMetaName(boolean asType) {
        Global typeName = typeName();
        return (typeName.isEmpty() ? emitEmpty() : typeName.emitMetaName(asType)); }

    public Emission emitType() { return (typeName().isEmpty() ? emitEmpty() : typeName().emitItem()); }
    public Emission emitNakedType() { return emitItem(toTypeName().convertedTail()); }

    public Emission emitDetails(boolean wantsBases) { return details().emitDetails(wantsBases); }
    private Emission emitDetailedType(boolean wantsBases) { return emitDetailedType(emitType(), emitDetails(wantsBases)); }
    private Emission emitExtendedType(boolean wantsBases) {
        return emitExtendedType(emitType(), wantsBases ? basicType().emitCode(wantsBases) : NoValue); }

    @Override public Emission emitItem() { return emitCode(true); }
    public Emission emitArrayed() { return this.argued ? emitItem(Etc) : emitItem(Arrayed); }

    public Emission emitCode(boolean wantsBases) {
        if (isEmpty()) return null;
        return extendsBase() ?
                emitExtendedType(wantsBases) :
                emitDetailedTypes(wantsBases) ; }

    public Emission emitDetailedTypes(boolean wantsBases) {
        return isArrayed() ?
                emitSequence(emitNakedType(), emitArrayed()) :
                emitDetailedType(wantsBases) ; }


    public static final DetailedType RootType = DetailedType.named(Object);
    public static final DetailedType MetaType = DetailedType.named(Metatype);
    public static final DetailedType MetaClass = DetailedType.named(Metaclass);
    public static final DetailedType BehaviorClass = DetailedType.named(Behavior);

} // DetailedType
