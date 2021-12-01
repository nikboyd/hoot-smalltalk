package Hoot.Runtime.Emissions;

import org.stringtemplate.v4.ST;

/**
 * Emits code using StringTemplate.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2019 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 * @see <a href="https://gitlab.com/hoot-smalltalk/hoot-smalltalk/tree/master/LICENSE.txt">LICENSE for more details</a>
 */
public interface Emitter {

    public ST emitCode();

} // Emitter
