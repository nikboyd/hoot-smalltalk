package Hoot.Compiler.Scopes;

import java.util.*;
import Hoot.Runtime.Notes.*;
import Hoot.Runtime.Values.*;
import Hoot.Runtime.Emissions.*;
import Hoot.Runtime.Behaviors.*;
import Hoot.Runtime.Names.TypeName;
import Hoot.Runtime.Behaviors.Scope;
import static Hoot.Runtime.Emissions.Emission.*;
import static Hoot.Runtime.Names.Keyword.Dollar;
import static Hoot.Runtime.Functions.Utils.*;

import Hoot.Compiler.Expressions.*;

/**
 * A block scope, which may contain a sequence of statements.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2025 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class Block extends Scope implements Signed, ClosureSource {

    public Block() { this(Block.currentBlock()); }
    public Block(Scope aScope) { super(aScope); checkScope(); }
    public static Block currentBlock() { return from(Scope.currentBlock()); }
    public static Block from(Item item) { return nullOr(b -> (Block)b, item); }
    public static Block emptyBlock() {
        Block result = new Block();
        result.signature(KeywordSignature.emptyNiladic());
        result.content(BlockContent.emptyBlock());
        return result;
    }

    @Override public void clean() { sig.clean(); content.clean(); super.clean(); }
    @Override public Block makeCurrent() { return (Block)Scope.makeCurrentBlock(this); }
    @Override public Scope popScope() { Scope.popBlockScope(); return currentBlock(); }
    private void checkScope() { if (hasNone(container())) warn("null parent scope in Block"); }

    @Override public boolean isBlock() { return true; }
    @Override public boolean isEmpty() { return hasNone(content()) || content().isEmpty(); }
    @Override public int nestLevel() { return scope().nestLevel() + 1; }

    public static final int MethodLevel = 2;
    public boolean isNested() { return nestLevel() > MethodLevel; }
    public Nest withNest() { return new Nest(this); }

    @Override public Typified faceType() { return face(); }
    @Override public String name() { return hasNone(signature()) ? Empty : signature().name(); }
    @Override public String qualify(String identifier) { return facialScope().qualify(identifier); }

    public List<Variable> arguments() { return signature().arguments(); }
    public List<String> argumentNames() { return map(arguments(), arg -> arg.name()); }
    @Override public int argumentCount() { return isSigned() ? arguments().size() : 0; }
    @Override public List<Typified> argumentTypes() { return map(arguments(), arg -> arg.typeFace()); }
    @Override public List<TypeName> argumentTypeNames() { return map(arguments(), arg -> arg.typeResolver()); }

    protected BlockContent content;
    public BlockContent content() { return this.content; }
    public void content(BlockContent content) { if (hasOne(content)) this.content = content; }

    protected BasicSignature sig;
    public BasicSignature signature() { return sig; }
    public boolean isSigned() { return hasOne(sig); }
    public void signature(BasicSignature s) { this.sig = s.inside(this); reportScope(); }

    @Override public Method method() { return signature().method(); }
    @Override public String description() { return method().description() + "::" + blockName(); }

    @Override public boolean hasLocal(String symbolName) {
        return super.hasLocal(symbolName) || signature().hasLocal(symbolName); }

    @Override public Variable localNamed(String symbolName) {
        if (super.hasLocal(symbolName)) return super.localNamed(symbolName);
        if (signature().hasLocal(symbolName)) return signature().localNamed(symbolName);
        return null; }

    public DetailedType resolvedType() { return signature().resolvedType(); }

    public boolean needsFrame() { return content().needsFrame(); }
    public boolean matches(Block scope) { return !missingAny(scope, content()) && content() == scope.content(); }

    public boolean isConstructor() { return false; }
    public boolean returnsVoid() { return signature().returnsVoid(); }
    public boolean receiverNeedsTerm() { return true; }

    @Override public boolean isVoidedContext() { return this.isConstructor() || this.returnsVoid(); }
    @Override public boolean containsExit() { return content().containsExit(); }

    @Override public Block parentBlock() { return findParentExactly(Block.class, Method.class, Face.class); }
    public Expression parentExpression() { return findParentExactly(Expression.class, Method.class, Face.class); }
    public boolean hasParentExpression() { return hasOne(parentExpression()); }
    public boolean expressionHasResult() { return falseOr(x -> x.isResult(), parentExpression()); }
    public boolean expressionExitsMethod() { return falseOr(x -> x.exitsMethod(), parentExpression()); }
    public boolean needsPredicateWrapper() {
        return falseOr(x -> x.hasPredicateTerm(this) || x.hasPredicateArgument(this), parentExpression()); }

    public String closureType() { return closureType(needsPredicateWrapper()); }
    public Emission emitClosure() { return emitOptimized(); }

    @Override public Emission emitOptimized() {
        return emitOptimizedBlock(emitEmpty(), nestLevel() - 1, closureType(),
            emitLines(emitBlockArguments()), emitContents(), emitArgumentNames()); }

    public List<Emission> emitCastedArguments() { return map(arguments(), arg -> arg.emitCast()); }
    public List<Emission> emitBlockArguments() { final int[] index = { 0 };
        return map(arguments(), arg -> arg.emitBlockArgument(index[0]++, nestLevel() - 1)); }

    public List<Emission> emitArguments() { return emitArguments(true); }
    public List<Emission> emitArguments(boolean useFinal) { return map(arguments(), arg -> arg.emitArgument(useFinal)); }
    public List<Emission> emitErasedArguments() { return map(arguments(), arg -> arg.emitErasedArgument()); }
    public List<Emission> emitArgumentNames() {
        List<Emission> results = map(arguments(), arg -> emitQuoted(arg.name()));
        if (results.isEmpty()) results.add(emitQuoted(Empty));
        return results; }

    public Emission emitResultType() {
        if (needsPredicateWrapper() || isNested()) return null;
        return isSigned() ? signature().emitResultType() : emitItem(face().defaultName()); }

    public Emission emitSignature() {
        Emission erasure = null;
        if (needsErasure()) erasure = emitErasure();
        return emitBlockSignature(blockName(), erasure, emitList(emitArguments()), emitEmpty()); }

    public Emission emitFinalValue() { return isEmpty() ? emitEmpty() : content().emitFinalValue(); }
    public Emission emitContents() {
        makeCurrent(); // manage scope
        try { return isEmpty() ? emitEmpty() : content().emitItem(); }
        finally { popScope(); }
    }

    static final String[] valueMessages = { "value", "value", "value_value" };
    private String valueMessage() { return valueMessages[argumentCount()]; }
    public String blockName() { return needsErasure() ? Dollar + valueMessage() : valueMessage(); }

    public Emission emitTry() { return emitOnlyTry(emitLocalVariables(), emitContents()); }
    public Emission emitCatch() { return emitOnlyCatch(emitArguments(false), emitLocalVariables(), emitContents()); }
    public Emission emitFinally() { return emitOnlyEnsure(emitLocalVariables(), emitContents()); }
    public Emission emitNewClosure() { return emitNewClosure(closureType(), nestLevel() - 1, emitArgumentNames()); }

    public boolean needsErasure() { return needsErasure(argumentCount()); }
    public boolean needsErasure(int argumentCount) {
        if (this.isMethod()) return false;
        if (argumentCount == 0) return false;
        return (signature().args().hasTypedNames() &&
               !signature().args().hasElementaryNames()); }

    public Emission emitErasedCall() {
        return returnsVoid() ?
                emitErasedVoid(valueMessage(), emitCastedArguments()) :
                emitErasedCall(valueMessage(), emitCastedArguments()) ; }

    public Emission emitErasure() {
        return emitErasedBlock(valueMessage(), emitErasedArguments(), emitErasedCall()) ; }

} // Block
