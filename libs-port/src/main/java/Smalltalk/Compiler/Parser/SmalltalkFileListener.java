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
import Hoot.Runtime.Notes.DetailedType;
import static Hoot.Runtime.Functions.Utils.*;
import Hoot.Runtime.Notes.Note;
import static Smalltalk.Compiler.Parser.SmalltalkParser.*;

/**
 * Implements AST walking for the compiler.
 * @author Nik Boyd <nik.boyd@educery.dev>
 */
public class SmalltalkFileListener extends SmalltalkBaseListener implements Logging {

    File fileScope() { return File.currentFile(); }
    Face faceScope() { return fileScope().faceScope(); }
    Face activeFace() { return fileScope().activeFace(); }

    static final String Quoted = "'%s'";
    String quoted(String text) { return String.format(Quoted, text); }
    String unquote(LiteralString s) { return s.unquotedValue(); }
    String classComment(FiledHeaderContext ctx) { return hasNo(ctx.ch)? "": ctx.ch.cc.hc.getText(); }
    String classComment(ClassHeaderContext ctx) { return hasNo(ctx.cc)? "": ctx.cc.cc.getText(); }
    @Override public void exitFiledHeader(FiledHeaderContext ctx) { faceScope().comment(classComment(ctx)); }
    @Override public void exitClassHeader(ClassHeaderContext ctx) { faceScope().comment(classComment(ctx)); }

    // Global subclass: Symbol instanceVariableNames: String 
    //      classVariableNames: String poolDictionaries: String category: String
    @Override public void exitClassSignature(ClassSignatureContext ctx) { signFace(ctx, message(ctx.x.kmsg)); }
    void signFace(ClassSignatureContext ctx, KeywordMessage m) {
        faceScope().signature(signClass(ctx, m)); for (String v : memberVars(m)) makeMember(v); }

    String[] memberVars(KeywordMessage m) { return unquote(primaryString(term(1, m))).split(" "); }
    Variable makeMember(String v) { return Variable.memberNamed(v, DetailedType.RootType).defineMember(); }
    ClassSignature signClass(ClassSignatureContext ctx, KeywordMessage m) {
        return ClassSignature.with(superType(ctx), subType(m), m.methodName()); }
    
    // Global methodsFor: String stamp: String
    static final String ProtoForm = "protocol: '%s'";
    static final String StampForm = "stamp: '%s'";
    @Override public void exitProtoHeader(ProtoHeaderContext ctx) {
        Formula f = value(ctx.p.f);
        Global classGlobal = globalFrom(f);
        if (hasSome(classGlobal)) {
            String meta = hasSelectors(f)? f.primaryTerm().firstSelector(): "";
            ProtocolScope s = ProtocolScope.with(classGlobal.name(), meta);
            faceScope().addScope(s);
            KeywordMessage m = message(ctx.p.kmsg);
            String proto = unquote(primaryString(term(0, m)));
            s.note(Note.with(String.format(ProtoForm, proto)));
            if (m.formulas().size() > 1) {
                String stamp = unquote(primaryString(term(1, m)));
                s.note(Note.with(String.format(StampForm, stamp)));
            }
        }
    }
    
    void addMethod(Method m) { activeFace().addMethod(m); }
    @Override public void exitMethodReader(MethodReaderContext ctx) { addMethod(methodScope(ctx.ms)); }

    Method methodScope(MethodScopeContext ctx) {
        Method m = new Method().makeCurrent(); 
        m.signature(sign(ctx, m)); 
        m.content(blockFill(ctx)); return m; }
//        return Method.with(sign(ctx), blockFill(ctx)).acquireStatements(); }

    Block  blockScope(BlockContext ctx) {
        Block b = new Block().makeCurrent();
        b.signature(sign(ctx.b.sign, b)); 
        b.signature().defineLocals();
        b.content(blockFill(ctx.b));
        return b.acquireStatements(); }
//        return Block.with(sign(ctx.b.sign, b), blockFill(ctx.b)).acquireStatements(); }

    Global subType(KeywordMessage m) { return globalFrom(primarySymbol(term(0, m))); }
    Global superType(ClassSignatureContext ctx) { return globalFrom(value(ctx.x.f)); }
    Global globalFrom(Formula f) { return hasTermPrime(f)? primaryGlobal(f): null; }
    boolean hasSelectors(Formula f) { return hasOne(f) && f.primaryTerm().hasSelectors(); }

    Primary termPrime(Formula f) { return f.primaryTerm().primary(); }
    boolean hasTermPrime(Formula f) { return f.primaryTerm().hasPrimary(); }
    Global globalFrom(LiteralSymbol s) { return Global.named(s.encodedValue()); }
    Global primaryGlobal(Formula f) { return termPrime(f).asGlobal(); }
    LiteralSymbol primarySymbol(Formula f) { return termPrime(f).asSymbol(); }
    LiteralString primaryString(Formula f) { return termPrime(f).asString(); }

    Nest nest(BlockContext ctx) { return blockScope(ctx).withNest(); }
    BlockContent blockFill(MethodScopeContext ctx) { return blockFill(ctx.content); }
    public BlockContent blockFill(BlockScopeContext ctx)  { return blockFill(ctx.content); }
    BlockContent blockFill(BlockFillContext ctx) { return makeBlock(evals(ctx.s), value(ctx.r), ctx.p.size()); }
    BlockContent makeBlock(List<Statement> ss, Expression ex, int ps) { return BlockContent.with(ss, ex, ps); }

