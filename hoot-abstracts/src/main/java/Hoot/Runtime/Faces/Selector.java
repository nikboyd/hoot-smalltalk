package Hoot.Runtime.Faces;

import static Hoot.Runtime.Functions.Exceptional.*;

/**
 * A method selector. Defines the type signature for ANSI Smalltalk Selector (section 5.3.7).
 * Implementors of this marker are expected to override value and count.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2019 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public interface Selector extends Named, Valued {

    default boolean isOptimized() { return false; }
    @Override default String name() { return this.toString(); }
    default Class<?> toClass() { return nullOrTryQuietly(n -> Class.forName(n), name()); }

} // Selector
