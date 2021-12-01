package Hoot.Compiler.Constants;

import static Hoot.Runtime.Names.Keyword.*;
import static Hoot.Runtime.Names.Primitive.*;
import static Hoot.Runtime.Behaviors.HootRegistry.*;

/**
 * A literal Fixed Decimal.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 1999,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 * @see <a href="https://gitlab.com/hoot-smalltalk/hoot-smalltalk/tree/master/LICENSE.txt">LICENSE for more details</a>
 */
public class LiteralDecimal extends LiteralNumber {

    public LiteralDecimal() { super(); }
    public LiteralDecimal(String value) { super(quoteNatively(value)); }
    public static LiteralDecimal with(String value, int line) { return new LiteralDecimal(value).withLine(line); }

    @Override public Class primitiveType() { return java.math.BigDecimal.class; }
    @Override public String primitiveFactoryName() { return Fixed; }
    @Override public String resolvedTypeName() {
        return file().needsMagnitudes() ? RootType().fullName() : FixedType().fullName(); }

} // LiteralDecimal
