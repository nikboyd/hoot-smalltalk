package Hoot.Compiler.Constants;

import static Hoot.Runtime.Names.Keyword.*;
import static Hoot.Runtime.Names.Primitive.*;
import static Hoot.Runtime.Behaviors.HootRegistry.*;

/**
 * A literal Character.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 1999,2025 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class LiteralCharacter extends LiteralNumber {

    public LiteralCharacter() { super(); }
    public LiteralCharacter(String value) { this(); this.value = value; }
    public static LiteralCharacter with(String text, int line) { return new LiteralCharacter(text).withLine(line); }

    @Override public String encodedValue() { return quoteLiterally(value.substring(1, 2)); }
    @Override public String primitiveFactoryName() { return Character; }
    @Override public Class primitiveType() { return char.class; }
    @Override public String resolvedTypeName() {
        return (file().needsMagnitudes() ? RootType().fullName() : CharacterType().fullName()); }

} // LiteralCharacter
