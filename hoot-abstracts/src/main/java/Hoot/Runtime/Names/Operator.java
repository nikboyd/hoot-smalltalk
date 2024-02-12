package Hoot.Runtime.Names;

import java.util.*;
import static org.apache.commons.lang3.StringUtils.*;

import Hoot.Runtime.Faces.Named;
import static Hoot.Runtime.Functions.Utils.*;

/**
 * Maps between Hoot Smalltalk operators and Java method names.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2019 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class Operator implements Named {

    protected Operator(String op) { this.op = op; }
    public static Operator with(String op) { return new Operator(op); }
    public static String getOperator(String selector) {
        return (InverseMap.containsKey(selector)) ? InverseMap.get(selector) : Empty; }

    private String op = Empty;
    @Override public String name() { return emptyOr(opText -> opText, this.op); }

    public boolean isCast() { return Keyword.As.equals(methodName()); }
    public boolean isAssignment() { return !name().isEmpty() && AssignOperators.contains(name()); }
    public boolean assignsValue() { return !name().isEmpty() && Assign.equals(name()); }
    private String buildTerm(String x) { return TermL + x + TermR; }
    public String methodName() {
        if (isEmpty(name())) return Empty;
        return (OperatorMap.containsKey(name())
                ? OperatorMap.get(name()) : UnknownOp + buildTerm(name()));
    }


    public static final String Dot = ".";
    public static final String WildDot = "\\.";

    public static final String Minus = "-";
    public static final String Assign = ":=";
    public static final String Associate = ">>";
    public static final String AssociateStatic = ">>$";
    public static final String[] AssignOps = { "+=", "-=", "*=", "/=", "<<=", "^=", };
    public static final List<String> AssignOperators = wrap(AssignOps);


    private static final char TermL = '(';
    private static final char TermR = ')';
    private static final String UnknownOp = "unknownOp";
    private static final HashMap<String, String> OperatorMap = emptyMap();
    private static final HashMap<String, String> InverseMap = emptyMap();

    /**
     * Prepares the maps for use in translations.
     */
    static {
        OperatorMap.put("@", Keyword.At);
        OperatorMap.put("<-", Keyword.As);
        OperatorMap.put(Associate, "associateWith");

        OperatorMap.put(",", "append");
        OperatorMap.put("+", "plus");
        OperatorMap.put("+=", "add");
        OperatorMap.put(Minus, "minus");
        OperatorMap.put("-=", "subtract");
        OperatorMap.put("%", "modulo");
        OperatorMap.put("*", "times");
        OperatorMap.put("*=", "multiplyBy");
        OperatorMap.put("/=", "divideBy");
        OperatorMap.put("**", "raisedTo");
        OperatorMap.put("/", "divideWith");
        OperatorMap.put("//", "truncateWith");
        OperatorMap.put("\\\\", "remainderFrom");

        OperatorMap.put("=", "equals");
        OperatorMap.put("==", "isSame");
        OperatorMap.put("===", "isKindOf");
        OperatorMap.put("~~", "notSame");
        OperatorMap.put("~=", "notEqual");
        OperatorMap.put(">=", "notLess");
        OperatorMap.put("<=", "notMore");
        OperatorMap.put(">", "moreThan");
        OperatorMap.put("<", "lessThan");

        OperatorMap.put("&", "and");
        OperatorMap.put("|", "or");

        OperatorMap.put("<<=", "bitShiftWith");
        OperatorMap.put("^=", "bitXorWith");

        OperatorMap.entrySet().forEach(
            entry -> InverseMap.put(entry.getValue(), entry.getKey()));
    }

} // Operator
