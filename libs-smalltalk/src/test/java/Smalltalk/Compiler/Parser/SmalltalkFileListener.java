package Smalltalk.Compiler.Parser;

import java.util.*;
import java.util.function.*;
import Hoot.Runtime.Names.*;
import Hoot.Runtime.Values.*;
import Hoot.Runtime.Behaviors.Scope;

import Hoot.Compiler.Scopes.*;
import Hoot.Compiler.Scopes.File;
import Hoot.Compiler.Scopes.Block;
import Hoot.Compiler.Expressions.*;
import Hoot.Compiler.Constants.*;
import Hoot.Runtime.Faces.Logging;
import static Hoot.Runtime.Functions.Utils.*;
import static Smalltalk.Compiler.Parser.SmalltalkParser.*;

/**
 * Implements AST walking for the compiler.
 * @author Nik Boyd <nik.boyd@educery.dev>
 */
public class SmalltalkFileListener extends SmalltalkBaseListener implements Logging {

    File fileScope() { return File.currentFile(); }
    Face activeFace() { return fileScope().activeFace(); }

    String quoted(String text) { return String.format("'%s'", text); }
    @Override public void exitFiledComment(FiledCommentContext ctx) {
        report("file comment: "+quoted(ctx.fc.getText())); }
    
    @Override public void exitCodedComment(CodedCommentContext ctx) {
        report("code comment: "+quoted(ctx.cc.getText())); }

    // Global subclass: Symbol instanceVariableNames: String 
    //      classVariableNames: String poolDictionaries: String category: String
    @Override public void exitClassSignature(ClassSignatureContext ctx) {
        Global superGlobal = globalFrom(value(ctx.x.f));
        KeywordMessage m = message(ctx.x.kmsg);
        Global subGlobal = globalFrom(primarySymbol(m.formulas().get(0)));
        String message = m.methodName();
        ClassSignature cs = ClassSignature.with(superGlobal, subGlobal, message);
        String[] vars = primaryString(m.formulas().get(1)).unquotedValue().split(" ");
        report(cs.description());
        report(wrap(vars).toString());
        //for (String v : vars) Variable.from(Face.currentFace(), v, DetailedType.RootType).defineMember();
        //Face.currentFace().makeCurrent(); // no op
    }
    
    // Global methodsFor: String stamp: String
    @Override public void exitProtoHeader(ProtoHeaderContext ctx) {
        Global classGlobal = globalFrom(value(ctx.p.f));
        KeywordMessage m = message(ctx.p.kmsg);
        LiteralString proto = primaryString(m.formulas().get(0));
        report(classGlobal.name()+" proto: "+quoted(proto.unquotedValue()));
        //Face face = Face.currentFace();
    }
    
    @Override public void exitMethodReader(MethodReaderContext ctx) {
        activeFace().addMethod(methodScope(ctx.ms));
    }

    Method methodScope(MethodScopeContext ctx) {
        Method m = new Method(activeFace()).makeCurrent();
        m.signature(sign(ctx));
        m.content(blockFill(ctx));
        return m; }

    Block blockScope(BlockContext ctx) {
        Block b = new Block().makeCurrent();
        b.signature(sign(ctx.b.sign));
        b.signature().defineLocals();
        b.content(blockFill(ctx.b));
        return b; }


    Global globalFrom(Formula f) { return f.primaryTerm().primary().asGlobal(); }
    Global globalFrom(LiteralSymbol s) { return Global.named(s.encodedValue()); }
    LiteralSymbol primarySymbol(Formula f) { return f.primaryTerm().primary().asSymbol(); }
    LiteralString primaryString(Formula f) { return f.primaryTerm().primary().asString(); }

    Nest nest(BlockContext ctx) { return blockScope(ctx).withNest(); }
    BlockContent blockFill(MethodScopeContext ctx) { return blockFill(ctx.content); }
    public BlockContent blockFill(BlockScopeContext ctx)  { return blockFill(ctx.content); }
    BlockContent blockFill(BlockFillContext ctx) { return BlockContent.with(evals(ctx.s), value(ctx.r), ctx.p.size()); }

