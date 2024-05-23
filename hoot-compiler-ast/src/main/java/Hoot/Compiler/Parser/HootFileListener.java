package Hoot.Compiler.Parser;

import java.util.*;
import java.util.function.*;
import Hoot.Runtime.Notes.*;
import Hoot.Runtime.Names.*;
import Hoot.Runtime.Values.*;
import Hoot.Runtime.Behaviors.Scope;

import Hoot.Compiler.Notes.*;
import Hoot.Compiler.Scopes.*;
import Hoot.Compiler.Scopes.File;
import Hoot.Compiler.Scopes.Block;
import Hoot.Compiler.Expressions.*;
import Hoot.Compiler.Constants.*;
import Hoot.Runtime.Faces.Logging;
import static Hoot.Runtime.Functions.Utils.*;
import static Hoot.Compiler.Parser.HootParser.*;

/**
 * Implements AST walking for the compiler.
 * @author Nik Boyd <nik.boyd@educery.dev>
 */
public class HootFileListener extends HootBaseListener implements Logging {

    // facial methods

    void defineType()  { Face.currentFace().definesType(true); }
    void defineClass() { Face.currentFace().definesType(false); }

    @Override public void exitEof(EofContext ctx) {
        Face.currentFace().popScope();
        File.currentFile().popScope();
        report("===== EOF ====="); 
    }

    // file scopes

    @Override public void enterTypeSign(TypeSignContext ctx)   { defineType(); }
    @Override public void enterClassSign(ClassSignContext ctx) { defineClass(); }
    @Override public void exitFileImport(FileImportContext ctx) { importFace(ctx); }
    void importFace(FileImportContext ctx) { File.currentFile().importFace(imported(ctx)); }
    Import imported(FileImportContext ctx) { return importSpec(ctx).withLowerCase(hasOne(ctx.c)); }
    Import importSpec(FileImportContext ctx) { return Import.from(value(ctx.g), ctx.m.getText()); }

    // types + classes

    @Override public void exitTypeSign(TypeSignContext ctx) { faceNotes(ctx); signFace(ctx); }
    void signFace(TypeSignContext ctx) { 
        Face.currentFace().signature(sign(ctx)); Face.currentFace().reportSigned(); }
    TypeSignature sign(TypeSignContext ctx) {
        return TypeSignature.with(types(ctx), type(ctx),
            notes(ctx), keyword(ctx), comment(ctx)); }

    // make class sig extend type sig
    @Override public void exitClassSign(ClassSignContext ctx) { faceNotes(ctx); signFace(ctx); }
    void signFace(ClassSignContext ctx) { 
        Face.currentFace().signature(sign(ctx)); Face.currentFace().reportSigned(); }
    ClassSignature sign(ClassSignContext ctx) {
        return ClassSignature.with(superType(ctx), type(ctx), types(ctx),
            notes(ctx), keyword(ctx), comment(ctx)); }

    Hoot.Compiler.Scopes.Face faceScope() { return File.currentFile().faceScope(); }
    @Override public void exitSubclassKeyword(SubclassKeywordContext ctx) { faceScope().makeCurrent(); }
    @Override public void exitSubtypeKeyword(SubtypeKeywordContext ctx)   { faceScope().makeCurrent(); }

    @Override public void exitProtocolScope(ProtocolScopeContext ctx) { report("exited protocol"); }
    @Override public void exitProtocolSign(ProtocolSignContext ctx) { report("entered protocol"); faceProto(ctx); }
    void faceProto(ProtocolSignContext ctx) { Face.currentFace().selectFace(metaProto(ctx)); }
    String metaProto(ProtocolSignContext ctx) { return hasOne(ctx.s)? ctx.s.getText(): ""; }

    // methods + blocks

    // create a fake method in which a variable gets defined, then transfer it into the class
    // so that value expressions and their parts have a block context for reference
    @Override public void enterMemberSlot(MemberSlotContext ctx) { }
    @Override public void exitMemberSlot(MemberSlotContext ctx) { faceSlot(ctx); }
    void faceSlot(MemberSlotContext ctx) { Face.currentFace().addLocal(member(ctx.v)); }

    @Override public void enterMethodMember(MethodMemberContext ctx) {  }
    @Override public void exitMethodMember(MethodMemberContext ctx) { faceMethod(ctx); }
    void faceMethod(MethodMemberContext ctx) {
        Method m = methodScope(ctx.m);
        Face.currentFace().addMethod(m);
        m.popScope();
//        m.report(m.description()+" exit");
    }