    List<Variable> args(List<ArgumentContext> ns) { return map(ns, n -> value(n)); }
    Keyword keyword(KeywordMessageContext ctx) { return Keyword.with(heads(ctx.kh)); }
    Keyword keyword(KeywordSignContext ctx) { return Keyword.with(heads(ctx.name.kh)); }
    List<String> heads(List<KeywordHeadContext> cs) { return map(cs, c -> c.getText()); }
//    List<String> tails(List<KeywordTailContext> cs) { return map(cs, c -> c.getText()); }

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
    Primary value(TypeNameContext ctx) { return Primary.with(global(ctx)); }
    Primary value(PrimaryContext ctx)  { return applyMatched(terms, ctx); }
    Primary value(BlockContext ctx)    { try { return Primary.with(nest(ctx)); } finally { Scope.popBlockScope(); }}
    
    List<Formula> values(List<FormulaContext> fs) { return map(fs, f -> value(f)); }
    Formula value(FormulaContext ctx) { return Formula.with(message(ctx.s), messages(ctx.ops)); }
    Formula term(BinaryMessageContext ctx) { return Formula.with(message(ctx.term)); }
    Formula term(int x, KeywordMessage m) { return m.formulas().get(x); }

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

    BasicSignature   sign(MethodScopeContext ctx, Method m) { return sign(ctx.sign, m); }
    BasicSignature   sign(MethodSignContext ctx, Method m)  { return applyMatched(signs, ctx, m); }
    KeywordSignature sign(BlockSignContext ctx, Block b)    { return KeywordSignature.with(b, null, args(ctx.args)); }

    KeywordSignature sign(KeywordSignContext ctx, Method m) { return KeywordSignature.with(m, null, args(ctx.name.args), keyword(ctx)); }
    BinarySignature  sign(BinarySignContext ctx, Method m)  { return BinarySignature.with(m, null, args(ctx.args), message(ctx.name)); }
    UnarySignature   sign(UnarySignContext ctx, Method m)   { return UnarySignature.with(m, null, selector(ctx.name)); }

    UnarySignature   sign(UnarySigContext ctx, Method m)    { return sign(ctx.us, m); }
    BinarySignature  sign(BinarySigContext ctx, Method m)   { return sign(ctx.bs, m); }
    KeywordSignature sign(KeywordSigContext ctx, Method m)  { return sign(ctx.ks, m); }

    List<Statement> evals(List<StatementContext> ss) { return mapList(ss, s -> hasOne(s), s -> value(s)); }
    Statement value(StatementContext ctx) { return hasNone(ctx)? null: say(ctx); }
    Statement say(StatementContext ctx) { return Statement.with(hasOne(ctx.v)? value(ctx.v): value(ctx.n)); }

    Variable part(ArgumentContext ctx)   { return Variable.argNamed(name(ctx.v), null); }
    Variable part(AssignmentContext ctx) { return Variable.named(name(ctx.v), null, value(ctx.value)); }

    Variable value(ArgumentContext ctx)   { return part(ctx); }
    Variable value(AssignmentContext ctx) { return part(ctx).makeAssignment(); }

    Global global(TypeNameContext ctx) { return value(ctx.g).makePrimary(); }
    Global value(GlobalReferContext ctx) { return Global.withList(map(ctx.names, n -> name(n))); }
    Constant value(LiteralValueContext ctx) { return literal(ctx.lit); }
    Constant value(ElementValueContext ctx) { return applyMatched(elements, ctx); }

    LiteralArray literal(ArrayLiteralContext ctx) { return values(ctx.array); }
    LiteralArray values(ElementValuesContext ctx) { return LiteralArray.withItems(cons(ctx.array)); }
    List<Constant> cons(List<ElementValueContext> vs) { return map(vs, v -> value(v)); }

//    Constant literal(SelfishContext ctx) { return applyMatched(selfs, ctx); }
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
    @SuppressWarnings("unchecked") <T,R> R apply(BiFunction f, T it, Method m) { return (R)f.apply(it, m); }
    @SuppressWarnings("unchecked") 
    <B, T extends B, R> R applyMatched(HashMap<Class, BiFunction<? extends B,Method,R>> m, B it, Method x) {
        if (hasNone(it)) return null;
        for (Class c : m.keySet()) {
            if (c.isInstance(it))
                return apply(m.get(c), c.cast(it), x);
        }
        report(it.getClass().getSimpleName()+" not found");
        return null; }

    @SuppressWarnings("unchecked") 
    <B, T extends B, R> R applyMatched(HashMap<Class, Function<? extends B,R>> m, B it) {
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
//    final HashMap<Class, Function<? extends MethodSignContext, BasicSignature>> signs = new HashMap<>();
    final HashMap<Class, BiFunction<? extends MethodSignContext, Method, BasicSignature>> signs = new HashMap<>();
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

        signs.put(KeywordSigContext.class, (KeywordSigContext ctx, Method m) -> sign(ctx, m));
        signs.put(BinarySigContext.class, (BinarySigContext ctx, Method m) -> sign(ctx, m));
        signs.put(UnarySigContext.class, (UnarySigContext ctx, Method m) -> sign(ctx, m));

        messages.put(KeywordSelectionContext.class, (KeywordSelectionContext ctx) -> message(ctx));
        messages.put(BinarySelectionContext.class, (BinarySelectionContext ctx) -> message(ctx));
        messages.put(UnarySelectionContext.class, (UnarySelectionContext ctx) -> message(ctx));
    }

} // SmalltalkFileListener
