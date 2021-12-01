package Hoot.Compiler.Expressions;

import java.util.*;
import Hoot.Runtime.Values.*;
import Hoot.Runtime.Behaviors.*;
import Hoot.Runtime.Emissions.*;
import Hoot.Runtime.Names.TypeName;
import static Hoot.Runtime.Functions.Utils.*;
import Hoot.Compiler.Scopes.*;
import static Hoot.Runtime.Names.Keyword.NewMessage;

/**
 * An expression.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 1999,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 * @see <a href="https://gitlab.com/hoot-smalltalk/hoot-smalltalk/tree/master/LICENSE.txt">LICENSE for more details</a>
 */
public class Expression extends Operand implements ScopeSource, MessageSource {

    public Expression(NamedItem container) { super(container); }
    @Override public void clean() { super.clean(); cleanFormula(); cleanMessage(); cleanCascades(); }
    @Override public void container(Item container) {
        super.container(container);
        this.contain(this.term);
        this.contain(this.message);
        this.containAll(cascadedMessages());
    }

    public static Expression frameNew() {
        return Expression.with(Formula.frameReference(), KeywordMessage.frameNew()); }

    public static Expression with(Formula term, KeywordMessage message, Message... cascades) {
        return Expression.with(term, message, wrap(cascades)); }

    public static Expression with(Formula term, KeywordMessage message, List<Message> cascades) {
        Expression result = new Expression(Scope.current());
        result.message = message;
        result.cascades.addAll(cascades);
        return result.withTerm(term);
    }

    @SuppressWarnings("unchecked")
    public <ResultType extends Expression> ResultType withTerm(Formula term) {
        if (term.isLoopy()) makeLoopy(term);
        this.term = term.inside(this);
        return (ResultType)this;
    }

    protected Formula term;
    public Formula formula() { return this.term; }
    public Operand receiver() { return formula(); }
    public UnarySequence primaryTerm() { return formula().primaryTerm(); }
    @Override public TypeName typeResolver() { return formula().typeResolver(); }
    public void cleanFormula() { if (this.term != null) this.term.clean(); }

    protected KeywordMessage message;
    public KeywordMessage keywordMessage() { return this.message; }
    public boolean hasMessage() { return keywordMessage() != null; }
    protected void cleanMessage() { if (hasMessage()) keywordMessage().clean(); }
    protected void makeLoopy(Formula term) { this.message = term.toLoopMessage().inside(this); }

    protected List<Message> cascades = emptyList(Message.class);
    public List<Message> cascadedMessages() { return this.cascades; }
    @Override public boolean hasCascades() { return !cascadedMessages().isEmpty(); }
    protected void cleanCascades() { this.cascades.forEach(m -> m.clean()); }

    protected boolean isEvaluated = false;
    public boolean isEvaluated() { return !this.exitsMethod() && this.isEvaluated; }
    public Expression makeEvaluated() { this.isEvaluated = true; return this; }

    protected boolean isAssigned = false;
    public boolean isAssigned() { return this.isAssigned; }
    @Override public Operand makeAssigned() { this.isAssigned = true; return this; }

    protected boolean exitsMethod = false;
    @Override public boolean isExit() { return this.exitsMethod; }
    @Override public boolean containsExit() {
        if (hasCascades() && matchAny(cascadedMessages(), m -> m.exitsMethod())) return true;
        if (hasMessage() && keywordMessage().exitsMethod()) return true;
        if (formula() == null) return false;
        return formula().exitsMethod();
    }

    public Expression makeExit() {
        this.exitsMethod = true;
        makeResult();
        return this;
    }

    @Override public boolean producesPredicate() {
        if (hasCascades() && matchAny(cascadedMessages(), m -> m.producesPredicate())) return true;
        if (hasMessage() && keywordMessage().producesPredicate()) return true;
        if (formula() == null) return false;
        return formula().producesPredicate();
    }

    @Override public boolean isFramed() {
        return hasParent(Block.class) && findParent(Block.class).needsFrame(); }

    public boolean parentExits() {
        return hasParent(Expression.class, Block.class) && findParent(Expression.class).isResult(); }

