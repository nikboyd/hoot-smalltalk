package Hoot.Runtime.Blocks;

/**
 * A dyadic block closure protocol.
 * Defines the type signature for ANSI Smalltalk DyadicValuable (section 5.4.6).
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2025 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public interface DyadicValuable extends Arguable {

    /**
     * @return the result when evaluated with 2 values
     * @param <R> a result type
     * @param <A> 1st value type
     * @param <B> 2nd value type
     * @param a 1st value
     * @param b 2nd value
     */
    public <A, B, R> R value_value(A a, B b);

} // DyadicValuable