    Method methodScope(MethodScopeContext ctx) {
        Method m = new Method(Face.currentFace()).makeCurrent();
        m.notes().noteAll(notes(ctx));
        m.signature(sign(ctx));
        m.content(blockFill(ctx));
        m.construct(value(ctx));
//        m.popScope();
        m.reportScope();
        return m;
    }

    Nest nest(BlockContext ctx) { return blockScope(ctx).withNest(); }
    BlockContent blockFill(MethodScopeContext ctx) { return blockFill(ctx.content); }
    BlockContent blockFill(BlockScopeContext ctx)  { return blockFill(ctx.content); }
    BlockContent blockFill(BlockFillContext ctx) { return BlockContent.with(evals(ctx.s), value(ctx.r), ctx.p.size()); }

    Block blockScope(BlockContext ctx) {
        Block b = new Block().makeCurrent();
        b.signature(sign(ctx.b.sign));
        b.signature().defineLocals();
        b.content(blockFill(ctx.b));
//        b.popScope();
        b.report(b.nestLevel()+" level");
        return b; }

    // names

    String name(GlobalValueContext ctx) { return name(ctx.g); }
    String name(GlobalNameContext ctx) { return ctx.g.getText(); }
    String name(LocalValueContext ctx) { return name(ctx.v); }
    String name(LocalNameContext ctx) { return ctx.v.getText(); }
    String name(ValueNameContext ctx) { return applyMatched(names, ctx); }

    // signatures

    BasicSignature   sign(MethodScopeContext ctx) { return sign(ctx.sign); }
    BasicSignature   sign(MethodSignContext ctx)  { return applyMatched(signs, ctx); }
    KeywordSignature sign(BlockSignContext ctx)   { return KeywordSignature.with(note(ctx.type), args(ctx.args)); }
    KeywordSignature sign(KeywordSignContext ctx) { return KeywordSignature.with(note(ctx.result), args(ctx.name.args), keyword(ctx)); }
    BinarySignature  sign(BinarySignContext ctx)  { return BinarySignature.with(note(ctx.result), args(ctx.args), message(ctx.name)); }
    UnarySignature   sign(UnarySignContext ctx)   { return UnarySignature.with(note(ctx.result), selector(ctx.name)); }
    UnarySignature   sign(UnarySigContext ctx)    { return sign(ctx.us); }
    BinarySignature  sign(BinarySigContext ctx)   { return sign(ctx.bs); }
    KeywordSignature sign(KeywordSigContext ctx)  { return sign(ctx.ks); }

    // messages

    List<Variable> args(List<ArgumentContext> ns) { return map(ns, n -> value(n)); }
    Keyword keyword(KeywordMessageContext ctx) { return Keyword.with(heads(ctx.kh), tails(ctx.kt)); }
    Keyword keyword(KeywordSignContext ctx) { return Keyword.with(heads(ctx.name.kh), tails(ctx.name.kt)); }
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

    // values

