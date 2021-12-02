package Hoot.Runtime.Functions;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;
import static java.util.Arrays.asList;
import static java.util.Collections.reverse;

import static Hoot.Runtime.Faces.Logging.Empty;
import static Hoot.Runtime.Functions.Exceptional.*;
import static java.util.Collections.sort;

/**
 * Convenience methods for streams and such.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2019 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public interface Utils {

    // NOTE: in the following T = ItemType and R = ResultType

    public static void doIf(boolean p, Runnable r) { if (p) runSafely(r); }

    // set a value if needed and return the currently set value
    public static <R> R getSet(Supplier<R> f, Runnable c) { if (hasOne(f.get())) c.run(); return f.get(); }

    // set a value safely: when a replacement value exists
    public static <R> R setSafely(Supplier<R> f, Consumer<R> c) {
        R value = f.get(); if (hasOne(value)) c.accept(value); return value; }

    public static <T> T itemOr(T defaultValue, T item) { return hasNone(item) ? defaultValue : item; }
    public static <T> T itemOr(Supplier<T> f, T item) { return hasNone(item) ? f.get() : item; }
    public static <T, R> R nullOr(Function<? super T, ? extends R> f, T item) { return hasNone(item) ? null : f.apply(item); }
    public static <T> String emptyOr(Function<? super T, String> f, T item) { return hasNone(item) ? Empty : f.apply(item); }
    public static <T> boolean falseOr(Predicate<? super T> p, T item) { return hasNone(item) ? false : p.test(item); }

    @SafeVarargs public static <T> boolean hasSome(T... items) {
        return (items != null && items.length > 0 && items[0] != null); }

    @SafeVarargs public static <T> boolean hasNo(T... items) {
        return (items == null || items.length == 0 || items[0] == null); }

    static Predicate<Object> HasItem = (item) -> (item != null);
    public static <T> boolean hasOne(T item) { return HasItem.test(item); }
    public static <T> boolean hasNone(T item) { return (item == null); }

    public static <T> boolean hasSome(Collection<T> items) {
        return items != null && !items.isEmpty() && hasOne(items.iterator().next()); }

    public static <T> boolean hasNo(Collection<T> items) {
        return items == null || items.isEmpty() || hasNone(items.iterator().next()); }

    @SafeVarargs public static <T> boolean hasAny(T... items) { return hasSome(items); }
    public static <T> boolean hasAny(Collection<T> items) { return !hasNo(items); }
    public static <K,V> boolean hasKeys(Map<K,V> map) { return hasOne(map) && !map.isEmpty(); }

    @SafeVarargs public static <R> List<R> wrapLast(int count, R... items) {
        if (count < 1) return new ArrayList<>();
        int size = items.length; int index = size - Math.min(size, count);
        return wrap(Arrays.copyOfRange(items, index, size)); }

    public static <R> R[] unwrap(List<R> items, R[] sample) { return hasNo(items) ? sample : items.toArray(sample); }
    @SafeVarargs public static <R> List<R> wrap(R... items) {
        return hasNo(items) ? new ArrayList<>() : copyList(asList(items)); }

    public static <R> R findFirst(Collection<R> items, Predicate<? super R> p) {
        return hasNo(items) ? null : items.stream().filter(p).findFirst().orElse(null); }

    public static <R> List<R> select(Collection<R> items, Predicate<? super R> p) { return selectList(items, p); }
    public static <R> List<R> selectList(Collection<R> items, Predicate<? super R> p) {
        return hasNo(items) ? new ArrayList<>() : items.stream().filter(p).collect(Collectors.toList()); }

    public static <R> Set<R> selectSet(Collection<R> items, Predicate<? super R> p) {
        return hasNo(items) ? new HashSet<>() : items.stream().filter(p).collect(Collectors.toSet()); }

    public static <T, R> List<R> map(Collection<T> items, Function<? super T, ? extends R> m) { return mapList(items, m); }
    public static <T, R> List<R> mapList(Collection<T> items, Function<? super T, ? extends R> m) {
        return mapList(items, HasItem, m); }

    public static <T, R> List<R> mapList(Collection<T> items, Predicate<? super T> p, Function<? super T, ? extends R> m) {
        return hasNo(items) ? new ArrayList<>() : items.stream().filter(p).map(m).collect(Collectors.toList()); }

    public static <T, R> Set<R> mapSet(Collection<T> items, Function<? super T, ? extends R> m) {
        return hasNo(items) ? new HashSet<>() : mapSet(items, HasItem, m); }

    public static <T, R> Set<R> mapSet(Collection<T> items, Predicate<? super T> p, Function<? super T, ? extends R> m) {
        return hasNo(items) ? new HashSet<>() : items.stream().filter(p).map(m).collect(Collectors.toSet()); }

    public static <T> int countAny(Collection<T> items, Predicate<? super T> p) {
        return (int)items.stream().filter(p).count(); }

    public static <T> boolean matchAny(Collection<T> items, Predicate<? super T> p) {
        return hasSome(items) && items.stream().anyMatch(p); }

    public static <T> boolean matchAll(Collection<T> items, Predicate<? super T> p) {
        return hasSome(items) && items.stream().allMatch(p); }

    public static String joinWith(String joint, List<String> names) {
        return hasNo(names) ? Empty : names.stream().collect(Collectors.joining(joint)); }

    public static <R> R reduce(Collection<R> items, BinaryOperator<R> op, R identity) {
        return items.stream().reduce(identity, op); }

    public static <T> ArrayList<T> copyList(Collection<T> list) { return new ArrayList<>(list); }
    public static <T> ArrayList<T> emptyList(Class<T> itemType) { return new ArrayList<>(); }
    public static <T> ArrayList<T> emptyList() { return new ArrayList<>(); }

    public static <T> HashSet<T> copySet(Collection<T> set) { return new HashSet<>(set); }
    public static <T> HashSet<T> emptySet(Class<T> itemType) { return new HashSet<>(); }
    public static <T> HashSet<T> emptySet() { return new HashSet<>(); }

    public static HashMap<String,String> emptyWordMap() { return emptyMap(String.class, String.class); }
    public static <T> HashMap<String,T> emptyMap(Class<T> itemType) { return emptyMap(String.class, itemType); }
    public static <K,V> HashMap<K,V> emptyMap(Class<K> keyType, Class<V> itemType) { return new HashMap<>(); }
    public static <K,V> HashMap<K,V> emptyMap() { return new HashMap<>(); }

    public static <R> Set<R> collectSet(Consumer<Collection<R>> c) { return collectInto(new HashSet<>(), c); }
    public static <R> List<R> collectList(Consumer<Collection<R>> c) { return collectInto(new ArrayList<>(), c); }
    public static <T, R extends Collection<T>>
        R collectInto(final R r, Consumer<Collection<T>> c) { c.accept(r); return r; }

    public static <R, T, CT extends Collection<T>>
        R collectWith(final R r, final CT items, BiConsumer<R, T> b) { items.forEach(it -> b.accept(r, it)); return r; }

    public static <K, V, M extends Map<K, V>> Map<K, V> selectFrom(M m, BiPredicate<K, V> p) {
        HashMap<K, V> results = new HashMap<>(); if (hasNo(m)) return results;
        m.forEach((k,v) -> { if (p.test(k, v)) results.put(k, v); }); return results; }

    public static <R> ArrayList<R> fillList(int count, R item) {
        return collectInto(new ArrayList<>(), (results) -> { int size = count; while (size-- > 0) results.add(item); }); }

    public static <T, R> List<R> listAll(Collection<T> items, Function<? super T, ? extends R> m) {
        return collectList((results) -> results.addAll(mapList(items, m))); }

    public static <T, R> Set<R> selectAll(Collection<T> items, Function<? super T, ? extends R> m) {
        return collectSet((results) -> results.addAll(mapSet(items, m))); }

    public static <T> List<T> reverseList(List<T> list) { reverse(list); return list; }
    public static <T extends Comparable<? super T>> List<T> sortList(List<T> list) { sort(list); return list; }

} // Utils
