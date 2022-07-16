package Hoot.Compiler.Expressions;

import java.util.*;
import static java.lang.Integer.min;

import Hoot.Runtime.Emissions.*;
import Hoot.Runtime.Behaviors.*;
import Hoot.Runtime.Values.Operand;
import Hoot.Runtime.Names.Selector;
import static Hoot.Runtime.Functions.Utils.*;
import static Hoot.Runtime.Names.Keyword.*;
import static Hoot.Runtime.Behaviors.HootRegistry.*;
import Hoot.Compiler.Scopes.*;

/**
 * A message (method invocation).
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 1999,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class Message extends Expression implements MessageSource {

    public Message(NamedItem container) { super(container); }

    protected static final HashMap<String, String> LoopMap = emptyMap(String.class);
    public boolean needsPredicate() { return LoopMap.containsKey(methodName()); }
    static {
        LoopMap.put(WhileTrue, "WhileTrue");
        LoopMap.put(WhileFalse, "WhileFalse");
    }


    static final String basicNew = "basicNew";
    static final String ensurePhrase = "ensure:";
    static final String catchPhrase = "catch:";

    static final String performs[] = {
        ".perform",
        ".perform_with",
        ".perform_with_with",
        ".perform_with_with_with",
        ".perform_with_with_with_with",
        ".perform_withArguments"
    };

//    static Class[] blockClass = {Block.class};
    Selector selector = new Selector();
    public Selector selector() { return selector; }
    public void selector(String name) { selector.append(name); }

    public String methodName() { return selector().methodName(); }
    @Override public String name() { return selector().contents(); }
    @Override public String description() { return getClass().getName() + " = " + name(); }

    Class resultType = null;


    @Override public void container(Item container) {
        super.container(container);
        if (hasOperands()) {
            operands().forEach(op -> this.contain(op));
        }
    }

    public void cleanOperands() {
        List<Operand> copy = operands();
        operands.clear();
        for (Operand o : copy) {
            operands.add(o.cleanTerm());
        }
    }

    @Override public void clean() {
        super.clean();
        cleanOperands();
        String faceName = this.facialScope().name();
        if (!"Object".equals(faceName) && !selector.isEmpty()) {
            resolveType();
        }
    }

    public void resolveType() {
        int ax = 0;
        Class[] argumentTypes = new Class[operandCount() - 1];

        // first, try to resolve a method based on the argument types
        for (Operand argument : arguments()) {
            argumentTypes[ax++] = argument.resolvedType();
        }
        Mirror mirror = Mirror.forClass(receiver().resolvedType());
        resultType = mirror.methodType(methodName(), argumentTypes);
        if (resultType != null) {
            return;
        }
        if (resultType == null) {
            return;
        }

        // finally, try to resolve a method with type erasure
        for (int i = 0; i < argumentTypes.length; i++) {
            Class aClass = argumentTypes[i];
            while ((aClass = aClass.getSuperclass()) != null) {
                if (RootType().equals(aClass.getName())) {
                    argumentTypes[i] = aClass;
                }
            }
        }
        resultType = mirror.methodType(methodName(), argumentTypes);
    }

    public boolean canOptimizeInvocation() {
        return receiver().optimizes(selector()) || (resultType != null); }

    @Override public Class resolvedType() {
        return (resultType == null ? super.resolvedType() : resultType); }

    @Override public String resolvedTypeName() {
        return (resultType == null ? super.resolvedTypeName() : resultType.getName()); }

    public Operand firstArgument() { return (operands.size() < 2) ? null : operands.get(1); }
    public Operand finalOperand() { return operands.get(operands.size() - 1); }
    public void receiver(Operand receiver) { operands.add(0, receiver); }

    public Operand removeReceiver() {
        Operand result = receiver();
        operands.remove(0);
        return result;
    }

    public void replaceReceiver(Operand receiver) {
        removeReceiver();
        receiver(receiver);
    }

    ArrayList<Operand> operands = emptyList(Operand.class);
    public int operandCount() { return operands.size(); }
    public void addOperand(Object operand) { addOperand((Operand)operand); }
    public void addOperand(Operand operand) { operands.add(operand); }
    public List<Operand> operands() { return copyList(this.operands); }
    public boolean hasOperands() { return hasAny(this.operands); }

    public List<Operand> arguments() {
        List<Operand> results = operands();
        results.remove(0);
        return results;
    }

    @Override public boolean isMessage() { return true; }

    public String performString() { return performs[min(operands.size(), 6) - 1]; }
    public Emission performedMethodName() { return selector.emitQuotedMethodName(); }

    /**
     * @return whether the receiver has elementary type.
     */
    public boolean elementaryReceiver() {
        Operand receiver = receiver();
        if (receiver.resolvesToPrimitive()) return true;

        Typified typeFace = Face.named(receiver.resolvedTypeName());
        return (typeFace != null && typeFace.isElementaryType());
    }

    public boolean isPrimitiveOperation() {
        return (operandCount() == 2) && selector().isPrimitive() && receiver().resolvesToPrimitive(); }


    @Override public Emission emitOperand() {
        return elementaryReceiver() ? emitElementary() : emitInvocation(); }

    public Emission emitElementary() {
        return selector().isPrimitive() ? emitPrimitive() : emitOptimized(); }

    public Emission emitInvocation() {
        return canOptimizeInvocation() ? emitOptimized() : emitPerform(); }

    public Emission emitPerform() {
        if (selector().isEmpty()) return receiver().emitOperand();
        return emitPerform(receiver().emitOperand(), performString(), performedMethodName(), emitArguments()); }

    public List<Emission> emitArguments() { return map(arguments(), arg -> arg.emitOperand()); }

    public Emission emitOpPrim() {
        return emitOperation(selector().asPrimitiveOperator(), firstArgument().emitPrimitive()); }

    public Emission emitOp() {
        return emitOperation(selector().asPrimitiveOperator(), firstArgument().emitOperand()); }

    public Emission emitCall() { return emitCall(methodName()); }
    public Emission emitCall(String methodName) { return emitCall(methodName, emitArguments()); }
    public Emission emitExpression() { return emitExpression(receiver().emitOperand(), emitCall()); }

    @Override public Emission emitPrimitive() {
        return emitExpression(receiver().emitPrimitive(), emitOpPrim()); }

    @Override public Emission emitOptimized() {
        if (selector().isEmpty()) return receiver().emitOperand();
        if (selector().isSelfish()) return emitCall();
        return emitExpression(); }

    public Emission emitAlternatives(boolean positively, Operand trueBlock, Operand falseBlock) {
        return emitAlternatives(emitGuarded(receiver(), positively),
                emitOptimizedBlock(trueBlock), emitOptimizedBlock(falseBlock));
    }

    public Emission emitGuardedStatement(boolean positively, Operand aBlock) {
        return emitGuardedBlock(emitGuarded(receiver(), positively), emitStatement(emitOptimizedBlock(aBlock)));
    }

    public Emission emitGuardedStatement(boolean positively, Operand trueBlock, Operand falseBlock) {
        return emitGuardedPair(emitGuarded(receiver(), positively),
                emitStatement(emitOptimizedBlock(trueBlock)), emitStatement(emitOptimizedBlock(falseBlock)));
    }

    public Emission emitWhileLoop(boolean positively, Operand guardedBlock) {
        return emitWhileLoop(emitGuardedValue(receiver(), positively),
                emitStatement(emitClosureValue(emitOptimizedBlock(guardedBlock))));
    }

    public Emission emitGuardedValue(Operand value, boolean positively) {
        return positively ?
                emitTrueGuard(emitClosureValue(value.emitOperand())) :
                emitFalseGuard(emitClosureValue(value.emitOperand())); }

    public Emission emitGuarded(Operand value, boolean positively) {
        return positively ? emitTrueGuard(value.emitOperand()) : emitFalseGuard(value.emitOperand()); }

    public Emission emitOptimizedBlock(Operand aBlock) {
        return aBlock == null ? emitNil() : emitClosureValue(aBlock.emitOptimized()); }

} // Message
