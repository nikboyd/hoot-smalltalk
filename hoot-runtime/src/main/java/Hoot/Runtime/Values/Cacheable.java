package Hoot.Runtime.Values;

/**
 * A cache-able item.
 * @param <T> an item type
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2025 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public interface Cacheable<T> {
        int stackIndex(); // 0-based stack index
        void stackIndex(int value);

        CachedStack itemStack();
        @SuppressWarnings("unchecked")
        default <R extends Cacheable<T>> R priorItem() {
            return (R)itemStack().priorItem(this); }

        default int prior() { return stackIndex() - 1; }
        default boolean hasPrior() { return 0 < stackIndex(); }
        default boolean isBottom() { return 0 == stackIndex(); }
        default boolean isTop() { return itemStack().hasTop(this); }
        default boolean onStack() { return 0 <= stackIndex() && stackIndex() < itemStack().stackDepth(); }
        default boolean isDefault() { return itemStack().defaultItem() == this; }

} // Cacheable<T>
