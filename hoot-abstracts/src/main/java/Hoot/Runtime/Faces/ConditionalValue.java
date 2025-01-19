package Hoot.Runtime.Faces;

/**
 * A boolean value holder.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2025 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public interface ConditionalValue extends Valued {

    default boolean primitiveBoolean() { return false; }
}
