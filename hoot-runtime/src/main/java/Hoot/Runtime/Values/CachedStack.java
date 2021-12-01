package Hoot.Runtime.Values;

import java.util.*;
import static Hoot.Runtime.Functions.Utils.*;

/**
 * Caches items on a thread scoped stack.
 * @param <T> item type
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 * @see <a href="https://gitlab.com/hoot-smalltalk/hoot-smalltalk/tree/master/LICENSE.txt">LICENSE for more details</a>
 */
public class CachedStack<T extends Cacheable<T>> {

    private final ThreadLocal<Stack<T>> cache = new ThreadLocal<>();
    private Stack<T> cacheStack() { cache.set(new Stack<>()); return cache.get(); }
    public Stack<T> cachedStack() { return itemOr(() -> cacheStack(), cache.get()); }

    public T pop() { return hasItems() ? cachedStack().pop() : defaultItem(); }
    public T top() { return hasItems() ? cachedStack().peek() : defaultItem(); }
    public T popIfTop(T item) { if (hasTop(item)) cachedStack().pop(); return item; }
    public boolean hasTop(Cacheable item) { return hasOne(item) && hasItems() && top() == item; }

    public T push(T item) {
        if (hasNone(item) || item.isDefault()) return item;
        item.stackIndex(stackDepth()); cachedStack().push(item); return item; }

    public T priorItem(T item) {
        return (item.isBottom() || !item.onStack()) ?
            defaultItem() : cachedStack().get(item.prior()); }

    public boolean hasItems() { return 0 < stackDepth(); }
    public boolean isEmpty() { return stackDepth() < 1; }
    public int stackDepth() { return cachedStack().size(); }

    private T defaultItem = null;
    public T defaultItem() { return this.defaultItem; }
    public CachedStack defaultIfEmpty(T item) { this.defaultItem = item; return this; }

} // CachedStack<T>
