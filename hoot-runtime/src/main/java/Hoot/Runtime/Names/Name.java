package Hoot.Runtime.Names;

import java.util.*;

import static Hoot.Runtime.Names.Keyword.Java;
import static Hoot.Runtime.Faces.Logging.*;
import static Hoot.Runtime.Functions.Utils.*;

/**
 * Maps between Hoot Smalltalk selectors and Java method names.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2025 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class Name {

    static final char DOT = '.';
    static final String Dot = Operator.Dot;
    static final String Escape = "\\";

    static final char COLON = ':';
    static final String Colon = ":";

    static final char UNDER = '_';
    static final String Under = "_";

    static final char DOLLAR = '$';
    static final String Dollar = "$";

    public static final String Public = "public";
    public static final String Protected = "protected";
    public static final String Private = "private";
    public static final String Transient = "transient";
    public static final String Static = "static";
    public static final String Abstract = "abstract";
    public static final String Wrapped = "wrapped";

    /**
     * Internal meta member names.
     */
    public static final String Metatype = "Metatype";
    public static final String Metaclass = "Metaclass";
    public static final String MetaMember = "$class";

    static final List<String> ReservedWords = emptyList(String.class);
    public static final Properties BinaryOperators = new Properties();
    public static final char[] Vowels = { 'a', 'e', 'i', 'o', 'u', 'A', 'E', 'I', 'O', 'U' };

    /**
     * Initializes (reservedWords), (binaryOperators), (controlClasses).
     */
    static {
        ReservedWords.add("package");
        ReservedWords.add("import");
        ReservedWords.add("class");
        ReservedWords.add("interface");
        ReservedWords.add("if");
        ReservedWords.add("else");
        ReservedWords.add("while");
        ReservedWords.add("new");
        ReservedWords.add("return");
        ReservedWords.add("do");
        ReservedWords.add("case");
        ReservedWords.add("continue");
        ReservedWords.add("switch");
        ReservedWords.add(Public);
        ReservedWords.add(Private);
        ReservedWords.add(Protected);
        ReservedWords.add("synchronized");
        ReservedWords.add("native");
        ReservedWords.add("volatile");
        ReservedWords.add(Static);
        ReservedWords.add("null");
        ReservedWords.add("byte");
        ReservedWords.add("char");
        ReservedWords.add("short");
        ReservedWords.add("int");
        ReservedWords.add("long");
        ReservedWords.add("float");
        ReservedWords.add("double");

        BinaryOperators.put(":=", " = ");
        BinaryOperators.put("^", "return");

        BinaryOperators.put("@", "$at");
        BinaryOperators.put(",", "$append");
        BinaryOperators.put("&", "$and");
        BinaryOperators.put("|", "$or");
        BinaryOperators.put("+", "$plus");
        BinaryOperators.put("-", "$minus");
        BinaryOperators.put("+=", "$incremented");
        BinaryOperators.put("-=", "$decremented");
        BinaryOperators.put("*", "$times");
        BinaryOperators.put("*=", "$magnified");
        BinaryOperators.put("/", "$dividedBy");
        BinaryOperators.put("/=", "$divided");
        BinaryOperators.put("//", "$idiv");
        BinaryOperators.put("\\", "$into");
        BinaryOperators.put("\\\\", "$imod");
        BinaryOperators.put("**", "$raised");
        BinaryOperators.put("%", "$modulus");
        BinaryOperators.put("<", "$lessThan");
        BinaryOperators.put("<=", "$lessEqual");
        BinaryOperators.put(">", "$moreThan");
        BinaryOperators.put(">=", "$moreEqual");
        BinaryOperators.put("=", "$equal");
        BinaryOperators.put("~=", "$notEqual");
        BinaryOperators.put("==", "$is");
        BinaryOperators.put("~~", "$isnt");
        BinaryOperators.put(">>", "$associateWith");

        BinaryOperators.put("$at", "@");
        BinaryOperators.put("$append", ",");
        BinaryOperators.put("$and", "&");
        BinaryOperators.put("$or", "|");
        BinaryOperators.put("$plus", "+");
        BinaryOperators.put("$minus", "-");
        BinaryOperators.put("$incremented", "+=");
        BinaryOperators.put("$decremented", "-=");
        BinaryOperators.put("$times", "*");
        BinaryOperators.put("$magnified", "*=");
        BinaryOperators.put("$dividedBy", "/");
        BinaryOperators.put("$divided", "/=");
        BinaryOperators.put("$idiv", "//");
        BinaryOperators.put("$into", "\\");
        BinaryOperators.put("$imod", "\\\\");
        BinaryOperators.put("$raised", "**");
        BinaryOperators.put("$modulus", "%");
        BinaryOperators.put("$lessThan", "<");
        BinaryOperators.put("$lessEqual", "<=");
        BinaryOperators.put("$moreThan", ">");
        BinaryOperators.put("$moreEqual", ">=");
        BinaryOperators.put("$equal", "=");
        BinaryOperators.put("$notEqual", "~=");
        BinaryOperators.put("$is", "==");
        BinaryOperators.put("$isnt", "~~");
        BinaryOperators.put("$associateWith", ">>");
    }

    /**
     * @return a primitive String
     * @param selector a Hoot selector
     */
    public static String from(String selector) {
        String op = BinaryOperators.getProperty(selector);
        if (op != null) {
            return op;
        }
        int tail = selector.length() - 1;
        while (tail > 0 && selector.charAt(tail) == COLON) {
            tail--;
        }
        selector = selector.substring(0, tail + 1);
        selector = selector.replace(COLON, UNDER);
        if (ReservedWords.contains(selector)) {
            selector = Dollar + selector;
        }
        return selector;
    }

    /**
     * @return a selector
     * @param methodName the name of a Java method.
     * @param argumentCount the number of method arguments.
     */
    public static String from(String methodName, int argumentCount) {
        String op = BinaryOperators.getProperty(methodName);
        if (op != null) {
            return op;
        }
        if (methodName.startsWith(Dollar)) {
            methodName = methodName.substring(1);
        }
        methodName = methodName.replace(UNDER, COLON);
        if (argumentCount > 0) {
            methodName = methodName + Colon;
        }
        return methodName;
    }

    /**
     * Returns the root package name from a (fully qualified) name.
     * @param fullName a (fully qualified) name
     * @return a root name if present, or empty
     */
    public static String rootPackage(String fullName) {
        String packageName = packageName(fullName);
        if (!packageName.contains(Dot)) return packageName;
        String[] parts = packageName.split(Escape + Dot);
        return parts[0];
    }

    /**
     * Returns the package name from a (fully qualified) name.
     * @param fullName a (fully qualified) name
     * @return a package name if present, or empty
     */
    public static String packageName(String fullName) {
        if (isEmpty(fullName)) return Empty;
        fullName = Name.asMetaMember(fullName.trim());
        if (!fullName.contains(Dot)) return Empty;

        String[] parts = fullName.split(Escape + Dot);
        String lastPart = parts[parts.length - 1];
        if (Character.isLowerCase(lastPart.charAt(0)) &&
            !Primitive.isPrimitiveType(lastPart)) return fullName;

        int headLength = fullName.length() - lastPart.length();
        return fullName.substring(0, headLength - 1);
    }

    /**
     * Returns the full type name from a (fully qualified) name.
     * @param fullName a (fully qualified) name
     * @return a type name if present, or empty
     */
    public static String typeName(String fullName) {
        if (isEmpty(fullName)) return Empty;
        fullName = Name.asMetaMember(fullName.trim());
        if (!fullName.contains(Dot)) return fullName;

        String[] parts = fullName.split(Escape + Dot);
        String lastPart = parts[parts.length - 1];
        if (Character.isLowerCase(lastPart.charAt(0)) &&
            !Primitive.isPrimitiveType(lastPart)) return Empty;

        return lastPart;
    }