    List<Variable> args(List<ArgumentContext> ns) { return map(ns, n -> value(n)); }
    Keyword keyword(KeywordMessageContext ctx) { return Keyword.with(heads(ctx.kh)); }
    Keyword keyword(KeywordSignContext ctx) { return Keyword.with(heads(ctx.name.kh)); }
    List<String> heads(List<KeywordHeadContext> cs) { return map(cs, c -> c.getText()); }
    List<String> tails(List<KeywordTailContext> cs) { return map(cs, c -> c.getText()); }

    List<String> selectors(List<UnarySelectorContext> cs) { return map(cs, s -> selector(s)); }
    String selector(UnarySelectorContext ctx) { return Keyword.with(ctx.s.getText()).methodName(); }
    UnarySequence message(UnarySelectionContext ctx) { return UnarySequence.with(selector(ctx.umsg)); }
    UnarySequence message(UnarySequenceContext ctx) { return UnarySequence.with(value(ctx.p), selectors(ctx.msgs)); }

    List<BinaryMessage> messages(List<BinaryMessageContext> ms) { return map(ms, m -> message(m)); }
    BinaryMessage message(BinaryMessageContext ctx) { return BinaryMessage.with(message(ctx.operator), term(ctx)); }
    BinaryMessage message(BinarySelectionContext ctx) { return message(ctx.bmsg);  }
    Operator message(BinaryOperatorContext ctx) { return Operator.with(ctx.s.getText()); }

    KeywordMessage message(KeywordSelectionContext ctx) { return message(ctx.kmsg); }
    KeywordMessage message(KeywordMessageContext ctx) { return hasNone(ctx)? null: send(ctx); }
    KeywordMessage send(KeywordMessageContext ctx) { return KeywordMessage.with(keyword(ctx), values(ctx.fs)); }
    List<Message> cascades(List<MessageCascadeContext> ms) { return map(ms, mc -> message(mc.m)); }
    Message message(MessageContext ctx) { return applyMatched(messages, ctx); }

    Primary value(TermContext ctx)     { return Primary.with(value(ctx.n)); }
    Primary value(LitValueContext ctx) { return Primary.with(literal(ctx.l)); }
    Primary value(VariableContext ctx) { return Primary.with(literal(ctx)); }
    Primary value(PrimaryContext ctx)  { return applyMatched(terms, ctx); }
    Primary value(BlockContext ctx)    {
        Nest n = nest(ctx);
        try {
            return Primary.with(n);
        }
        finally {
            Scope.popBlockScope();
        }
    }
    
    List<Formula> values(List<FormulaContext> fs) { return map(fs, f -> value(f)); }
    Formula value(FormulaContext ctx) { return Formula.with(message(ctx.s), messages(ctx.ops)); }
    Formula term(BinaryMessageContext ctx) { return Formula.with(message(ctx.term)); }

    Expression value(NestedTermContext ctx) { return value(ctx.term); }
    Expression value(ExpressionContext ctx) { return hasNone(ctx)? null: send(ctx); }
    Expression value(EvaluationContext ctx) { return value(ctx.value).makeEvaluated(); }
    Expression value(ExitResultContext ctx) { return !hasExit(ctx)? null: value(ctx.value).makeExit(); }
    Expression send(ExpressionContext ctx) { return Expression.with(value(ctx.f), message(ctx.kmsg), cascades(ctx.cmsgs)); }
    boolean hasExit(ExitResultContext ctx) { return hasOne(ctx) && hasOne(ctx.value); }
    
    String name(GlobalValueContext ctx) { return name(ctx.g); }
    String name(GlobalNameContext ctx) { return ctx.g.getText(); }
    String name(LocalValueContext ctx) { return name(ctx.v); }
    String name(LocalNameContext ctx) { return ctx.v.getText(); }
    String name(ValueNameContext ctx) { return applyMatched(names, ctx); }

    // signatures

