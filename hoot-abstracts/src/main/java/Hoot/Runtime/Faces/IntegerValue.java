package Hoot.Runtime.Faces;

import Hoot.Runtime.Blocks.MonadicValuable;
import static Hoot.Runtime.Names.Operator.Empty;

/**
 * An integer value holder.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2025 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public interface IntegerValue extends Valued {

    public static interface Factory { IntegerValue valueFrom(int value); }

    public static IntegerValue with(int value) { return Source.with(value); }

    /**
     * The primitive value cached by this value holder.
     * @return a value
     */
    default int intValue() { return this.hashCode(); }
    default long longValue() { return elemetaryInteger().longValue(); }
    default float floatValue() { return elemetaryInteger().floatValue(); }
    default double doubleValue() { return elemetaryInteger().doubleValue(); }
    default Integer elemetaryInteger() { return intValue(); }

    /**
     * A value cache of limited extent (flyweight values).
     * Cached values are created lazily.
     */
    public static class Cache {

        MonadicValuable valueFactory = new MonadicValuable() {
            @Override public <V,R> R value(V value) { return (R)(new Source((Integer)value)); }
            @Override public IntegerValue argumentCount() { return new Source(1); }
        };
        public Cache(MonadicValuable f) { valueFactory = f; }
        public Cache() {}

        public static final int Size = 4096;

        /**
         * @return the size of this cache
         */
        public static int size() { return Size; }

        private final IntegerValue[] posValues = new IntegerValue[Size];
        private final IntegerValue[] negValues = new IntegerValue[Size];
        private IntegerValue get(int value) { return (value < 0 ? this.negValues[0 - value]: this.posValues[value]); }
        private IntegerValue set(IntegerValue value) {
            int index = value.intValue(); if (index < 0) negValues[0 - index] = value; else posValues[index] = value;
            return value;
        }

        /**
         * @return whether this cache covers a value
         * @param value a value
         */
        public boolean covers(int value) { return (value < 0 ? 0 - value < Size : value < Size); }

        /**
         * @return whether this cache has a value
         * @param value a value
         */
        public boolean hasCached(int value) { return (covers(value) && get(value) != null); }

        /**
         * @return a cached value holder (flyweight)
         * @param <R> an IntegerValue type
         * @param value a cached value
         */
        @SuppressWarnings("unchecked")
        public <R extends IntegerValue> R getCached(int value) {
            return hasCached(value)? (R)get(value): cacheCovered((R)valueFactory.value(value));
        }

        /**
         * @return a flyweight value holder
         * @param <R> an IntegerValue type
         * @param valueHolder a value holder
         */
        @SuppressWarnings("unchecked")
        public <R extends IntegerValue> R cacheCovered(R valueHolder) {
            return (valueHolder != null && covers(valueHolder.intValue()))? (R)set(valueHolder): valueHolder;
        }

    } // Cache

    public static class SourceFactory implements Factory {
        @Override public IntegerValue valueFrom(int value) { return new Source(value); }
    }

    public static class Source extends Number implements IntegerValue {

        static final Cache CachedValues = new Cache();
        public static Source with(int value) { return CachedValues.getCached(value); }

        private Integer value = 0;
        private Source(int value) { this.value = value; }
        @Override public Integer elemetaryInteger() { return this.value; }
        @Override public String toString() { return intValue() + Empty; }
        @Override public int intValue() { return elemetaryInteger(); }
        @Override public long longValue() { return elemetaryInteger().longValue(); }
        @Override public float floatValue() { return elemetaryInteger().floatValue(); }
        @Override public double doubleValue() { return elemetaryInteger().doubleValue(); }

    } // Source

    public static interface Metatype { }

} // IntegerValue
