package Hoot.Runtime.Notes;

import java.util.*;

import Hoot.Runtime.Emissions.Item;
import Hoot.Runtime.Emissions.Emission;
import static Hoot.Runtime.Functions.Utils.*;
import static Hoot.Runtime.Names.Primitive.SerializedTypes;

/**
 * A detailed type list.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2025 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class TypeList extends Item implements NoteSource {

    public TypeList() { super(); }
    protected TypeList(List<DetailedType> types) { this(); this.details.addAll(types); }
    @Override public int hashCode() { return hashList(); }
    @Override public boolean equals(Object types) {
        return hasAny(types) && getClass() == types.getClass() && falseOr(d -> equals(d.list()), (TypeList) types); }

    protected boolean equals(List<DetailedType> details) {
        final int[] index = { 0 };
        if (isEmpty() && details.isEmpty()) return true;
        return list().size() == details.size() &&
            matchAll(list(), d -> d.equals(details.get(index[0]++))); }

    public static TypeList withDetails(DetailedType... types) { return withDetails(wrap(types)); }
    public static TypeList withDetails(List<DetailedType> types) { return new TypeList(types); }

    private boolean exitsBlock = false;
    public boolean exitsBlock() { return this.exitsBlock; }
    public TypeList withExit(DetailedType exitType) {
        if (exitType == null) return this;
        details.add(exitType);
        exitsBlock = true;
        return this; }

    private final List<DetailedType> details = emptyList(DetailedType.class);
    public List<DetailedType> list() { return this.details; }
    @Override public boolean isEmpty() { return list().isEmpty(); }

    public int hashList() { return reduce(map(list(), d -> d.hashCode()), (ha, hb) -> ha ^ hb, 0); }
    public String firstName() { return isEmpty() ? Empty : list().get(0).typeName().fullName(); }
    public Emission firstType() { return isEmpty() ? null : list().get(0).emitItem(); }
    public List<String> metaNames(boolean asType) { return map(listWithoutNatives(), type -> type.metaName(asType)); }
    public List<DetailedType> listMetaTypes() { return map(listWithoutNatives(), type -> type.metaType(true)); }
    public List<DetailedType> listWithoutNatives() {
        return select(list(), it -> !SerializedTypes.contains(it.typeName().name())); }

    public boolean hasType(String typeName) {
        Emission firstType = firstType();
        if (firstType == null) return false;
        return firstType.render().equals(typeName);
    }

    public List<Emission> detailedTypesCode() {
        return (isEmpty()) ? EmptyList : map(list(), type -> type.emitItem()); }

    public List<Emission> derivedTypesCode() {
        return (isEmpty()) ? EmptyList : map(list(), type -> type.emitItem()); }

    public Map<String, Emission> extractGenerics(boolean wantsBases) {
        HashMap<String, Emission> results = emptyMap(Emission.class);
        if (isEmpty()) return results;

        list().forEach(d -> results.putAll(d.extractGenerics(wantsBases)));
        return results;
    }

    public List<String> inferGenerics() {
        ArrayList<String> results = emptyList(String.class);
        if (isEmpty()) return results;

        list().forEach(d -> results.addAll(d.inferGenerics()));
        return results;
    }

    @Override public Emission emitItem() { return emitList(detailedTypesCode()); }
    public Emission emitCode(boolean wantsBases) { return emitList(emitCodeList(wantsBases)); }
    public List<Emission> emitCodeList(boolean wantsBases) { return map(list(), d -> d.emitCode(wantsBases)); }

    public Emission emitDetails(boolean wantsBases) {
        return (list().isEmpty()) ? emitEmpty() : emitDetails(map(list(), d -> d.emitCode(wantsBases))); }

    public Emission emitMetaNames() {
        return (isEmpty()) ? null : emitList(map(metaNames(true), name -> emitItem(name))); }

} // TypeList
