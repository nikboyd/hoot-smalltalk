package Hoot.Compiler.Expressions;

import java.util.*;
import Hoot.Runtime.Names.Keyword;
import Hoot.Runtime.Names.TypeName;
import Hoot.Runtime.Behaviors.Scope;
import Hoot.Runtime.Emissions.Emission;
import static Hoot.Runtime.Values.Exit.*;
import static Hoot.Runtime.Functions.Utils.*;
import static Hoot.Runtime.Names.Keyword.*;
import static Hoot.Runtime.Names.Primitive.*;

import Hoot.Compiler.Scopes.*;
import Hoot.Compiler.Constants.*;

/**
 * An unary sequence.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 1999,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class UnarySequence extends Message {

    public UnarySequence() { super(Scope.current()); }
    @Override public void clean() { super.clean(); cleanPrimary(); }

    public static UnarySequence yourself() { return with(Primary.with(LiteralName.with(Self))); }
    public static UnarySequence frame() { return with(Primary.with(LiteralName.with(FrameId))); }
    public static UnarySequence exitFrom(Scope scope) {
        return with(Primary.with(LiteralName.with(quoteWith(NativeQuote, scope.description())))); }

    public static UnarySequence with(String selector) { return with(null, selector); }
    public static UnarySequence with(Primary term) { return with(term, emptyList(String.class)); }
    public static UnarySequence with(Primary term, String... selectors) {
        return with(term, wrap(selectors)); }

    public static UnarySequence with(Primary term, List<String> selectors) {
        UnarySequence result = new UnarySequence();
        result.selectors.addAll(selectors);
        return result.withPrimary(term);
    }


    List<String> selectors = emptyList(String.class);
    public List<String> selectors() { return selectors; }
    public boolean noSelectors() { return selectors().isEmpty(); }
    public boolean hasSelectors() { return !selectors().isEmpty(); }
    public void clearSelectors() { this.selectors.clear(); }
    public int countSelectors() { return selectors().size(); }
    public String firstSelector() { return selectors().get(0); }
    public String finalSelector() { return selectors().get(countSelectors() - 1); }
    @Override public boolean throwsException() { return endsWith(Keyword.Throw); }
    public boolean startsWith(String selector) { return hasSelectors() && firstSelector().equals(selector); }
    public boolean endsWith(String selector) { return hasSelectors() && finalSelector().equals(selector); }

    static final String PredicateMessage = "toPredicate";
    @Override public boolean producesPredicate() { return endsWith(PredicateMessage); }

    public boolean primaryEndsWith(String selector) {
        return hasPrimary() && primary().isExpression() && primary().expression().primaryTerm().endsWith(selector); }

    public boolean isLoopy() { return countSelectors() == 1 && LoopMap.containsKey(firstSelector()); }
    public String removeOnlySelector() {
        String result = firstSelector();
        clearSelectors();
        return result;
    }

    Primary primary = null;
    public Primary primary() { return primary; }
    protected void cleanPrimary() { if (hasPrimary()) this.primary.clean(); }
    public boolean noPrimary() { return hasNo(primary()); }
    public boolean hasPrimary() { return hasAny(primary()); }
    @Override public boolean containsExit() { return hasPrimary() && primary().exitsMethod(); }
    @Override public boolean hasOnlyValue() { return hasPrimary() && primary().hasOnlyValue() && noSelectors(); }
    @Override public boolean selfIsPrimary() { return hasPrimary() && primary().selfIsPrimary(); }
    public boolean superIsPrimary() { return hasPrimary() && primary().isSuper(); }
    public Block primaryBlock() { return primary().block(); }
    public UnarySequence withPrimary(Primary primary) {
        this.primary = primary;
        this.contain(primary);
        return this;
    }

    @Override public TypeName typeResolver() {
        return noPrimary() ? super.typeResolver() : primary().typeResolver(); }

    @Override public boolean isConstructed() { return noSelectors() && (selfIsPrimary() || superIsPrimary()); }
    public boolean containsOnlyClass() { return containsOnly(Keyword.ClassMessage); }
    public boolean containsOnlyYourself() { return containsOnly(Keyword.Yourself); }
    public boolean containsOnly(String selector) {
        return (countSelectors() == 1) && firstSelector().equals(selector); }

    public Emission emitPrimaryTerm() {
        if (noPrimary()) return emitEmpty();
        return this.hasPrimitiveContext() ? primary().emitPrimitive() : primary().emitOperand(); }

    @Override public List<Emission> emitArguments() { return EmptyList; }
    @Override public Emission emitExpression() { return emitExpression(emitPrimaryTerm(), emitMessages()); }
    public List<Emission> emitMessages() {  return map(selectors(), s -> emitCall(s)); }

    private Emission emitNewPrimary() { return emitNew(emitPrimaryTerm(), EmptyList); }
    private Emission emitTypeNewPrimary() {
        if (selfIsPrimary()) return emitExpression();
        return emitTypeNew(emitPrimaryTerm(), EmptyList); }

    @Override public Emission emitPrimitive() {
        if (throwsException()) return emitThrow();
        if (containsOnlyClass()) return emitClassAccess();

        if (endsWith(Keyword.BasicNew)) return emitNewPrimary();
        if (endsWith(Keyword.NewMessage)) return emitTypeNewPrimary();
        if (endsWith(Keyword.ArrayNew)) return emitNewArray(emitPrimaryTerm());

        if (startsWith(Keyword.BasicNew)) return emitNewExpression();
        if (startsWith(Keyword.NewMessage)) return emitTypeNewExpression();

        return emitExpression(noPrimary() ? emitEmpty() : primary().emitPrimitive(), emitMessages());
    }

    @Override public Emission emitOperand() {
        if (this.hasPrimitiveContext()) {
            if (this.containsOnlyClass()) {
                return emitClassAccess();
            }
        }

        if (throwsException()) return emitThrow();
        if (endsWith(Keyword.BasicNew)) return emitNewPrimary();
        if (endsWith(Keyword.NewMessage)) return emitTypeNewPrimary();
        if (endsWith(Keyword.ArrayNew)) return emitNewArray(emitPrimaryTerm());

        if (startsWith(Keyword.BasicNew)) return emitNewExpression();
        if (startsWith(Keyword.NewMessage)) return emitTypeNewExpression();

        return emitExpression();
    }

    private Emission emitClassAccess() {
        return emitExpression(emitPrimaryTerm(), emitPath(Keyword.ClassSelector)); }

    private Emission emitNewExpression() {
        List<Emission> messages = emitMessages();
        messages.remove(0); // remove 1st message
        return emitExpression(emitNewPrimary(), messages);
    }

    private Emission emitTypeNewExpression() {
        List<Emission> messages = emitMessages();
        messages.remove(0); // remove 1st message
        return emitExpression(emitTypeNewPrimary(), messages);
    }

    private Emission emitThrow() { return selfIsPrimary() ? emitThrowSelf() : emitThrowItem(); }
    public Emission emitThrowItem() {
        List<Emission> messages = emitMessages();
        messages.remove(messages.size() - 1);
        return emitThrow(emitExpression(emitPrimaryTerm(), messages)); }

} // UnarySequence
