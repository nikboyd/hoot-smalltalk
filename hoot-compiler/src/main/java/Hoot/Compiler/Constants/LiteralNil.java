package Hoot.Compiler.Constants;

import Hoot.Runtime.Emissions.*;
import static Hoot.Runtime.Behaviors.HootRegistry.*;

/**
 * A literal nil (UndefinedObject).
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 1999,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 * @see <a href="https://gitlab.com/hoot-smalltalk/hoot-smalltalk/tree/master/LICENSE.txt">LICENSE for more details</a>
 */
public class LiteralNil extends Scalar {

    public LiteralNil() { super(); }
    public LiteralNil(String value) { super(value); }
    public LiteralNil(NamedItem container) { super(container, "null"); }
    public static LiteralNil with(String value, int line) { return new LiteralNil("null").withLine(line); }

    @Override public Class primitiveType() { return void.class; }
    @Override public java.lang.String primitiveFactoryName() { return "Void"; }
    @Override public java.lang.String declaredType() { return "UndefinedObject"; }
    @Override public java.lang.String encodedValue() { return "Hoot.Nil()"; }
    @Override public String resolvedTypeName() { return NilType().fullName(); }

    @Override public Emission emitPrimitive() { return emitItem(rawValue()); }
    @Override public Emission emitOperand() { return emitNil(); }

} // LiteralNil
