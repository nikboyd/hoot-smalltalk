package Hoot.Runtime.Names;

import java.util.*;
import static Hoot.Runtime.Functions.Utils.*;
import static Hoot.Runtime.Faces.Logging.*;

/**
 * A keyword mapper. Knows Hoot keywords, and maps those to Java.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2019 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class Keyword {

    public static Keyword fromSelector(String selector) {
        return Keyword.with(map(wrap(selector.split(Colon)), p -> p + Colon), new ArrayList<>()); }

    public static Keyword with(String... heads) { return Keyword.with(wrap(heads)); }
    public static Keyword with(List<String> heads) {
        if (heads == null) return Keyword.with();
        if (heads.size() == 1) return Keyword.fromSelector(heads.get(0));
        return Keyword.with(heads, new ArrayList<>()); }

    public static Keyword with(List<String> heads, List<String> tails) {
        return new Keyword().withAll(heads).withAll(tails); }

    public boolean isCast() { return As.equals(methodName()); }
    public boolean isThrow() { return Throw.equals(methodName()); }
    public boolean isArrayNew() { return ArrayNew.equals(methodName()); }
    public boolean isBasicNew() { return BasicNew.equals(methodName()); }
    public boolean isElementGetter() { return At.equals(methodName()); }
    public boolean isElementSetter() { return AtPut.equals(methodName()); }
    public boolean isTypeCheck() { return InstanceOf.equals(methodName()); }
    public boolean isWhileTrue() { return WhileTrue.equals(methodName()); }
    public boolean isWhileFalse() { return WhileFalse.equals(methodName()); }
    public boolean isIfTrue() { return IfTrue.equals(methodName()); }
    public boolean isIfTrueFalse() { return IfTrueFalse.equals(methodName()); }
    public boolean isIfFalse() { return IfFalse.equals(methodName()); }
    public boolean isIfFalseTrue() { return IfFalseTrue.equals(methodName()); }

    protected final List<String> words = emptyList();
    protected Keyword withAll(List<String> words) { this.words.addAll(words); return this; }
    public String methodName() {
        if (words.isEmpty()) return Empty;
        String result = translate();
        while (result.endsWith(Under)) result = result.substring(0, result.length() - 1);
        return (MethodMap.containsKey(result) ? MethodMap.get(result) : result);
    }

    private String translate() {
        final int[] index = { 0 }; // tracks which word is being translated
        return joinWith(Empty, map(words, word -> translate(word, ++index[0]))); }

    private String translate(String keyword, int index) {
        if (isEmpty(keyword) || Under.equals(keyword)) return Empty;
        return keyword.replace(COLON, tailReplacement(index)).trim(); }

    private char tailReplacement(int index) { return (index == words.size() ? BLANK : SCORE); }

    public static String from(String methodName, int argumentCount) {
        String op = Operator.getOperator(methodName);
        if (!op.isEmpty()) return op;

        if (methodName.startsWith(Dollar)) methodName = methodName.substring(1);
        methodName = methodName.replace(Under, Colon);

        if (argumentCount > 0) methodName = methodName + COLON;
        return methodName; }

    public static final String Under = "_";
    public static final String Colon = ":";
    public static final String Dollar = "$";
    public static final String Arrayed = "[]";

    private static final char COLON = ':';
    private static final char BLANK = ' ';
    private static final char SCORE = '_';

    public static final String Public = "Public";
    public static final String Private = "Private";
    public static final String Protected = "Protected";
    public static final String Override = "Override";
    public static final String Default = "Default";

    public static final String Transient = "Transient";
    public static final String Abstract = "Abstract";
    public static final String Final = "Final";

    public static final String Stacked = "Stacked";
    public static final String Notice = "Notice";
    public static final String Native = "Native";
    public static final String Static = "Static";
    public static final String Property = "Property";
    public static final String Primitive = "Primitive";
    public static final String Synchronized = "Synchronized";

    private static final String[] MethodTypes = {
        "Public", "Protected", "Private",
        "Abstract", "Native", "Void", "Default",
        "Static", "Final", "Synchronized"
    };

    public static final String MetaclassType = "Metaclass";
    public static final String MetaclassBase = "MetaclassBase";
    public static final String TypeSuffix = "Type";

    public static final String Hoot = "Hoot";
    public static final String Java = "Java";
    public static final String Smalltalk = "Smalltalk";

    public static final String Any = "Any";
    public static final String Nil = "Nil";
    public static final String Void = "Void";

    public static final String Object = "Object";
    public static final String Subject = "Subject";
    public static final String Boolean = "Boolean";
    public static final String Behavior = "Behavior";
    public static final String Classified = "Classified";

    public static final String Streams = "Streams";
    public static final String Behaviors = "Behaviors";
    public static final String Magnitudes = "Magnitudes";
    public static final String Exceptions = "Exceptions";
    public static final String Collections = "Collections";

    public static final String True = "True";
    public static final String False = "False";
    public static final String Array = "Array";
    public static final String String = "String";
    public static final String Symbol = "Symbol";

    public static final String Fixed = "Fixed";
    public static final String Float = "Float";
    public static final String Double = "Double";
    public static final String Integer = "Integer";
    public static final String Character = "Character";
    public static final String SmallInteger = "SmallInteger";

    private static final String[] Reservations = {
        "Public",
        "Protected",
        "Private",
        "Transient",
        "Default",
        "Final"
    };

    public static final String As = "as";
    public static final String At = "at";
    public static final String AtPut = "at_put";
    public static final String Subtype = "subtype:";
    public static final String InstanceOf = "instanceOf";

    public static final String Throw = "throw";
    public static final String BasicNew = "basicNew";
    public static final String ArrayNew = "arrayNew";

    public static final String Null = "null";
    public static final String Self = "self";
    public static final String Super = "super";
    public static final String Yourself = "yourself";
    public static final String TypeSelector = "type";
    public static final String ClassSelector = "class";
    public static final String ClassMessage = "$class";
    public static final String ReturnMessage = "$return";
    public static final String NewMessage = "$new";
    public static final String DoMessage = "$do";
    public static final String EmptySelector = "isEmpty";
    public static final String EmptyMessage = SCORE + EmptySelector;

    public static final String Import = "import";
    public static final String ImportAll = "importAll";
    public static final String ImportStatics = "importStatics";

    public static final String IfTrue = "ifTrue";
    public static final String IfFalse = "ifFalse";
    public static final String IfTrueFalse = "ifTrue_ifFalse";
    public static final String IfFalseTrue = "ifFalse_ifTrue";

    public static final String Try = "try";
    public static final String OnDo = "on_do";
    public static final String Ensure = "ensure";
    public static final String Usage = "usage";
    public static final String Detect = "detect";
    public static final String ExclusivelyDo = "exclusivelyDo";
    public static final String ExclusivelyDoEach = "exclusivelyDoEach";

    public static final String[] Singles = { IfTrue, IfFalse, };
    public static final List<String> SinglesList = wrap(Singles);

    public static final String[] Alternatives = { IfTrue, IfFalse, IfTrueFalse, IfFalseTrue, };
    public static final List<String> AlternativesList = wrap(Alternatives);

    public static final String WhileTrue = "whileTrue";
    public static final String WhileFalse = "whileFalse";
    public static final String[] PredicatedWords =  { WhileTrue, WhileFalse, };
    public static final List<String> PredicatedKeywords = wrap(PredicatedWords);

    public static final String Or = "or";
    public static final String And = "and";
    public static final String Xor = "xor";
    public static final String[] LogicalOps = { Or, And, Xor, };
    public static final List<String> LogicKeywords = wrap(LogicalOps);

    static final String[] MappedSelectors = { "do", "new", "return", "class", };
    static final HashMap<String, String> MethodMap = new HashMap<>();

    /**
     * Prepares the maps used for translations.
     */
    static {
        for (String s : MappedSelectors) MethodMap.put(s, Dollar + s);
        MethodMap.put(EmptySelector, EmptyMessage);
    }

} // Keyword