    public boolean hasPredicateTerm(Block aBlock) {
        return blockIsPrimary() && hasMessage() &&
                keywordMessage().needsPredicate() &&
                primaryTerm().primary().isBlock(aBlock); }

    public boolean hasPredicateArgument(Block aBlock) {
        return hasMessage() && keywordMessage().takesPredicate(aBlock); }

    @Override public boolean hasOnlyValue() {
        return !hasCascades() && !this.hasMessage() && formula().hasOnlyValue(); }

    public boolean isConstructed() {
        return !isResult() && !hasMessage() && formula().isConstructed(); }

    public boolean answersYourself() {
        return selfIsPrimary() && primaryTerm().containsOnlyYourself(); }

    public boolean variableIsPrimary() { return primaryTerm().primary().isLiteralName(); }
    public boolean blockIsPrimary() { return primaryTerm().primary().isBlock(); }
    @Override public boolean selfIsPrimary() { return formula().selfIsPrimary(); }
    @Override public boolean throwsException() { return primaryTerm().throwsException(); }
    public boolean optimizesAlternatives() { return this.isResult() || this.isAssigned(); }

    public boolean hasTypeNewPrimary() { return formula().primaryTerm().endsWith(NewMessage); }
    public boolean hasTypeNewMessage() { return hasMessage() && this.keywordMessage().methodName().equals(NewMessage); }
    public boolean startsWithTypeNew() { return hasTypeNewPrimary() || formula().primaryTerm().hasTypeNewMessage(); }

    @Override public Emission emitResult() {
        return !block().needsFrame() ? super.emitResult() :
                exitsMethod() ? super.emitResult() : super.emitResult(); }

    @Override public Emission emitOperand() { return hasCascades() ? emitCascades() : emitSimply(); }
    public Emission emitSimply() { return this.hasPrimitiveContext() ? emitPrimitive() : emitMessages(); }

    private Emission emitMessages() {
        return hasMessage() ? keywordMessage().emitCall(formula()) : formula().emitOperand(); }

    @Override public Emission emitPrimitive() {
        if (hasMessage()) {
            try {
                return keywordMessage().emitPrimitive(formula());
            }
            catch (Exception e) {
                String problem = method().description() + " " + e.getMessage();
                report(problem);
                throw e;
            }
        }

        return formula().emitPrimitive();
    }

    private Emission emitCascades() {
        if (selfIsPrimary()) {
            if (formula().hasOnlyValue()) {
                String typeName = face().typeFace().shortName();
                return emitSelfCascades(emitAllCascades(), typeName, isResult(), isFramed());
            }

            if (hasTypeNewPrimary()) {
                String typeName = face().typeFace().shortName();
                return emitExitCascades(emitAllCascades(), typeName, isResult(), isFramed());
            }

            if (primaryTerm().primary().isExpression() &&
                primaryTerm().primary().expression().hasTypeNewMessage()) {
                String typeName = face().typeFace().shortName();
                return emitExitCascades(emitAllCascades(), typeName, isResult(), isFramed());
            }
        }


        if (isAssigned()) {
            Variable v = variable();
            if (v.hasTypeNote()) {
                String typeName = v.notedType().simpleName();
                return emitAssignedCascades(emitAllCascades(), typeName, v.name());
            }
            else {
                Variable ref = v.referencedLocal();
                if (ref != null && ref.hasTypeNote()) {
                    String typeName = ref.notedType().simpleName();
                    return emitAssignedCascades(emitAllCascades(), typeName, v.name());
                }
            }
        }

        if (isResult()) {
            String typeName = typeResolver().shortName();
            return emitExitCascades(emitAllCascades(), typeName, isResult(), isFramed());
        }

        String typeName = formula().resolvedTypeName();
        if (!typeName.endsWith(".Object")) {
            return emitExitCascades(emitAllCascades(), typeName, isResult(), isFramed());
        }

        return emitSimply();
    }

    private List<Emission> emitAllCascades() { return emitCascades(formula()); }
    private List<Emission> emitCascades(Formula f) {
        List<Emission> messages = map(cascadedMessages(), m -> m.emitItem());
        if (hasMessage()) messages.add(0, keywordMessage().emitItem());
        messages.add(0, f.emitOperand());
        return messages; }

} // Expression
