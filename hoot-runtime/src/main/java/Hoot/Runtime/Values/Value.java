package Hoot.Runtime.Values;

import Hoot.Runtime.Faces.Named;
import static Hoot.Runtime.Functions.Utils.*;
import static Hoot.Runtime.Faces.Logging.*;
import static Hoot.Runtime.Names.Keyword.Self;
import static Hoot.Runtime.Names.Primitive.*;

/**
 * A named typed value.
 * @param <V> a value type
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 * @see <a href="https://gitlab.com/hoot-smalltalk/hoot-smalltalk/tree/master/LICENSE.txt">LICENSE for more details</a>
 */
public class Value<V> implements Named {

    private Value() { super(); }
    private <T> Value(Class<T> valueType) { this(); this.valueType = valueType; }
    private <T> Value(Class<T> valueType, String valueName) { this(valueType); name(valueName); }
    private Value(String name, V value) { this(value.getClass()); this.value = value; name(name); }

    public static <T> Value<T> self(T value) { return named(Self, value); }
    public static <T> Value<T> with(T value) { return named(Empty, value); }
    public static <T> Value<T> from(int index) { return named("value"+index); }
    public static <T> Value<T> named(String name) { return new Value<>(Object.class, name); }
    public static <T> Value<T> named(String name, Class<T> valueType) { return new Value<>(valueType, name); }
    public static <T> Value<T> named(String name, T value) {
        if (hasNone(value)) return named(name);
        Value<T> v = new Value<>(value.getClass(), name);
        return v.bind(value); }

    private Object value = null;
    private void value(Object value) { checkType(value); this.value = value; this.valueType = value.getClass(); }
    @Override public <R> R value() { Class<R> resultType = resultType(valueType()); return resultType.cast(value); }
    public <T extends V> Value<V> bind(T value) { if (hasOne(value)) value(value); return this; }

    public static <R> R as(Class<R> resultType, Value v) { return hasOne(v) ? resultType.cast(v.value()) : null; }
    public static <V> Value<V> asValue(Class<Value<V>> valueType, Value v) { return valueType.cast(v); }

    private Class<?> valueType = Void.class;
    @Override public Class<?> valueType() { return this.valueType; }
    private boolean voidType() { return Void.class == valueType(); }

    private void checkType(Class<?> aType) { if (hasOne(value) && !aType.isInstance(value)) reportType(value, aType); }
    private void checkType(Object value) {
        if (hasOne(value) && !voidType() && !valueType().isInstance(value)) reportType(value, value.getClass()); }

    static final String TypeWarning = "change? %s => %s '%s'";
    private void reportType(Object value, Class<?> aType) {
        warn(format(TypeWarning, toString(), aType.getSimpleName(), value.toString())); }

    private String valueName = Empty;
    @Override public String name() { return this.valueName; }
    private void inferName() { this.valueName = inferName(valueType); }
    private Value orInferName() { if (isEmpty(name())) inferName(); return this; }
    private Value name(String valueName) { this.valueName = valueName; return this.orInferName(); }
    public Value makeSelfish() { this.valueName = Self; return this; }

    static final String ValueReport = "%s %s: %s";
    @Override public String toString() {
        return format(ValueReport, valueType().getSimpleName(), name(), formatValue(value(), valueType())); }

    static final String Comma = ", ";
    static final String Arrayed = "[]";
    private static <ValueType> String formatValue(ValueType value, Class<?> valueType) {
        if (value == null) return "null";
        if (valueType.getSimpleName().endsWith(Arrayed)) {
            Object[] values = (Object[]) value;
            return joinWith(Comma, map(wrap(values), v -> formatValue(v, v.getClass())));
        }

        if (valueType.getSimpleName().contains(String.class.getSimpleName())) {
            return quoteLiterally(value.toString());
        }

        return value.toString();
    }

    static final String Array = "Array";
    static final String Vowels = "aeiou";
    static final String[] Articles = { "a", "an" };
    static String inferName(Class<?> valueType) {
        String typeName = valueType.getSimpleName();
        String letter = Character.toString(typeName.charAt(0));
        int index = Vowels.contains(letter.toLowerCase()) ? 1 : 0;
        String result = Articles[index] + typeName;
        if (typeName.endsWith(Arrayed)) result = result.replace(Arrayed, Array);
        return result; }

} // Value<ValueType>