    Primary value(TermContext ctx)     { return Primary.with(value(ctx.n)); }
    Primary value(LitValueContext ctx) { return Primary.with(literal(ctx.l)); }
    Primary value(TypeNameContext ctx) { return Primary.with(global(ctx)); }
    Primary value(VariableContext ctx) { return Primary.with(literal(ctx)); }
    Primary value(PrimaryContext ctx)  { return applyMatched(terms, ctx); }
    Primary value(BlockContext ctx)    {
        Nest n = nest(ctx);
        try {
            return Primary.with(n);
        }
        finally {
            n.blockScope().report(n.blockScope()+" exit");
//            n.blockScope().popScope();
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

    List<Statement> evals(List<StatementContext> ss) { return mapList(ss, s -> hasOne(s), s -> value(s)); }
    Statement value(StatementContext ctx) { return hasNone(ctx)? null: say(ctx); }
    Statement say(StatementContext ctx) { return Statement.with(hasOne(ctx.v)? value(ctx.v): value(ctx.n)); }

    Construct value(MethodScopeContext ctx) { return value(ctx.c); }
    Construct value(ConstructContext ctx) { return hasNone(ctx)? null: say(ctx); }
    Construct say(ConstructContext ctx) { return Construct.with(literal(ctx.ref), values(ctx.terms)); }

    Variable part(MemberVarContext ctx)  { return Variable.memberNamed(name(ctx.v), note(ctx.type)); }
    Variable part(ArgumentContext ctx)   { return Variable.argNamed(name(ctx.v), note(ctx.type)); }
    Variable part(AssignmentContext ctx) { return Variable.named(name(ctx.v), note(ctx.type), value(ctx.value)); }

    Variable value(ArgumentContext ctx)   { return part(ctx).withNotes(notes(ctx.n)); }
    Variable value(AssignmentContext ctx) { return part(ctx).withNotes(notes(ctx.n)).makeAssignment(); }

    Variable member(MemberVarContext ctx) { return slot(ctx).defineMember(); }
    Variable slot(MemberVarContext ctx) { return part(ctx).withValue(value(ctx.value)).withNotes(notes(ctx.n)); }

    Global global(TypeNameContext ctx) { return value(ctx.g).makePrimary(); }
    Global value(GlobalReferContext ctx) { return Global.withList(map(ctx.names, n -> name(n))); }
    Constant value(LiteralValueContext ctx) { return literal(ctx.lit); }
    Constant value(ElementValueContext ctx) { return applyMatched(elements, ctx); }

    LiteralArray literal(PrimArrayContext ctx) { return values(ctx.array); }
    LiteralArray values(PrimitiveValuesContext ctx) { return LiteralArray.withItems(prims(ctx.array)); }
    List<Constant> prims(List<PrimitiveContext> vs) { return map(vs, v -> literal(v)); }

    LiteralArray literal(ArrayLiteralContext ctx) { return values(ctx.array); }
    LiteralArray values(ElementValuesContext ctx) { return LiteralArray.withItems(cons(ctx.array)); }
    List<Constant> cons(List<ElementValueContext> vs) { return map(vs, v -> value(v)); }

    // notes

    TypeList types(ClassSignContext ctx) { return TypeList.withDetails(types(ctx.ts.types)); }
    TypeList types(TypeSignContext ctx)  { return TypeList.withDetails(types(ctx.h.superTypes)); }
    TypeList types(GenericArgsContext ctx) { return TypeList.withDetails(types(ctx.types)); }
    TypeList types(DetailedSignContext ctx) { return hasNone(ctx)? new TypeList(): notes(ctx.generics); }
    TypeList notes(GenericArgsContext ctx) { return hasNone(ctx)? new TypeList(): types(ctx); }
    TypeList notes(DetailedSignContext ctx) { return types(ctx).withExit(type(ctx.exit)); }

    DetailedType type(ExtentItemContext ctx) { return note(ctx.extent); }
    DetailedType type(SignedItemContext ctx) { return note(ctx.signed); }
    DetailedType note(TypeNoteContext ctx) { return !hasType(ctx)? null: type(ctx); }
    DetailedType type(TypeNoteContext ctx) { return type(ctx.type).makeArrayed(hasOne(ctx.etc)); }
    boolean hasType(TypeNoteContext ctx) { return hasOne(ctx) && hasOne(ctx.type); }

    List<DetailedType> types(List<DetailedTypeContext> cs) { return map(cs, c -> type(c)); }
    DetailedType type(DetailedTypeContext ctx) { return applyMatched(details, ctx); }

    Global subName(TypeSignContext ctx) { return Global.named(name(ctx.sub)); }
    Global subName(ClassSignContext ctx) { return Global.named(name(ctx.sub)); }
    DetailedType superType(ClassSignContext ctx) { return note(ctx.h.superClass); }
    DetailedType type(TypeSignContext ctx) { return DetailedType.with(subName(ctx), notes(ctx.ds)); }
    DetailedType type(ClassSignContext ctx) { return DetailedType.with(subName(ctx), notes(ctx.ds)); }
    DetailedType note(SignedTypeContext ctx) { return hasNone(ctx)? null: type(ctx); }
    DetailedType note(ExtendTypeContext ctx) { return type(ctx).makeExtensive(); }
    DetailedType type(SignedTypeContext ctx) { return DetailedType.with(value(ctx.g), notes(ctx.ds)); }
    DetailedType type(ExtendTypeContext ctx) { return DetailedType.with(Global.with(name(ctx.g)), type(ctx.baseType)); }

    List<Note> notes(CompilationUnitContext ctx) { return notes(ctx.n); }
    List<Note> notes(MethodScopeContext ctx) { return notes(ctx.n); }
    List<Note> notes(NotationsContext ctx) { return map(ctx.notes, n -> note(n)); }

    void faceNotes(TypeSignContext ctx) { Face.currentFace().notes().noteAll(notes(unit(ctx))); }
    void faceNotes(ClassSignContext ctx) { Face.currentFace().notes().noteAll(notes(unit(ctx))); }
    CompilationUnitContext unit(TypeSignContext ctx)  { return (CompilationUnitContext)ctx.getParent().getParent(); }
    CompilationUnitContext unit(ClassSignContext ctx) { return (CompilationUnitContext)ctx.getParent().getParent(); }

    NoteList notes(TypeSignContext ctx) { return new NoteList().noteAll(notes(ctx.n)); }
    NoteList notes(ClassSignContext ctx) { return new NoteList().noteAll(notes(ctx.n)); }
    KeywordNote note(NotationContext ctx) { return KeywordNote.with(name(ctx.name), nakeds(ctx.nakeds), nameds(ctx.values)); }

    List<NamedValue> nakeds(List<NakedValueContext> cs) { return map(cs, c -> note(c)); }
    NamedValue note(NakedPrimContext ctx) { return NamedValue.with(Empty, literal(ctx.v)); }
    NamedValue note(NakedGlobalContext ctx) { return NamedValue.with(Empty, Global.with(name(ctx.g))); }
    NamedValue note(NakedValueContext ctx) { return applyMatched(nakeds, ctx); }

    List<NamedValue> nameds(List<NamedValueContext> cs) { return map(cs, c -> note(c)); }
    NamedValue note(NamedPrimContext ctx) { return NamedValue.with(ctx.head.getText(), literal(ctx.v)); }
    NamedValue note(NamedGlobalContext ctx) { return NamedValue.with(ctx.head.getText(), Global.with(name(ctx.g))); }
    NamedValue note(NamedValueContext ctx) { return applyMatched(nameds, ctx); }

    // literals

    String keyword(TypeSignContext ctx) { return ctx.k.getText(); }
    String keyword(ClassSignContext ctx) { return ctx.k.getText(); }
    String comment(TypeSignContext ctx) { return Comment.findComment(ctx.h.getStart(), ctx.p); }
    String comment(ClassSignContext ctx) { return Comment.findComment(ctx.h.getStart(), ctx.p); }

    Constant literal(SelfishContext ctx) { return applyMatched(selfs, ctx); }
    Constant literal(LiteralContext ctx) { return applyMatched(lits, ctx); }
    Constant literal(PrimitiveContext ctx) { return applyMatched(prims, ctx); }

    LiteralName literal(VariableContext ctx) { return LiteralName.with(name(ctx.v)); }
    LiteralName literal(SelfSelfishContext ctx) { return LiteralName.with(ctx.refSelf.getText(), ctx.start.getLine()); }
    LiteralName literal(SuperSelfishContext ctx) { return LiteralName.with(ctx.refSuper.getText(), ctx.start.getLine()); }
    LiteralName literal(SelfLiteralContext ctx) { return LiteralName.with(ctx.refSelf.getText(), ctx.start.getLine()); }
    LiteralName literal(SuperLiteralContext ctx) { return LiteralName.with(ctx.refSuper.getText(), ctx.start.getLine()); }

    LiteralNil literal(NilLiteralContext ctx) { return LiteralNil.with(ctx.refNil.getText(), ctx.start.getLine()); }
    LiteralRadical literal(NumLiteralContext ctx) { return LiteralRadical.with(ctx.n.getText(), ctx.start.getLine()); }
    LiteralDecimal literal(DecimalLiteralContext ctx) { return LiteralDecimal.with(ctx.value.getText(), ctx.start.getLine()); }

    LiteralCharacter literal(PrimCharContext ctx) { return LiteralCharacter.with(ctx.value.getText(), ctx.start.getLine()); }
    LiteralCharacter literal(CharLiteralContext ctx) { return LiteralCharacter.with(ctx.value.getText(), ctx.start.getLine()); }
    LiteralBoolean literal(PrimBoolContext ctx) { return LiteralBoolean.with(ctx.bool.getText(), ctx.start.getLine()); }
    LiteralBoolean literal(BoolLiteralContext ctx) { return LiteralBoolean.with(ctx.bool.getText(), ctx.start.getLine()); }

    LiteralFloat literal(PrimFloatContext ctx) { return LiteralFloat.with(ctx.value.getText(), ctx.start.getLine()); }
    LiteralFloat literal(FloatLiteralContext ctx) { return LiteralFloat.with(ctx.value.getText(), ctx.start.getLine()); }
    LiteralInteger literal(PrimIntContext ctx) { return LiteralInteger.with(ctx.value.getText(), ctx.start.getLine()); }
    LiteralInteger literal(IntLiteralContext ctx) { return LiteralInteger.with(ctx.value.getText(), ctx.start.getLine()); }

    LiteralSymbol literal(PrimSymbolContext ctx) { return LiteralSymbol.with(ctx.value.getText(), ctx.start.getLine()); }
    LiteralSymbol literal(SymbolLiteralContext ctx) { return LiteralSymbol.with(ctx.value.getText(), ctx.start.getLine()); }
    LiteralString literal(PrimStringContext ctx) { return LiteralString.with(ctx.value.getText(), ctx.start.getLine()); }
    LiteralString literal(StringLiteralContext ctx) { return LiteralString.with(ctx.value.getText(), ctx.start.getLine()); }

    <T,R> R apply(Function f, T it) { return (R)f.apply(it); } // apply function for matched item class
    <B, T extends B, R> R applyMatched(HashMap<Class, Function<? extends B,R>> m, B it) { if (hasNone(it)) return null;
        for (Class<T> c : m.keySet()) if (c.isInstance(it)) return apply(m.get(c), c.cast(it)); return null; }

    final HashMap<Class, Function<? extends ValueNameContext, String>> names = new HashMap<>();
    final HashMap<Class, Function<? extends MethodSignContext, BasicSignature>> signs = new HashMap<>();
    final HashMap<Class, Function<? extends MessageContext, Message>> messages = new HashMap<>();
    final HashMap<Class, Function<? extends PrimaryContext, Primary>> terms = new HashMap<>();
    final HashMap<Class, Function<? extends ElementValueContext, Constant>> elements = new HashMap<>();
    final HashMap<Class, Function<? extends DetailedTypeContext, DetailedType>> details = new HashMap<>();
    final HashMap<Class, Function<? extends NakedValueContext, NamedValue>> nakeds = new HashMap<>();
    final HashMap<Class, Function<? extends NamedValueContext, NamedValue>> nameds = new HashMap<>();
    final HashMap<Class, Function<? extends PrimitiveContext, Constant>> prims = new HashMap<>();
    final HashMap<Class, Function<? extends SelfishContext, Constant>> selfs = new HashMap<>();
    final HashMap<Class, Function<? extends LiteralContext, Constant>> lits = new HashMap<>();
    public HootFileListener() {
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

        prims.put(PrimBoolContext.class, (PrimBoolContext ctx) -> literal(ctx));
        prims.put(PrimCharContext.class, (PrimCharContext ctx) -> literal(ctx));
        prims.put(PrimIntContext.class,  (PrimIntContext ctx) -> literal(ctx));
        prims.put(PrimFloatContext.class, (PrimFloatContext ctx) -> literal(ctx));
        prims.put(PrimSymbolContext.class, (PrimSymbolContext ctx) -> literal(ctx));
        prims.put(PrimStringContext.class, (PrimStringContext ctx) -> literal(ctx));
        prims.put(PrimArrayContext.class,  (PrimArrayContext ctx) -> literal(ctx));

        selfs.put(SelfSelfishContext.class,  (SelfSelfishContext ctx) -> literal(ctx));
        selfs.put(SuperSelfishContext.class, (SuperSelfishContext ctx) -> literal(ctx));
        nameds.put(NamedPrimContext.class,   (NamedPrimContext ctx) -> note(ctx));
        nameds.put(NamedGlobalContext.class, (NamedGlobalContext ctx) -> note(ctx));
        nakeds.put(NakedPrimContext.class,   (NakedPrimContext ctx) -> note(ctx));
        nakeds.put(NakedGlobalContext.class, (NakedGlobalContext ctx) -> note(ctx));
        details.put(ExtentItemContext.class, (ExtentItemContext ctx) -> type(ctx));
        details.put(SignedItemContext.class, (SignedItemContext ctx) -> type(ctx));
        elements.put(LiteralValueContext.class,  (LiteralValueContext ctx) -> value(ctx));
        elements.put(VariableValueContext.class, (VariableValueContext ctx) -> value(ctx));

        terms.put(TermContext.class, (TermContext ctx) -> value(ctx));
        terms.put(BlockContext.class, (BlockContext ctx) -> value(ctx));
        terms.put(LitValueContext.class, (LitValueContext ctx) -> value(ctx));
        terms.put(TypeNameContext.class, (TypeNameContext ctx) -> value(ctx));
        terms.put(VariableContext.class, (VariableContext ctx) -> value(ctx));

        names.put(LocalValueContext.class, (LocalValueContext ctx) -> name(ctx));
        names.put(GlobalValueContext.class, (GlobalValueContext ctx) -> name(ctx));
        signs.put(KeywordSigContext.class, (KeywordSigContext ctx) -> sign(ctx));
        signs.put(BinarySigContext.class, (BinarySigContext ctx) -> sign(ctx));
        signs.put(UnarySigContext.class, (UnarySigContext ctx) -> sign(ctx));
        messages.put(KeywordSelectionContext.class, (KeywordSelectionContext ctx) -> message(ctx));
        messages.put(BinarySelectionContext.class, (BinarySelectionContext ctx) -> message(ctx));
        messages.put(UnarySelectionContext.class, (UnarySelectionContext ctx) -> message(ctx));
    }

} // HootFileListener
