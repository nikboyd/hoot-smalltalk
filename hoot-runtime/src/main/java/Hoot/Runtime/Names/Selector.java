package Hoot.Runtime.Names;

import java.util.*;

import Hoot.Runtime.Maps.Library;
import Hoot.Runtime.Emissions.NamedItem;
import Hoot.Runtime.Emissions.Emission;
import Hoot.Runtime.Emissions.EmissionSource;
import static Hoot.Runtime.Names.Keyword.*;
import static Hoot.Runtime.Emissions.Emission.*;
import static Hoot.Runtime.Faces.Logging.*;
import static Hoot.Runtime.Functions.Utils.*;

/**
 * A method name (Smalltalk selector).
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 1999,2025 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class Selector extends NamedItem implements Hoot.Runtime.Faces.Selector, EmissionSource {

    public Selector() { super(null); contents = new StringBuffer(); }
    public static Selector named(String name) { return from(name); }

    /**
     * @return a new selector created from the supplied name
     * @param name a selector name.
     */
    public static Selector from(String name) {
        Selector selector = new Selector();
        selector.append(name);
        return selector;
    }

    /**
     * @return the number of operands in a message involving the selector.
     * @param selector a selector
     */
    public static int operandCountFrom(String selector) {
        if (Name.BinaryOperators.containsKey(selector)) {
            return 2;
        }
        return countMatches(selector, Colon) + 1;
    }

    StringBuffer contents;
    public String contents() { return contents.toString(); }
    public String methodName() { return Name.from(contents()); }
    public String asPrimitiveOperator() { return (String) PrimitiveOperators.get(contents()); }
    public void append(String aString) { contents.append(aString); }

    public boolean isAssign() { return contents().equals(Assign); }
    public boolean isKeyword() { return contents().endsWith(Colon); }
    public boolean isBasicNew() { return contents().startsWith("basicNew"); }
    public boolean isNew() { return contents().startsWith("new"); }

    @Override public String toString() { return contents(); }
    @Override public String name() { return methodName(); }
    @Override public boolean isEmpty() { return contents.length() == 0; }
    @Override public boolean isSelfish() {
        return contents().startsWith(Super) || contents().startsWith(Self); }

    public boolean isOperator() { return Name.BinaryOperators.containsKey(contents()); }
    @Override public boolean isPrimitive() { return PrimitiveOperators.containsKey(contents()); }
    @Override public boolean isOptimized() { return ObjectSelectors.contains(contents()); }

    public boolean isReturn() { return contents().equals(Exit); }
    public int operandCount() { return operandCountFrom(contents()); }

    public Emission emitQuotedMethodName() { return emitQuoted(methodName()); }

    @Override public Class<?> toClass() {
        return Name.isQualified(fullName()) ?
            Hoot.Runtime.Faces.Selector.super.toClass() :
            nullOr(f -> f.primitiveClass(), Library.findFace(fullName())); }

    @Override public int hashCode() { return contents().hashCode(); }
    @Override public boolean equals(Object selector) {
        return hasAny(selector) &&
            Hoot.Runtime.Faces.Selector.class.isAssignableFrom(selector.getClass()) &&
            falseOr(s -> contents().equals(s.toString()), (Hoot.Runtime.Faces.Selector)selector); }


    public static final Selector empty = Selector.from(Empty);
    public static final String Assign = ":=";
    public static final Selector forAssignment = Selector.from(Assign);
    public static final String Exit = "^";
    public static final Selector forExit = Selector.from(Exit);

    public static final char COLON = ':';
    static final String Colon = ":";

    static final char UNDER = '_';
    static final String Under = "_";

    static final char DOLLAR = '$';
    static final String Dollar = "$";

    static final Properties PrimitiveOperators = new Properties();
    static final List<String> ObjectSelectors = emptyList(String.class);

    /**
     * Initializes (reservedWords), (binaryOperators), (controlClasses).
     */
    static {
        PrimitiveOperators.put("&", " & ");
        PrimitiveOperators.put("|", " | ");
        PrimitiveOperators.put("+", " + ");
        PrimitiveOperators.put("+=", " += ");
        PrimitiveOperators.put("-", " - ");
        PrimitiveOperators.put("-=", " -= ");
        PrimitiveOperators.put("*", " * ");
        PrimitiveOperators.put("*=", " *= ");
        PrimitiveOperators.put("/", " / ");
        PrimitiveOperators.put("%", " % ");
        PrimitiveOperators.put("/=", " /= ");
        PrimitiveOperators.put("//", " / ");
        PrimitiveOperators.put("\\\\", " % ");
        PrimitiveOperators.put("==", " == ");
        PrimitiveOperators.put("<", " < ");
        PrimitiveOperators.put("<=", " <= ");
        PrimitiveOperators.put(">", " > ");
        PrimitiveOperators.put(">=", " >= ");
        PrimitiveOperators.put("~~", " != ");
        PrimitiveOperators.put(Assign, " = "); // ??

        ObjectSelectors.add("class");
        ObjectSelectors.add("new");
        ObjectSelectors.add("new:");
        ObjectSelectors.add("==");
        ObjectSelectors.add("~~");
        ObjectSelectors.add("=");
        ObjectSelectors.add("~=");
        ObjectSelectors.add("do:");
        ObjectSelectors.add("hash");
        ObjectSelectors.add("hashCode");
        ObjectSelectors.add("value");
        ObjectSelectors.add("yourself");
        ObjectSelectors.add("species");
        ObjectSelectors.add("isNil");
        ObjectSelectors.add("notNil");
        ObjectSelectors.add(Assign);
    }

} // Selector
