package Hoot.Compiler.Constants;

import Hoot.Runtime.Emissions.Emission;
import static Hoot.Runtime.Names.Keyword.*;
import static Hoot.Runtime.Names.Primitive.*;
import static Hoot.Runtime.Emissions.Emission.*;
import static Hoot.Runtime.Behaviors.HootRegistry.*;

/**
 * A literal String.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 1999,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class LiteralString extends LiteralCollection {

    public LiteralString() { super(); }
    public LiteralString(String value) { this(); this.value = value; }
    public static LiteralString with(String value, int line) { return new LiteralString(value).withLine(line); }

    @Override public Class primitiveType() { return java.lang.String.class; }
    @Override public String primitiveFactoryName() { return String; }
    @Override public String encodedValue() { return getLiteral(rawValue()); }
    @Override public Emission emitOperand() { return emit(String).value(encodedValue()); }
    @Override public String resolvedTypeName() {
        return (file().needsCollections() ? RootType().fullName() : StringType().fullName()); }

} // LiteralString
