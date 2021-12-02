package Hoot.Runtime.Blocks;

import Hoot.Runtime.Faces.*;

/**
 * All valuable types have arguments.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2019 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public interface Arguable {

    /**
     * A count of the arguments required.
     * @return an argument count
     */
    public IntegerValue argumentCount();

} // Arguable
