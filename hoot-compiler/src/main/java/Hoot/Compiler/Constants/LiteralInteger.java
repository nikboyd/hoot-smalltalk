package Hoot.Compiler.Constants;

import Hoot.Runtime.Emissions.*;
import static Hoot.Runtime.Names.Keyword.*;
import static Hoot.Runtime.Behaviors.HootRegistry.*;

/**
 * A literal SmallInteger.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 1999,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 * @see <a href="https://gitlab.com/hoot-smalltalk/hoot-smalltalk/tree/master/LICENSE.txt">LICENSE for more details</a>
 */
public class LiteralInteger extends LiteralNumber {

    public LiteralInteger() { super(); }
    public LiteralInteger(String value) { super(value); }
    public static LiteralInteger with(String value, int line) { return new LiteralInteger(value).withLine(line); }

    @Override public Class primitiveType() { return Integer.class; }
    @Override public String primitiveFactoryName() { return Integer; }
    @Override public String declaredType() { return Integer; }
    @Override public Emission emitPrimitive() { return super.emitPrimitive(); }
    @Override public Emission emitOperand() { return emitInteger(encodedValue()); }
    @Override public String resolvedTypeName() {
        return (file().needsMagnitudes() ? RootType().fullName() : IntegerType().fullName()); }

} // LiteralInteger