    BasicSignature   sign(MethodScopeContext ctx) { return sign(ctx.sign); }
    BasicSignature   sign(MethodSignContext ctx)  { return applyMatched(signs, ctx); }
    KeywordSignature sign(BlockSignContext ctx)   { return KeywordSignature.with(null, args(ctx.args)); }
    KeywordSignature sign(KeywordSignContext ctx) { return KeywordSignature.with(null, args(ctx.name.args), keyword(ctx)); }
    BinarySignature  sign(BinarySignContext ctx)  { return BinarySignature.with(null, args(ctx.args), message(ctx.name)); }
    UnarySignature   sign(UnarySignContext ctx)   { return UnarySignature.with(null, selector(ctx.name)); }
    UnarySignature   sign(UnarySigContext ctx)    { return sign(ctx.us); }
    BinarySignature  sign(BinarySigContext ctx)   { return sign(ctx.bs); }
    KeywordSignature sign(KeywordSigContext ctx)  { return sign(ctx.ks); }

    List<Statement> evals(List<StatementContext> ss) { return mapList(ss, s -> hasOne(s), s -> value(s)); }
    Statement value(StatementContext ctx) { return hasNone(ctx)? null: say(ctx); }
    Statement say(StatementContext ctx) { return Statement.with(hasOne(ctx.v)? value(ctx.v): value(ctx.n)); }

    Variable part(ArgumentContext ctx)   { return Variable.argNamed(name(ctx.v), null); }
    Variable part(AssignmentContext ctx) { return Variable.named(name(ctx.v), null, value(ctx.value)); }

    Variable value(ArgumentContext ctx)   { return part(ctx); }
    Variable value(AssignmentContext ctx) { return part(ctx).makeAssignment(); }

    Global value(GlobalReferContext ctx) { return Global.withList(map(ctx.names, n -> name(n))); }
    Constant value(LiteralValueContext ctx) { return literal(ctx.lit); }
    Constant value(ElementValueContext ctx) { return applyMatched(elements, ctx); }

    LiteralArray literal(ArrayLiteralContext ctx) { return values(ctx.array); }
    LiteralArray values(ElementValuesContext ctx) { return LiteralArray.withItems(cons(ctx.array)); }
    List<Constant> cons(List<ElementValueContext> vs) { return map(vs, v -> value(v)); }

    Constant literal(SelfishContext ctx) { return applyMatched(selfs, ctx); }
    Constant literal(LiteralContext ctx) { return applyMatched(lits, ctx); }

    LiteralName literal(VariableContext ctx) { return LiteralName.with(name(ctx.v)); }
    LiteralName literal(SelfSelfishContext ctx) { return LiteralName.with(ctx.refSelf.getText(), ctx.start.getLine()); }
    LiteralName literal(SuperSelfishContext ctx) { return LiteralName.with(ctx.refSuper.getText(), ctx.start.getLine()); }
    LiteralName literal(SelfLiteralContext ctx) { return LiteralName.with(ctx.refSelf.getText(), ctx.start.getLine()); }
    LiteralName literal(SuperLiteralContext ctx) { return LiteralName.with(ctx.refSuper.getText(), ctx.start.getLine()); }

    LiteralNil literal(NilLiteralContext ctx) { return LiteralNil.with(ctx.refNil.getText(), ctx.start.getLine()); }
    LiteralRadical literal(NumLiteralContext ctx) { return LiteralRadical.with(ctx.n.getText(), ctx.start.getLine()); }
    LiteralDecimal literal(DecimalLiteralContext ctx) { return LiteralDecimal.with(ctx.value.getText(), ctx.start.getLine()); }

    LiteralCharacter literal(CharLiteralContext ctx) { return LiteralCharacter.with(ctx.value.getText(), ctx.start.getLine()); }
    LiteralBoolean literal(BoolLiteralContext ctx) { return LiteralBoolean.with(ctx.bool.getText(), ctx.start.getLine()); }

    LiteralFloat literal(FloatLiteralContext ctx) { return LiteralFloat.with(ctx.value.getText(), ctx.start.getLine()); }
    LiteralInteger literal(IntLiteralContext ctx) { return LiteralInteger.with(ctx.value.getText(), ctx.start.getLine()); }

    LiteralSymbol literal(SymbolLiteralContext ctx) { return LiteralSymbol.with(ctx.value.getText(), ctx.start.getLine()); }
    LiteralString literal(StringLiteralContext ctx) { return LiteralString.with(ctx.value.getText(), ctx.start.getLine()); }

