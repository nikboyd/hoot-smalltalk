package Hoot.Runtime.Blocks;

import java.util.*;
import Hoot.Runtime.Faces.IntegerValue;
import static Hoot.Runtime.Functions.Utils.*;

/**
 * A multiple value block closure protocol.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2020 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public interface MultiValuable extends Valuable {

    public static Class<?> type() { return MultiValuable.class; }

    static final ThreadLocal<List<Object>> CachedArgs = new ThreadLocal<>();
    static void clearCache() { CachedArgs.set(new ArrayList<>()); }
    static List<Object> cachedArguments() { return CachedArgs.get(); }
    static void cacheArguments(List<Object> arguments) { clearCache(); cachedArguments().addAll(arguments); }

    default List<Object> arguments() { return cachedArguments(); }
    @Override default IntegerValue argumentCount() { return IntegerValue.with(arguments().size()); }

    default <R> R valueWith(List<Object> arguments) { cacheArguments(arguments); return value(); }
    default <R> R valueWith(Object ... arguments) { return valueWith(wrap(arguments)); }

    default void runWith(List<Object> arguments) { cacheArguments(arguments); value(); }
    default void runWith(Object ... arguments) { runWith(wrap(arguments)); value(); }

} // MultiValuable
