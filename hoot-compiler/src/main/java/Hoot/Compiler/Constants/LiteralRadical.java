package Hoot.Compiler.Constants;

/**
 * A radical number.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 * @see <a href="https://gitlab.com/hoot-smalltalk/hoot-smalltalk/tree/master/LICENSE.txt">LICENSE for more details</a>
 */
public class LiteralRadical extends Scalar {

    public LiteralRadical() { super(); }
    public LiteralRadical(String value) { super(value); }
    public static LiteralRadical with(String value, int line) { return new LiteralRadical(value).withLine(line); }

    @Override public Class primitiveType() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override public String primitiveFactoryName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

} // LiteralRadical
