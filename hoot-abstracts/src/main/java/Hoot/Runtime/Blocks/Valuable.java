package Hoot.Runtime.Blocks;

import Hoot.Runtime.Faces.Valued;

/**
 * A basic block closure protocol.
 * Defines the type signature for ANSI Smalltalk Valuable (section 5.4.1).
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2019 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public interface Valuable extends Arguable, Valued {

    /**
     * @return the result of this block when evaluated
     * @param <R> a result type
     */
    @Override public <R> R value();

} // Valuable
