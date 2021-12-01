package Hoot.Runtime.Blocks;

import Hoot.Runtime.Faces.Valued;

/**
 * A block closure protocol (alias of Valuable).
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2019 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 * @see <a href="https://gitlab.com/hoot-smalltalk/hoot-smalltalk/tree/master/LICENSE.txt">LICENSE for more details</a>
 */
public interface NiladicValuable extends Valuable {

    public Valued on_do(Valued.Metatype exceptionType, MonadicValuable block);

} // NiladicValuable, this kind has no result
