package Hoot.Runtime.Faces;

/**
 * Indicates whether this exits a method.
 *
 * blocks (and methods) may or may not have results
 * blocks (and methods) may or may not have ^exits
 * some ^exits are also results, as when ^exit is final statement of a method
 * nested ^exits are method results that bail out of a method, and require special frames and handling
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2019 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public interface Resulting {

    default boolean isExit() { return false; }
    default boolean containsExit() { return false; }
    default boolean exitsMethod() { return this.isExit() || this.containsExit(); }
    default boolean isResult() { return false; }

} // Resulting