    @SuppressWarnings("unchecked") <T,R> R apply(Function f, T it) { return (R)f.apply(it); }
    @SuppressWarnings("unchecked") <B, T extends B, R> R applyMatched(HashMap<Class, Function<? extends B,R>> m, B it) {
        if (hasNone(it)) return null;
        for (Class c : m.keySet())
            if (c.isInstance(it)) return apply(m.get(c), c.cast(it));
        return null; }

    final HashMap<Class, Function<? extends LiteralContext, Constant>> lits = new HashMap<>();
    final HashMap<Class, Function<? extends PrimaryContext, Primary>> terms = new HashMap<>();
    final HashMap<Class, Function<? extends ValueNameContext, String>> names = new HashMap<>();
    final HashMap<Class, Function<? extends SelfishContext, Constant>> selfs = new HashMap<>();
    final HashMap<Class, Function<? extends MessageContext, Message>> messages = new HashMap<>();
    final HashMap<Class, Function<? extends ElementValueContext, Constant>> elements = new HashMap<>();
    final HashMap<Class, Function<? extends MethodSignContext, BasicSignature>> signs = new HashMap<>();
    public SmalltalkFileListener() {
        lits.put(ArrayLiteralContext.class, (ArrayLiteralContext ctx) -> literal(ctx));
        lits.put(NilLiteralContext.class,   (NilLiteralContext ctx) -> literal(ctx));
        lits.put(SelfLiteralContext.class,  (SelfLiteralContext ctx) -> literal(ctx));
        lits.put(SuperLiteralContext.class, (SuperLiteralContext ctx) -> literal(ctx));
        lits.put(BoolLiteralContext.class,  (BoolLiteralContext ctx) -> literal(ctx));
        lits.put(CharLiteralContext.class,  (CharLiteralContext ctx) -> literal(ctx));
        lits.put(FloatLiteralContext.class, (FloatLiteralContext ctx) -> literal(ctx));
        lits.put(IntLiteralContext.class,   (IntLiteralContext ctx) -> literal(ctx));
        lits.put(NumLiteralContext.class,   (NumLiteralContext ctx) -> literal(ctx));
        lits.put(SymbolLiteralContext.class,  (SymbolLiteralContext ctx) -> literal(ctx));
        lits.put(StringLiteralContext.class,  (StringLiteralContext ctx) -> literal(ctx));
        lits.put(DecimalLiteralContext.class, (DecimalLiteralContext ctx) -> literal(ctx));

        elements.put(LiteralValueContext.class,  (LiteralValueContext ctx) -> value(ctx));
        elements.put(VariableValueContext.class, (VariableValueContext ctx) -> value(ctx));

        terms.put(TermContext.class, (TermContext ctx) -> value(ctx));
        terms.put(BlockContext.class, (BlockContext ctx) -> value(ctx));
        terms.put(LitValueContext.class, (LitValueContext ctx) -> value(ctx));
        terms.put(TypeNameContext.class, (TypeNameContext ctx) -> value(ctx));
        terms.put(VariableContext.class, (VariableContext ctx) -> value(ctx));

        names.put(LocalValueContext.class, (LocalValueContext ctx) -> name(ctx));
        names.put(GlobalValueContext.class, (GlobalValueContext ctx) -> name(ctx));
        selfs.put(SelfSelfishContext.class,  (SelfSelfishContext ctx) -> literal(ctx));
        selfs.put(SuperSelfishContext.class, (SuperSelfishContext ctx) -> literal(ctx));

        signs.put(KeywordSigContext.class, (KeywordSigContext ctx) -> sign(ctx));
        signs.put(BinarySigContext.class, (BinarySigContext ctx) -> sign(ctx));
        signs.put(UnarySigContext.class, (UnarySigContext ctx) -> sign(ctx));

        messages.put(KeywordSelectionContext.class, (KeywordSelectionContext ctx) -> message(ctx));
        messages.put(BinarySelectionContext.class, (BinarySelectionContext ctx) -> message(ctx));
        messages.put(UnarySelectionContext.class, (UnarySelectionContext ctx) -> message(ctx));
    }

} // SmalltalkFileListener
