package Hoot.Compiler.Parser;

import java.util.*;
import Hoot.Runtime.Notes.*;
import Hoot.Runtime.Names.*;
import Hoot.Runtime.Values.*;

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

    // file scopes

    @Override public void exitFileImport(FileImportContext ctx) { File.currentFile().importFace(imported(ctx)); }
    Import importSpec(FileImportContext ctx) { return Import.from(value(ctx.g), ctx.m.getText()); }
    Import imported(FileImportContext ctx) { return importSpec(ctx).withLowerCase(hasOne(ctx.c)); }

    @Override public void enterClassSign(ClassSignContext ctx) {
        Face.currentFace().definesType(false); }

    @Override public void enterTypeSign(TypeSignContext ctx) {
        Face.currentFace().definesType(true); }
    
    // types + classes

    @Override public void exitTypeSign(TypeSignContext ctx) {
        Face.currentFace().notes().noteAll(notes(unit(ctx)));
        Face.currentFace().signature(sign(ctx));
    }
    
    @Override public void exitClassSign(ClassSignContext ctx) {
        Face.currentFace().notes().noteAll(notes(unit(ctx)));
        Face.currentFace().signature(sign(ctx));
    }

    CompilationUnitContext unit(TypeSignContext ctx) { return (CompilationUnitContext)ctx.getParent().getParent(); }
    TypeSignature sign(TypeSignContext ctx) {
        return TypeSignature.with(superNotes(ctx), subNote(ctx), 
                           notes(ctx), keyword(ctx), comment(ctx)); }

    CompilationUnitContext unit(ClassSignContext ctx) { return (CompilationUnitContext)ctx.getParent().getParent(); }
    ClassSignature sign(ClassSignContext ctx) {
        return ClassSignature.with(superNote(ctx), subNote(ctx), 
            subNotes(ctx), notes(ctx), keyword(ctx), comment(ctx)); }

    Hoot.Compiler.Scopes.Face faceScope() { return File.currentFile().faceScope(); }
    @Override public void exitSubclassKeyword(SubclassKeywordContext ctx) { faceScope().makeCurrent(); }
    @Override public void exitSubtypeKeyword(SubtypeKeywordContext ctx)   { faceScope().makeCurrent(); }

    String metaProto(ProtocolSignContext ctx) { return hasOne(ctx.s)? ctx.s.getText(): ""; }
    @Override public void exitProtocolSign(ProtocolSignContext ctx) {
        Face.currentFace().selectFace(metaProto(ctx)); }
    
    @Override public void exitProtocolScope(ProtocolScopeContext ctx) {}

    @Override public void exitVarMember(VarMemberContext ctx) {
        Face.currentFace().addLocal(value(ctx)); }
    @Override public void exitMethodMember(MethodMemberContext ctx) {
        Face.currentFace().addMethod(methodScope(ctx.m)); }
    
    // methods + blocks
    
    Method methodScope(MethodScopeContext ctx) {
        Method m = new Method(Face.currentFace()).makeCurrent();
        m.notes().noteAll(notes(ctx));
        m.signature(sign(ctx));
        m.content(block(ctx));
        m.construct(value(ctx));
        m.popScope();
        return m;
    }

    Nest blockNest(BlockContext ctx) { return blockScope(ctx).withNest(); }
    BlockContent block(MethodScopeContext ctx) { return block(ctx.content); }
    BlockContent block(BlockScopeContext ctx)  { return block(ctx.content); }
    BlockContent block(BlockFillContext ctx) {
        return BlockContent.with(evals(ctx.s), value(ctx.r), ctx.p.size()); }

    Block blockScope(BlockContext ctx) {
        Block b = new Block().makeCurrent();
        b.signature(sign(ctx.b.sign));
        b.signature().defineLocals();
        b.content(block(ctx.b));
        b.popScope();
        return b; }
    
    // names

    String name(GlobalValueContext ctx) { return name(ctx.g); }
    String name(GlobalNameContext ctx) { return ctx.g.getText(); }
    String name(VarValueContext ctx) { return name(ctx.v); }
    String name(VarNameContext ctx) { return ctx.v.getText(); }
    String name(ValueNameContext ctx) {
        if (ctx instanceof VarValueContext) return name((VarValueContext)ctx);
        if (ctx instanceof GlobalValueContext) return name((GlobalValueContext)ctx);
        return null; }

    // signatures

    BasicSignature sign(MethodScopeContext ctx) { return sign(ctx.sign); }
    BasicSignature sign(MethodSignContext ctx) {
        if (ctx instanceof KeywordSigContext) return sign((KeywordSigContext)ctx);
        if (ctx instanceof BinarySigContext)  return sign((BinarySigContext)ctx);
        if (ctx instanceof UnarySigContext)   return sign((UnarySigContext)ctx);
        return null; }

    KeywordSignature sign(BlockSignContext ctx) {
        return KeywordSignature.with(note(ctx.type), args(ctx.args)); }

    KeywordSignature sign(KeywordSigContext ctx) { return sign(ctx.ks); }
    KeywordSignature sign(KeywordSignContext ctx) {
        return KeywordSignature.with(note(ctx.result), args(ctx.name.args), heads(ctx.name.kh), tails(ctx.name.kt)); }

    BinarySignature sign(BinarySigContext ctx) { return sign(ctx.bs); }
    BinarySignature sign(BinarySignContext ctx) {
        return BinarySignature.with(note(ctx.result), args(ctx.args), selector(ctx.name)); }

    UnarySignature sign(UnarySigContext ctx) { return sign(ctx.us); }
    UnarySignature sign(UnarySignContext ctx) {
        return UnarySignature.with(note(ctx.result), selector(ctx.name)); }
    
    // messages

    List<String> heads(List<KeywordHeadContext> cs) { return map(cs, c -> c.getText()); }
    List<String> tails(List<KeywordTailContext> cs) { return map(cs, c -> c.getText()); }
    List<Variable> args(List<NamedArgContext> ns) { return map(ns, n -> value(n)); }

    List<String> selectors(List<UnarySelectorContext> cs) { return map(cs, s -> selector(s)); }
    String selector(UnarySelectorContext ctx) { return Keyword.with(ctx.s.getText()).methodName(); }

    UnarySequence selector(UnarySelectionContext ctx) { return UnarySequence.with(selector(ctx.umsg)); }
    UnarySequence message(UnarySequenceContext ctx) {
        return UnarySequence.with(value(ctx.p), selectors(ctx.msgs)); }

    Operator selector(BinaryOperatorContext ctx) { return Operator.with(ctx.s.getText()); }
    List<BinaryMessage> messages(List<BinaryMessageContext> ms) { return map(ms, m -> message(m)); }

    BinaryMessage message(BinarySelectionContext ctx) { return message(ctx.bmsg);  }
    BinaryMessage message(BinaryMessageContext ctx) {
        return BinaryMessage.with(selector(ctx.operator), Formula.with(message(ctx.term))); }

    KeywordMessage message(KeywordSelectionContext ctx) { return message(ctx.kmsg); }
    KeywordMessage message(KeywordMessageContext ctx) {
        if (hasNone(ctx)) return null;
        return KeywordMessage.with(heads(ctx.kh), tails(ctx.kt), values(ctx.fs)); }

    List<Message> cascades(List<MessageCascadeContext> ms) { return map(ms, mc -> message(mc.m)); }
    Message message(MessageContext ctx) {
        if (hasNone(ctx)) return null;
        if (ctx instanceof KeywordSelectionContext) return message((KeywordSelectionContext) ctx);
        if (ctx instanceof BinarySelectionContext)  return message((BinarySelectionContext)ctx);
        if (ctx instanceof UnarySelectionContext)   return selector((UnarySelectionContext)ctx);
        return null;
    }
    
    // values

    Primary value(TermContext ctx)     { return Primary.with(term(ctx)); }
    Primary value(BlockContext ctx)    { return Primary.with(blockNest(ctx)); }
    Primary value(LitValueContext ctx) { return Primary.with(literal(ctx)); }
    Primary value(TypeNameContext ctx) { return Primary.with(global(ctx)); }
    Primary value(VariableContext ctx) { return Primary.with(literal(ctx)); }
    Primary value(PrimaryContext ctx) {
        if (hasNone(ctx)) return null;
        if (ctx instanceof TermContext)     return value((TermContext) ctx);
        if (ctx instanceof BlockContext)    return value((BlockContext) ctx);
        if (ctx instanceof LitValueContext) return value((LitValueContext) ctx);
        if (ctx instanceof TypeNameContext) return value((TypeNameContext) ctx);
        if (ctx instanceof VariableContext)  return value((VariableContext) ctx);
        return null; }
    
    List<Formula> values(List<FormulaContext> fs) { return map(fs, f -> value(f)); }
    Formula value(FormulaContext ctx) { return Formula.with(message(ctx.s), messages(ctx.ops)); }

    Expression term(TermContext ctx) { return value(ctx.n); }
    Expression value(NestedTermContext ctx) { return value(ctx.term); }
    Expression value(ExpressionContext ctx) {
        return hasNone(ctx)? null: Expression.with(value(ctx.f), message(ctx.kmsg), cascades(ctx.cmsgs)); }

    Expression value(ExitResultContext ctx) {
        boolean hasExit = (hasOne(ctx) && hasOne(ctx.value));
        return hasExit? value(ctx.value).makeExit(): null; }

    Expression value(EvaluationContext ctx) { return value(ctx.value).makeEvaluated(); }

    List<Statement> evals(List<StatementContext> ss) { return mapList(ss, s -> hasOne(s), s -> value(s)); }
    Statement value(StatementContext ctx) {
        return hasNone(ctx)? null: Statement.with(hasOne(ctx.v)? value(ctx.v): value(ctx.x)); }

    Construct value(MethodScopeContext ctx) { return value(ctx.c); }
    Construct value(ConstructContext ctx) { 
        return hasNone(ctx)? null: Construct.with(literal(ctx.ref), values(ctx.terms)); }

    Variable part(NamedVarContext ctx) { return Variable.named(name(ctx.v), note(ctx.type)); }
    Variable part(NamedArgContext ctx) { return Variable.named(name(ctx.v), note(ctx.type)); }
    Variable part(AssignmentContext ctx)    { return Variable.named(name(ctx.v), note(ctx.type)); }

    Variable value(NamedArgContext ctx) { return part(ctx).withNotes(notes(ctx.n)); }
    Variable value(AssignmentContext ctx) {
        return part(ctx).withValue(value(ctx.value)).withNotes(notes(ctx.n)).makeAssignment(); }
    
    Variable value(VarMemberContext ctx) { return value(ctx.v); }
    Variable value(NamedVarContext ctx) {
        return part(ctx).withValue(value(ctx.value)).withNotes(notes(ctx.n)).defineMember(); }

    Global global(TypeNameContext ctx) { return value(ctx.g).makePrimary(); }
    Global value(GlobalReferContext ctx) { return Global.withList(map(ctx.names, n -> name(n))); }
    Constant value(LiteralValueContext ctx) { return literal(ctx.lit); }
    Constant value(ElementValueContext ctx) {
        if (ctx instanceof LiteralValueContext)  return value((LiteralValueContext)ctx);
        if (ctx instanceof VariableValueContext) return value((VariableValueContext)ctx);
        return null; }


    LiteralArray literal(PrimArrayContext ctx) { return values(ctx.array); }
    LiteralArray values(PrimitiveValuesContext ctx) { return LiteralArray.withItems(lits(ctx.array)); }
    List<Constant> lits(List<PrimitiveContext> vs) { return map(vs, v -> literal(v)); }

    LiteralArray literal(ArrayLiteralContext ctx) { return values(ctx.array); }
    LiteralArray values(ElementValuesContext ctx) { return LiteralArray.withItems(list(ctx.array)); }
    List<Constant> list(List<ElementValueContext> vs) { return map(vs, v -> value(v)); }
    
    // notes

    TypeList superNotes(TypeSignContext ctx) { return TypeList.withDetails(notes(ctx.h.superTypes)); }
    TypeList subNotes(ClassSignContext ctx) { return TypeList.withDetails(notes(ctx.ts.types)); }
    TypeList notes(GenericArgsContext ctx) { return hasNone(ctx)? null: TypeList.withDetails(notes(ctx.types)); }
    TypeList notes(DetailedSignContext ctx) { return itemOr(new TypeList(), notes(ctx.generics)).withExit(note(ctx.exit)); }

    DetailedType note(ExtentItemContext ctx) { return note(ctx.extent); }
    DetailedType note(SignedItemContext ctx) { return note(ctx.signed); }
    DetailedType note(TypeNoteContext ctx) {
        return (hasNone(ctx) || hasNone(ctx.type))? null: note(ctx.type).makeArrayed(hasOne(ctx.etc)); }

    List<DetailedType> notes(List<DetailedTypeContext> cs) { return map(cs, c -> note(c)); }
    DetailedType note(DetailedTypeContext ctx) {
        if (hasNone(ctx)) return null;
        if (ctx instanceof ExtentItemContext)   return note((ExtentItemContext)ctx);
        if (ctx instanceof SignedItemContext)   return note((SignedItemContext)ctx);
        return null; }

    Global subName(TypeSignContext ctx) { return Global.named(name(ctx.sub)); }
    Global subName(ClassSignContext ctx) { return Global.named(name(ctx.sub)); }
    DetailedType superNote(ClassSignContext ctx) { return note(ctx.h.superClass); }
    DetailedType subNote(TypeSignContext ctx) { return DetailedType.with(subName(ctx), notes(ctx.ds)); }
    DetailedType subNote(ClassSignContext ctx) { return DetailedType.with(subName(ctx), notes(ctx.ds)); }
    DetailedType note(SignedTypeContext ctx) { return hasNone(ctx)? null: DetailedType.with(value(ctx.g), notes(ctx.details)); }
    DetailedType note(ExtendTypeContext ctx) { return DetailedType.with(Global.with(name(ctx.g)), note(ctx.baseType)).makeExtensive(); }

    List<Note> notes(CompilationUnitContext ctx) { return notes(ctx.n); }
    List<Note> notes(MethodScopeContext ctx) { return notes(ctx.n); }
    List<Note> notes(NotationsContext ctx) { return map(ctx.notes, n -> note(n)); }

    NoteList notes(TypeSignContext ctx) { return new NoteList().noteAll(notes(ctx.n)); }
    NoteList notes(ClassSignContext ctx) { return new NoteList().noteAll(notes(ctx.n)); }
    KeywordNote note(NotationContext ctx) { return KeywordNote.with(name(ctx.name), nakeds(ctx.nakeds), nameds(ctx.values)); }

    List<NamedValue> nakeds(List<NakedValueContext> cs) { return map(cs, c -> note(c)); }
    NamedValue note(NakedPrimContext ctx) { return NamedValue.with(Empty, literal(ctx.v)); }
    NamedValue note(NakedGlobalContext ctx) { return NamedValue.with(Empty, Global.with(name(ctx.g))); }
    NamedValue note(NakedValueContext ctx) {
        if (ctx instanceof NakedPrimContext)   return note((NakedPrimContext)ctx);
        if (ctx instanceof NakedGlobalContext)   return note((NakedGlobalContext)ctx);
        return null; }

    List<NamedValue> nameds(List<NamedValueContext> cs) { return map(cs, c -> note(c)); }
    NamedValue note(NamedPrimContext ctx) { return NamedValue.with(ctx.head.getText(), literal(ctx.v)); }
    NamedValue note(NamedGlobalContext ctx) { return NamedValue.with(ctx.head.getText(), Global.with(name(ctx.g))); }
    NamedValue note(NamedValueContext ctx) {
        if (ctx instanceof NamedPrimContext)   return note((NamedPrimContext)ctx);
        if (ctx instanceof NamedGlobalContext)   return note((NamedGlobalContext)ctx);
        return null; }
    
    // literals

    String keyword(TypeSignContext ctx) { return ctx.k.getText(); }
    String keyword(ClassSignContext ctx) { return ctx.k.getText(); }
    String comment(TypeSignContext ctx) { return Comment.findComment(ctx.h.getStart(), ctx.p); }
    String comment(ClassSignContext ctx) { return Comment.findComment(ctx.h.getStart(), ctx.p); }
    
    Constant literal(SelfishContext ctx) {
        if (ctx instanceof SelfSelfishContext)  return literal((SelfSelfishContext)ctx);
        if (ctx instanceof SuperSelfishContext) return literal((SuperSelfishContext)ctx);
        return null; }
    
    Constant literal(PrimitiveContext ctx) {
        if (ctx instanceof PrimBoolContext) return literal((PrimBoolContext)ctx);
        if (ctx instanceof PrimCharContext) return literal((PrimCharContext)ctx);
        if (ctx instanceof PrimIntContext)  return literal((PrimIntContext)ctx);
        if (ctx instanceof PrimFloatContext)  return literal((PrimFloatContext)ctx);
        if (ctx instanceof PrimSymbolContext) return literal((PrimSymbolContext)ctx);
        if (ctx instanceof PrimStringContext) return literal((PrimStringContext)ctx);
        if (ctx instanceof PrimArrayContext)  return literal((PrimArrayContext)ctx);
        return null; }
    
    Constant literal(LitValueContext ctx) { return literal(ctx.l); }
    Constant literal(LiteralContext ctx) {
        if (ctx instanceof ArrayLiteralContext) return literal((ArrayLiteralContext)ctx);
        if (ctx instanceof NilLiteralContext)   return literal((NilLiteralContext)ctx);
        if (ctx instanceof SelfLiteralContext)  return literal((SelfLiteralContext)ctx);
        if (ctx instanceof SuperLiteralContext) return literal((SuperLiteralContext)ctx);
        if (ctx instanceof BoolLiteralContext)  return literal((BoolLiteralContext)ctx);
        if (ctx instanceof CharLiteralContext)  return literal((CharLiteralContext)ctx);
        if (ctx instanceof FloatLiteralContext)  return literal((FloatLiteralContext)ctx);
        if (ctx instanceof IntLiteralContext)    return literal((IntLiteralContext)ctx);
        if (ctx instanceof NumLiteralContext)    return literal((NumLiteralContext)ctx);
        if (ctx instanceof SymbolLiteralContext) return literal((SymbolLiteralContext)ctx);
        if (ctx instanceof StringLiteralContext) return literal((StringLiteralContext)ctx);
        if (ctx instanceof DecimalLiteralContext) return literal((DecimalLiteralContext)ctx);
        return null; }

//    LiteralName value(VariableValueContext ctx) { return LiteralName.with(ctx.var.getText(), ctx.start.getLine()); }
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

} // HootFileListener
