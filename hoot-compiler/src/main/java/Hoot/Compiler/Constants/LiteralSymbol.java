package Hoot.Compiler.Constants;

import Hoot.Runtime.Emissions.Emission;
import static Hoot.Runtime.Names.Keyword.*;
import static Hoot.Runtime.Names.Primitive.*;
import static Hoot.Runtime.Emissions.Emission.*;
import static Hoot.Runtime.Behaviors.HootRegistry.*;

/**
 * A literal Symbol.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 1999,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 * @see <a href="https://gitlab.com/hoot-smalltalk/hoot-smalltalk/tree/master/LICENSE.txt">LICENSE for more details</a>
 */
public class LiteralSymbol extends LiteralString {

    public LiteralSymbol() { super(); }
    public LiteralSymbol(String value) { super(symbolize(value)); }
    public static LiteralSymbol with(String value, int line) { return new LiteralSymbol(value).withLine(line); }

    public static String symbolize(String contents) {
        if (contents.startsWith(Pound)) {
            contents = contents.substring(1);
        }

        if (contents.length() > 0 && contents.charAt(0) != SingleQuote.charAt(0)) {
            contents = SingleQuote + contents + SingleQuote;
        }

        return contents;
    }

    @Override public Emission emitOperand() { return emit(Symbol).value(encodedValue()); }
    @Override public Emission emitPrimitive() { return emitOperand(); }

    @Override public Class primitiveType() { return java.lang.String.class; }
    @Override public String primitiveFactoryName() { return Symbol; }
    @Override public String resolvedTypeName() {
        return (file().needsCollections() ? RootType().fullName() : SymbolType().fullName()); }

} // LiteralSymbol
