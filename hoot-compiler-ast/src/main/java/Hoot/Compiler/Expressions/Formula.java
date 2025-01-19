package Hoot.Compiler.Expressions;

import java.util.*;
import Hoot.Runtime.Names.*;
import Hoot.Runtime.Behaviors.Scope;
import Hoot.Runtime.Emissions.Emission;
import static Hoot.Runtime.Functions.Utils.*;
import Hoot.Compiler.Scopes.*;

/**
 * A formula.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 1999,2025 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class Formula extends Message {

    public Formula() { super(Scope.currentBlock()); }
    @Override public void clean() { super.clean(); cleanPrimary(); cleanOps(); }

    public static Formula with(UnarySequence term) { return with(term, emptyList(BinaryMessage.class)); }
    public static Formula with(UnarySequence term, List<BinaryMessage> ops) {
        Formula result = new Formula();
        result.ops.addAll(select(ops, op -> hasOne(op)));
        result.containAll(result.ops());
        result.primaryTerm = term;
        result.contain(term);
        return result;
    }

    public static Formula frameReference() { return Formula.with(UnarySequence.with(Primary.frameGlobal())); }
    public static Formula emptyBlock() { return Formula.with(UnarySequence.with(Primary.with(Nest.emptyBlock()))); }


    UnarySequence primaryTerm;
    @Override public UnarySequence primaryTerm() { return this.primaryTerm; }
    protected void cleanPrimary() { this.primaryTerm.clean(); }

    List<BinaryMessage> ops = emptyList(BinaryMessage.class);
    public List<BinaryMessage> ops() { return this.ops; }
    protected void cleanOps() { ops().forEach(op -> op.clean()); }

    @Override public TypeName typeResolver() { return primaryTerm().typeResolver(); }
    private UnarySequence secondaryTerm() { return ops().get(0).primaryTerm(); }

    public boolean isLoopy() { return ops().isEmpty() && primaryTerm().isLoopy(); }
    @Override public boolean selfIsPrimary() { return primaryTerm().selfIsPrimary(); }
    @Override public boolean hasOnlyValue() { return ops().isEmpty() && primaryTerm().hasOnlyValue(); }
    @Override public boolean isConstructed() { return operationsCode().isEmpty() && primaryTerm().isConstructed(); }
    @Override public boolean producesPredicate() { return primaryTerm().producesPredicate(); }
    @Override public boolean containsExit() {
        return primaryTerm().exitsMethod() || matchAny(ops(), op -> op.exitsMethod()); }

    public Block primaryBlock() { return primaryTerm().primaryBlock(); }
    public Emission operandCode() { return primaryTerm().emitOperand(); }

    public List<Emission> primitiveCode() { return map(ops(), op -> op.emitPrimitive()); }
    public List<Emission> operationsCode() { return map(ops(), op -> op.emitOperand()); }
    public List<Operator> operators() { return map(ops(), op -> op.operator()); }

    public KeywordMessage toLoopMessage() {
        return KeywordMessage.with(primaryTerm().removeOnlySelector(), Formula.emptyBlock()); }

    @Override public Emission emitPrimitive() {
        if (ops().isEmpty()) {
            return primaryTerm().emitPrimitive();
        }

        if (ops().size() == 1) {
            if (operators().get(0).isCast()) {
                return emitCast(primaryTerm().primary().name(), secondaryTerm().emitPrimitive());
            }

            if (operators().get(0).isAssignment()) {
                return emitExpression(primaryTerm().emitPrimitive(), primitiveCode());
            }

            return emitTerm(emitExpression(primaryTerm().emitPrimitive(), primitiveCode()));
        }

        return emitExpression(primaryTerm().emitPrimitive(), primitiveCode());
    }

    @Override public Emission emitOperand() {
        if (ops().isEmpty()) {
            return primaryTerm().emitOperand();
        }

        if (ops().size() == 1) {
            Operator onlyOp = operators().get(0);
            if (onlyOp.isCast()) {
                return emitCast(primaryTerm().primary().name(), secondaryTerm().emitOperand());
            }

            if (onlyOp.isAssignment()) {
                return emitExpression(operandCode(), operationsCode());
            }
            else {
                return emitTerm(emitExpression(operandCode(), operationsCode()));
            }
        }

        return emitExpression(operandCode(), operationsCode());
    }

} // Formula
