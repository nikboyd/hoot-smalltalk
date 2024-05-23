package Hoot.Compiler.Expressions;

import Hoot.Compiler.Scopes.*;
import Hoot.Runtime.Emissions.*;
import Hoot.Runtime.Faces.Resulting;
import Hoot.Runtime.Values.Operand;
import Hoot.Runtime.Behaviors.Scope;
import static Hoot.Runtime.Functions.Utils.*;

/**
 * A statement (or result).
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 1999,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class Statement extends Item implements ScopeSource, Resulting {

    public Statement(Item parent) { super(parent); }
    protected Statement(Operand item) { this(Scope.currentBlock()); this.item = item.inside(this); }
    public static Statement with(Operand item) { if (item == null) return null; return new Statement(item); }
    @Override public void clean() { super.clean(); this.item.clean(); }

    protected Operand item;
    public boolean isConstruct() { return item.isConstruct(); }
    @Override public boolean isExit() { return item.isExit(); }
    @Override public boolean containsExit() { return item.containsExit(); }
    @Override public boolean isResult() { return item.isResult(); }
    @Override public boolean isFramed() { return falseOr(m -> m.needsFrame(), method()); }

    public boolean parentProducesPredicate() { return falseOr(p -> p.producesPredicate(), findParent(Statement.class)); }
    public boolean producesPredicate() { return item.producesPredicate(); }
    public boolean throwsException() { return this.item.throwsException(); }
    public Statement makeResult() { item.makeResult(); return this; }

    @Override public Emission emitItem() { return this.isResult() ? emitValue() : emitStatement(); }
    public Emission emitValue() {
        if (this.isResult()) {
            // only exits are framed, results are not
            if (this.isFramed() && this.exitsMethod()) {
                if (producesPredicate() || parentProducesPredicate()) {
//                    report("framed statement produces predicate " + method().description());
                    return emitResult(); // don't emit exits for toPredicate
                }
                Emission type = method().signature().emitResultType();
                return emitExit(emitOperand(), type);
            }
            return emitResult();
        }
        return emitOperand();
    }

    private Emission emitStatement() {
        if (method().isPrimitive()) {
            return this.item.needsStatement() ?
                this.item.emitStatement(this.item.emitPrimitive()) :
                this.item.emitPrimitive();
        }

        return this.item.emitStatement(); }

    private Emission emitResult() {
        if (this.item.hasCascades()) {
            return this.item.emitOperand();
        }
        return method().isPrimitive() ?
                this.item.emitResult(block().emitResultType(), this.item.emitPrimitive()) :
                this.item.emitResult(block().emitResultType(), this.item.emitOperand()); }

    private Emission emitOperand() {
        return method().isPrimitive() ?
                this.item.emitPrimitive() :
                this.item.emitOperand(); }

} // Statement
