package Hoot.Runtime.Faces;

import static Hoot.Runtime.Names.Operator.Empty;

/**
 * An integer value holder.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2019 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public interface IntegerValue extends Valued {

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

        public static final int Size = 4096;

        /**
         * @return the size of this cache
         */
        public static int size() { return Size; }

        private final IntegerValue[] positiveIntegers = new IntegerValue[Size];
        private final IntegerValue[] negativeIntegers = new IntegerValue[Size];

        /**
         * @return whether this cache covers a value
         * @param value a value
         */
        public boolean covers(int value) { return (value < 0 ? 0 - value < Size : value < Size); }

        /**
         * @return whether this cache has a value
         * @param value a value
         */
        public boolean hasCached(int value) { return covers(value) && (getCached(value) != null); }

        /**
         * @return a cached value holder (flyweight)
         * @param <R> an IntegerValue type
         * @param value a cached value
         */
        @SuppressWarnings("unchecked")
        public <R extends IntegerValue> R getCached(int value) { if (!covers(value)) return null;
            return (R) (value < 0 ? this.negativeIntegers[0 - value] : this.positiveIntegers[value]); }

        /**
         * @return a flyweight value holder
         * @param <R> an IntegerValue type
         * @param valueHolder a value holder
         */
        public <R extends IntegerValue> R cache(R valueHolder) {
            if (valueHolder == null) return null;
            int index = valueHolder.intValue();
            if (!covers(index)) return null;

            if (index < 0) {
                this.negativeIntegers[0 - index] = valueHolder;
            }
            else {
                this.positiveIntegers[index] = valueHolder;
            }

            return valueHolder;
        }

    } // Cache

    public static class Source extends Number implements IntegerValue {

        static final Cache CachedValues = new Cache();
        public static Source with(int value) {
            return CachedValues.hasCached(value) ?
                CachedValues.getCached(value) : CachedValues.cache(new Source(value)); }

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