//    public static boolean isSimple(String name) {
//        if (isEmpty(name)) return false;
//        if (isProper(name)) return false;
//        return packageName(name).isEmpty();
//    }

    public static boolean isProper(String name) {
        if (isEmpty(name)) return false;
        return Character.isUpperCase(name.charAt(0));
    }

    public static boolean isQualified(String name) {
        if (isEmpty(name)) return false;
        return !packageName(name).isEmpty();
    }

    public static boolean isMetaNamed(String name) { return isMetaclassNamed(name) || isMetatypeNamed(name); }
    public static boolean isMetaclassNamed(String name) { return !isEmpty(name) && name.endsWith(Metaclass); }
    public static boolean isMetatypeNamed(String name) { return !isEmpty(name) && name.endsWith(Metatype); }

    public static String asMetaMember(String name) {
        if (isMetaclassNamed(name)) return name.replace(Dot + Metaclass, Dollar + Metaclass);
        if (isMetatypeNamed(name)) return name.replace(Dot + Metatype, Dollar + Metatype);
        return name; // not really a meta-member
    }

    public static String withoutMeta(String name) {
        if (isMetaclassNamed(name)) return removeTail(name, Metaclass);
        if (isMetatypeNamed(name)) return removeTail(name, Metatype);
        return name; // not really a meta-member
    }

    public static String formType(String... names) {
        return formType(wrap(names));
    }

    public static String formType(List<String> names) {
        if (names.isEmpty()) return Empty;

        String tailMost = names.get(names.size() - 1);
        if (names.size() == 1) {
            return Primitive.isPrimitiveType(tailMost) ?
                    tailMost.toLowerCase() : tailMost;
        }

        List<String> heads = names.subList(0, names.size() - 1);
        String joinedHeads = joinWith(Dot, heads);
        String joinedLowers = joinedHeads.toLowerCase();

        String headMost = names.get(0);
        String fullName = joinWith(Dot, names);

        if (headMost.startsWith(Java)) {
            String[] fullerNames = { joinedLowers, tailMost };
            return joinWith(Dot, wrap(fullerNames));
        }
        return fullName;
    }

    public static String removeTail(String name, String tail) {
        if (!name.endsWith(tail)) return name;
        String result = name.substring(0, name.length() - tail.length());
        if (result.endsWith(Dot)) result = result.substring(0, result.length() - 1);
        if (result.endsWith(Dollar)) result = result.substring(0, result.length() - 1);
        return result.trim(); // no tailing blank(s) either
    }

} // Name
