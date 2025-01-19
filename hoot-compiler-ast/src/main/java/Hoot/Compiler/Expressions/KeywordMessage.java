package Hoot.Compiler.Expressions;

import java.util.*;
import Hoot.Runtime.Values.*;
import Hoot.Runtime.Names.Keyword;
import Hoot.Runtime.Behaviors.Scope;
import Hoot.Runtime.Emissions.Emission;
import static Hoot.Runtime.Emissions.Emission.*;
import static Hoot.Runtime.Names.Keyword.*;
import static Hoot.Runtime.Functions.Utils.*;
import Hoot.Compiler.Scopes.*;

/**
 * A keyword message.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2025 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class KeywordMessage extends Message {

    public KeywordMessage() { super(Scope.currentBlock()); this.keyword = Keyword.with(); }
    protected KeywordMessage(Keyword keyword, List<Formula> items) {
        this(); this.keyword = keyword; this.terms.addAll(items); }

    public static KeywordMessage frameNew() { return KeywordMessage.with(BasicNew, Formula.with(UnarySequence.frame())); }
    public static KeywordMessage with(String head, Formula... items) {
        int count = items.length - 1;
        List<String> tails = emptyList(String.class);
        while (count > 0) { tails.add(Colon); count--; }
        return with(wrap(head), tails, wrap(items));
    }

    public static KeywordMessage with(Keyword keyword, List<Formula> items) {
        return new KeywordMessage(keyword, items).initialize(); }

    public static KeywordMessage with(List<String> heads, List<String> tails, List<Formula> items) {
        return with(Keyword.with(heads, tails), items); }

//    @Override public void clean() { super.clean(); cleanTerms(); }
    private KeywordMessage initialize() {
        this.operands.clear();
        this.operands.addAll(formulas());
        this.containAll(formulas());
        return this; }

    Keyword keyword;
    public Keyword methodKeyword() { return this.keyword; }
    @Override public String methodName() { return methodKeyword().methodName(); }
    @Override public boolean needsStatement() { return !this.hasPrimitiveBlock(); }
    @Override public boolean containsExit() { return matchAny(formulas(), f -> f.exitsMethod()); }

    public boolean needsPrimitiveCall() { return PrimitiveCode.containsKey(methodName()); }
    public boolean accessesElement() { return ElementMap.containsKey(methodName()); }
    public boolean hasPrimitiveBlock() { return PrimitiveMap.containsKey(methodName()); }
    public boolean isLogical() { return Keyword.LogicKeywords.contains(methodName()); }
    public boolean isPredicated() { return Keyword.PredicatedKeywords.contains(methodName()); }
    public boolean isExclusivelyDone() { return methodName().startsWith(Keyword.ExclusivelyDo); }
    public boolean isTypeNew() { return methodName().startsWith(Keyword.NewMessage); }
    public boolean handlesCurtailment() { return CurtailmentList.contains(methodName()); }
    public boolean messageExits() { return ExitMap.containsKey(methodName()); }

    List<Formula> terms = emptyList(Formula.class);
    public List<Formula> formulas() { return terms; }
    protected void cleanTerms() { formulas().forEach(f -> f.clean()); }
    public boolean primaryBlockExits() { return !terms.isEmpty() && terms.get(0).exitsMethod(); }
    public boolean takesPredicate(Block aBlock) {
        return PredicateMap.containsKey(methodName()) && matchAny(formulas(),
                f -> f.blockIsPrimary() && f.primaryTerm().primary().isBlock(aBlock)); }

    @Override public List<Emission> emitArguments() { return formulaCodes(); }
    public List<Emission> formulaPrims() { return map(formulas(), f -> f.emitPrimitive()); }
    public List<Emission> formulaCodes() { return map(formulas(), f -> f.emitOperand()); }
    public List<Emission> primaryBlockValues() { return map(formulas(), f -> f.primaryBlock().emitFinalValue()); }
    public List<Emission> primaryBlockContents() { return map(formulas(), f -> f.primaryBlock().emitContents()); }

    public Emission primaryTermCode() { return formulas().get(0).emitOperand(); }
    public Emission primaryBlockContent() { return formulas().get(0).primaryBlock().emitContents(); }
    public Emission secondaryBlockContent() { return formulas().get(1).primaryBlock().emitContents(); }

    protected Emission emitCurtailment(Emission term) {
        Emission tryBlock = emitCodes(PrimitiveMap, Keyword.Try, term);
        if (terms.size() > 1) {
            Emission catchBlock = emitCodes(PrimitiveMap, methodName(), primaryTermCode(), secondaryBlockContent());
            Emission[] blocks = { tryBlock, catchBlock };
            return emitSequence(wrap(blocks));
        }
        else {
            Emission finallyBlock = emitCodes(PrimitiveMap, methodName(), primaryBlockContent());
            Emission[] blocks = { tryBlock, finallyBlock };
            return emitSequence(wrap(blocks));
        }
    }

    protected Emission emitExclusivelyDo(String exclusiveName, Emission term, Block block) {
        return exclusiveName.equals(PrimitiveMap.get(Keyword.ExclusivelyDoEach)) ?
            emitExclusivelyDoEach(exclusiveName, term, block.arguments().get(0), block) :
            emitExclusively(exclusiveName, term, block.emitContents()); }

    protected Emission emitExclusivelyDoEach(String exclusiveName, Emission term, Variable v, Block block) {
        return emitExclusively(exclusiveName, term, v.emitType(), emitItem(v.name()), block.emitContents()); }

    protected Emission emitCode(Map<String, String> nameMap, Emission term, List<Emission> arguments) {
        String methodName = methodName();
        String codeName = nameMap.get(methodName);
        String[] argumentNames = ArgumentMap.get(methodName);
        return emitCode(codeName, argumentNames, term, arguments.get(0));
    }

    protected Emission emitCode(String codeName, String[] termNames, Emission term, Emission argument) {
        return emit(codeName).with(termNames[0], term).with(termNames[1], argument); }

    protected Emission emitSingleCode(Map<String, String> nameMap, Emission term, List<Emission> arguments) {
        String methodName = methodName();
        String codeName = nameMap.get(methodName);
        String[] argumentNames = ArgumentMap.get(methodName);
        return emitSingleCode(codeName, argumentNames, term, arguments);
    }

    protected Emission emitSingleCode(String codeName, String[] termNames, Emission term, List<Emission> arguments) {
        return emit(codeName).with(termNames[0], term).with(termNames[1], arguments); }

    protected Emission emitCodes(Map<String, String> nameMap, Emission term, List<Emission> arguments) {
        String methodName = methodName();
        String codeName = nameMap.get(methodName);
        String[] argumentNames = ArgumentMap.get(methodName);
        return emitCodes(codeName, argumentNames, term, arguments);
    }

    protected Emission emitCodes(Map<String, String> nameMap, String methodName, Emission term) {
        return emitCodes(nameMap, methodName, term, emptyList(Emission.class)); }

    protected Emission emitCodes(Map<String, String> nameMap, String methodName, Emission term, Emission... arguments) {
        return emitCodes(nameMap, methodName, term, wrap(arguments)); }

    protected Emission emitCodes(Map<String, String> nameMap, String methodName, Emission term, List<Emission> arguments) {
        return emitCodes(nameMap.get(methodName), ArgumentMap.get(methodName), term, arguments); }

    protected Emission emitCodes(String codeName, String[] termNames, Emission term, List<Emission> arguments) {
        int names = termNames.length; // names
        int count = arguments.size() + 1; // terms

        Emission result = emit(codeName).with(termNames[0], term);
        if (names == 2 && count > names) {
            result.with(termNames[1], arguments); // single list
        }
        else {
            for (int index = 1; index < termNames.length; index++) {
                result.with(termNames[index], arguments.get(index - 1));
            }
        }
        return result;
    }

    public boolean emitsAlternative() {
        return parentExits() && Keyword.AlternativesList.contains(methodName()); }

    public Emission emitAlternatives(Emission condition) {
        return emitAlternatives(condition, primaryBlockValues()); }

    public Emission emitAlternatives(Emission condition, List<Emission> terms) {
        if (terms.size() < 2) terms.add(emitNil());
        return emitAlternatives(condition, terms.get(0), terms.get(1)); }

    @Override public Emission emitOperand() {
        if (emitsAlternative()) return emitAlternatives(emitGuard(formula()));
        if (hasPrimitiveBlock())
            return (handlesCurtailment()) ? emitCurtailedBlocks(formula()) : emitPrimitiveBlock(formula());

        return emitCall(); }

    public Emission emitCall(Formula receiver) {
        if (this.hasPrimitiveContext()) return emitPrimitive(receiver);
        if (this.needsPrimitiveCall() && !this.accessesElement()) {
            if (isTypeNew()) { // handle new: message
                return receiver.selfIsPrimary()?
                    emitExpression(receiver.emitOperand(), emitCall()) :
                    emitTypeNew(receiver.emitOperand(), emitArguments()) ;
            }
            return emitPrimitiveCall(receiver);
        }

        return emitExpression(receiver.emitOperand(), emitCall()); }

    public Emission emitPrimitive(Formula receiver) {
        if (emitsAlternative()) return emitAlternatives(emitGuard(receiver));

        if (isExclusivelyDone())
            return emitExclusivelyDo(PrimitiveMap.get(methodName()),
                receiver.emitPrimitive(), formulas().get(0).primaryBlock());

        if (hasPrimitiveBlock())
            return (handlesCurtailment()) ? emitCurtailedBlocks(receiver) : emitPrimitiveBlock(receiver);

        if (needsPrimitiveCall()) return emitPrimitiveCall(receiver);
        return emitExpression(receiver.emitPrimitive(), emitCall()); }

    private Emission emitValue(Formula receiver) {
        return needsPredicate() ? receiver.primaryBlock().emitFinalValue() : receiver.emitPrimitive(); }

    private Emission emitGuard(Formula receiver) { return emitTrueGuard(receiver.emitPrimitive()); }
    private Emission emitExitBlock(Formula receiver) {
        return emitCodes(ExitMap, emitGuard(receiver), primaryBlockContents()); }

    private Emission emitPrimitiveBlock(Formula receiver) {
        return emitCodes(PrimitiveMap, emitValue(receiver), primaryBlockContents()); }

    private Emission emitPrimitiveCall(Formula receiver) {
        return emitCodes(PrimitiveCode, receiver.emitPrimitive(), formulaPrims()); }

    private Emission emitCurtailedBlocks(Formula receiver) {
        Emission tryBlock = emitCodes(PrimitiveMap, Keyword.Try, receiver.primaryBlock().emitContents());
        if (terms.size() > 1) {
            Emission catchBlock = emitCodes(PrimitiveMap, methodName(), primaryTermCode(), secondaryBlockContent());
            Emission[] blocks = { tryBlock, catchBlock };
            return emitSequence(wrap(blocks));
        }
        else {
            Emission finallyBlock = emitCodes(PrimitiveMap, methodName(), primaryBlockContent());
            Emission[] blocks = { tryBlock, finallyBlock };
            return emitSequence(wrap(blocks));
        }
    }


    static final HashMap<String, String> ExitMap = emptyWordMap();
    static final HashMap<String, String> ElementMap = emptyWordMap();
    static final HashMap<String, String> PredicateMap = emptyWordMap();
    static final HashMap<String, String> PrimitiveMap = emptyWordMap();
    static final HashMap<String, String> PrimitiveCode = emptyWordMap();

    static final HashMap<String, String[]> ArgumentMap = emptyMap(String[].class);
    static final ArrayList<String> CurtailmentList = emptyList(String.class);

    static {
        ElementMap.put(Keyword.At, "ElementGet");
        ElementMap.put(Keyword.AtPut, "ElementSet");

        PredicateMap.put(Or, "Or");
        PredicateMap.put(And, "And");
        PredicateMap.put(Detect, "Detect");
        PredicateMap.put(Detect + "_ifNone", "DetectIfNone");

        ExitMap.put(Keyword.IfTrue, "TrueExit");
        ExitMap.put(Keyword.IfFalse, "FalseExit");
        ExitMap.put(Keyword.IfTrueFalse, "TrueFalseExit");

        PrimitiveCode.put(Keyword.As, "Cast");
        PrimitiveCode.put(Keyword.At, "ElementGet");
        PrimitiveCode.put(Keyword.AtPut, "ElementSet");
        PrimitiveCode.put(Keyword.InstanceOf, "InstanceOf");
        PrimitiveCode.put(Keyword.BasicNew, "New");
        PrimitiveCode.put(Keyword.ArrayNew, "NewArray");
        PrimitiveCode.put(Keyword.NewMessage, "TypeNew");

        PrimitiveMap.put(Keyword.WhileTrue, "WhileTrue");
        PrimitiveMap.put(Keyword.WhileFalse, "WhileFalse");
        PrimitiveMap.put(Keyword.IfTrue, "IfTrue");
        PrimitiveMap.put(Keyword.IfFalse, "IfFalse");
        PrimitiveMap.put(Keyword.IfTrueFalse, "IfTrueFalse");
        PrimitiveMap.put(Keyword.IfFalseTrue, "IfFalseTrue");

        PrimitiveMap.put(Keyword.Try, "OnlyTry");
        PrimitiveMap.put(Keyword.OnDo, "OnlyCatch");
        PrimitiveMap.put(Keyword.Ensure, "OnlyEnsure");
        PrimitiveMap.put(Keyword.Usage, "UseResource");
        PrimitiveMap.put(Keyword.ExclusivelyDo, "ExclusivelyDo");
        PrimitiveMap.put(Keyword.ExclusivelyDoEach, "ExclusivelyDoEach");

        CurtailmentList.add(Keyword.OnDo);
        CurtailmentList.add(Keyword.Ensure);

        ArgumentMap.put(Keyword.WhileTrue, new String[]{ "condition", "guardedBlock" });
        ArgumentMap.put(Keyword.WhileFalse, new String[]{ "condition", "guardedBlock" });

        ArgumentMap.put(Keyword.IfTrue, new String[]{ "condition", "trueValue" });
        ArgumentMap.put(Keyword.IfFalse, new String[]{ "condition", "falseValue" });

        ArgumentMap.put(Keyword.IfTrueFalse, new String[]{ "condition", "trueValue", "falseValue" });
        ArgumentMap.put(Keyword.IfFalseTrue, new String[]{ "condition", "falseValue", "trueValue" });

        ArgumentMap.put(Keyword.Try, new String[]{ "tryBlock" });
        ArgumentMap.put(Keyword.OnDo, new String[]{ "exceptionClass", "catchBlock" });
        ArgumentMap.put(Keyword.Ensure, new String[]{ "finallyBlock" });
        ArgumentMap.put(Keyword.Usage, new String[]{ "resource", "block" });
        ArgumentMap.put(Keyword.ExclusivelyDo, new String[]{"resource", "block"});

        ArgumentMap.put(Keyword.Throw, new String[]{ "item" });
        ArgumentMap.put(Keyword.BasicNew, new String[]{ "className", "arguments" });
        ArgumentMap.put(Keyword.ArrayNew, new String[]{ "className", "size" });
        ArgumentMap.put(Keyword.NewMessage, new String[]{ "className", "arguments" });

        ArgumentMap.put(Keyword.As, new String[]{ "type", "value" });
        ArgumentMap.put(Keyword.At, new String[]{ "name", "index" });
        ArgumentMap.put(Keyword.AtPut, new String[]{ "name", "index", "value" });
        ArgumentMap.put(Keyword.InstanceOf, new String[]{ "name", "className" });
    }

} // KeywordMessage
