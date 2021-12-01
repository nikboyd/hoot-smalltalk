package Hoot.Compiler.Constants;

import static Hoot.Runtime.Names.Keyword.*;
import static Hoot.Runtime.Behaviors.HootRegistry.*;

/**
 * A literal Float.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 1999,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 * @see <a href="https://gitlab.com/hoot-smalltalk/hoot-smalltalk/tree/master/LICENSE.txt">LICENSE for more details</a>
 */
public class LiteralFloat extends LiteralNumber {

    public LiteralFloat() { super(); }
    public LiteralFloat(String value) { super(value); }
    public static LiteralFloat with(String value, int line) { return new LiteralFloat(value).withLine(line); }

    @Override public String encodedValue() { return super.encodedValue() + "f"; }
    @Override public Class primitiveType() { return float.class; }
    @Override public String primitiveFactoryName() { return Float; }
    @Override public String resolvedTypeName() {
        return (file().needsMagnitudes() ? RootType().fullName() : FloatType().fullName()); }

} // LiteralFloat
