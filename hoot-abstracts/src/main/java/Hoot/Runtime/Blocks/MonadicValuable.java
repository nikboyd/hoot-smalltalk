package Hoot.Runtime.Blocks;

/**
 * A monadic block closure protocol.
 * Defines the type signature for ANSI Smalltalk MonadicValuable (section 5.4.4).
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2025 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public interface MonadicValuable extends Arguable {

    /**
     * @return the result of this block when evaluated
     * @param <R> a result type
     * @param <V> a value type
     * @param value a value
     */
    public <V,R> R value(V value);

} // MonadicValuable
