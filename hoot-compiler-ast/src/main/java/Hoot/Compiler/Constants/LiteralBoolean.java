package Hoot.Compiler.Constants;

import Hoot.Runtime.Emissions.*;
import static Hoot.Runtime.Names.Keyword.*;
import static Hoot.Runtime.Behaviors.HootRegistry.*;

/**
 * A literal Boolean.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 1999,2025 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class LiteralBoolean extends Scalar {

    public LiteralBoolean() { super(); }
    public LiteralBoolean(String value) { super(value); }
    public LiteralBoolean(NamedItem container, java.lang.String aValue) { super(container, aValue); }
    public static LiteralBoolean with(String value, int line) { return new LiteralBoolean(value).withLine(line); }

    @Override public Class primitiveType() { return boolean.class; }
    @Override public java.lang.String primitiveFactoryName() { return Boolean; }
    @Override public String resolvedTypeName() { return BooleanType().fullName(); }
    @Override public java.lang.String encodedValue() {
        return "Hoot." + (value.equals("true") ? "True()" : "False()"); }


    @Override public Emission emitPrimitive() { return emitItem(rawValue()); }
    @Override public Emission emitOperand() { return emitBoolean(value); }

} // LiteralBoolean
