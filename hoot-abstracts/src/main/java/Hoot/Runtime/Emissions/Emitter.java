package Hoot.Runtime.Emissions;

import org.stringtemplate.v4.ST;

/**
 * Emits code using StringTemplate.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2025 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public interface Emitter {

    public ST emitCode();

} // Emitter
