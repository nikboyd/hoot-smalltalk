package Hoot.Compiler.Constants;

import Hoot.Runtime.Emissions.*;
import Hoot.Runtime.Faces.Selector;
import static Hoot.Runtime.Names.Operator.*;

/**
 * A literal number.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 1999,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 * @see <a href="https://gitlab.com/hoot-smalltalk/hoot-smalltalk/tree/master/LICENSE.txt">LICENSE for more details</a>
 */
public abstract class LiteralNumber extends Scalar {

    public LiteralNumber() { super(); }
    public LiteralNumber(NamedItem container) { super(container); }
    public LiteralNumber(String value) { super(value); }

    public void negate() { value = Minus + value; }
    @Override public boolean optimizes(Selector selector) { return !file().needsMagnitudes(); }
    @Override public Emission emitOperand() {
        return (file().needsMagnitudes()) ? super.emitOperand() : emitCast(declaredType(), super.emitOperand()); }

} // LiteralNumber
