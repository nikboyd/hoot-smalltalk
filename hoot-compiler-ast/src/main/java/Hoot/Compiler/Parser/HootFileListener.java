package Hoot.Compiler.Parser;

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

    @Override public void exitFileImport(FileImportContext ctx) {
        File.currentFile().importFace(
            Import.from(File.currentFile(), ctx.g.item, ctx.m.getText()).withLowerCase(hasOne(ctx.c)));
    }

    @Override public void enterClassSignature(ClassSignatureContext ctx) {
        Face.currentFace().definesType(false);
    }

    @Override public void enterTypeSignature(TypeSignatureContext ctx) {
        Face.currentFace().definesType(true);
    }

    CompilationUnitContext unit(TypeSignatureContext ctx) { return (CompilationUnitContext)ctx.getParent().getParent(); }
    @Override public void exitTypeSignature(TypeSignatureContext ctx) {
        CompilationUnitContext unit = unit(ctx);
        Face.currentFace().notes().noteAll(map(unit.n.notes, n -> n.item));
        Face.currentFace().signature(TypeSignature.with(
            TypeList.withDetails(map(ctx.h.superTypes, t -> t.item)),
            DetailedType.with(Global.named(ctx.sub.name), ctx.ds.list),
            new NoteList().noteAll(map(ctx.n.notes, n -> n.item)), ctx.k.getText(),
            Comment.findComment(ctx.h.getStart(), ctx.p)
        ));
    }

    CompilationUnitContext unit(ClassSignatureContext ctx) { return (CompilationUnitContext)ctx.getParent().getParent(); }
    @Override public void exitClassSignature(ClassSignatureContext ctx) {
        CompilationUnitContext unit = unit(ctx);
        Face.currentFace().notes().noteAll(map(unit.n.notes, n -> n.item));
        Face.currentFace().signature(ClassSignature.with(
            nullOr(t -> t.item, ctx.h.superClass),
            DetailedType.with(Global.named(ctx.sub.name), ctx.ds.list),
            TypeList.withDetails(map(ctx.ts.types, t -> t.item)),
            new NoteList().noteAll(map(ctx.n.notes, n -> n.item)), ctx.k.getText(),
            Comment.findComment(ctx.h.getStart(), ctx.p)
        ));
    }
    
    @Override public void exitProtocolSignature(ProtocolSignatureContext ctx) {
        ctx.selector = ctx.s.getText();
        Face.currentFace().selectFace(ctx.selector);
    }

    // methods + blocks

    MethodScopeContext methodScope(MethodBegContext ctx) { return (MethodScopeContext)ctx.getParent(); }
    @Override public void exitMethodBeg(MethodBegContext ctx) {
        MethodScopeContext scope = methodScope(ctx);
        scope.item = new Method().makeCurrent();
        scope.item.notes().noteAll(map(scope.n.notes, n -> n.item));
        scope.item.signature(scope.sign.item);
    }

    MethodScopeContext methodScope(MethodEndContext ctx) { return (MethodScopeContext)ctx.getParent(); }
    @Override public void exitMethodEnd(MethodEndContext ctx) {
        MethodScopeContext scope = methodScope(ctx);
        scope.item.content(scope.content.item);
        scope.item.construct(nullOr(r -> r.item, nullOr(s -> s.c, scope)));
        scope.item.popScope();
    }

    @Override public void exitHeadsAndTails(HeadsAndTailsContext ctx) {
        ctx.argList  = map(ctx.args, arg -> arg.item);
        ctx.headList = map(ctx.kh, head -> head.selector);
        ctx.tailList = map(ctx.kt, tail -> tail.selector);
    }

    @Override public void exitNamedVariable(NamedVariableContext ctx) {
        ctx.item = Variable.named(ctx.v.name,
            nullOr(x -> x.item, ctx.type),
            nullOr(x -> x.item, ctx.value))
            .withNotes(map(ctx.n.notes, n -> n.item))
            .defineMember(); }

    BlockScopeContext blockScope(BlockBegContext ctx) { return (BlockScopeContext)ctx.getParent(); }
    @Override public void exitBlockBeg(BlockBegContext ctx) {
        BlockScopeContext scope = blockScope(ctx);
        scope.b = new Block().makeCurrent();
    }

    BlockScopeContext blockScope(BlockEndContext ctx) { return (BlockScopeContext)ctx.getParent(); }
    @Override public void exitBlockEnd(BlockEndContext ctx) {
        BlockScopeContext scope = blockScope(ctx);
        scope.b.content(blockScope(ctx).content.item);
        scope.b.popScope();
    }

    BlockScopeContext blockScope(BlockSignatureContext ctx) { return (BlockScopeContext)ctx.getParent(); }
    @Override public void exitBlockSignature(BlockSignatureContext ctx) {
        BlockScopeContext scope = blockScope(ctx);
        ctx.item = sign(ctx); scope.b.signature(ctx.item);
    }

    @Override public void exitKeywordSig(KeywordSigContext ctx) { ctx.item = sign(ctx); }
    @Override public void exitBinarySig(BinarySigContext ctx) { ctx.item = sign(ctx); }
    @Override public void exitUnarySig(UnarySigContext ctx) { ctx.item = sign(ctx); }
    
    @Override public void exitBlockContent(BlockContentContext ctx) { ctx.item = block(ctx); }

    // values + messages

    @Override public void exitExitResult(ExitResultContext ctx) {
        ctx.item = ctx.value.item.makeExit();
    }

    @Override public void exitStatement(StatementContext ctx) {
        ctx.item = Statement.with(ctx.v == null ? ctx.x.item : ctx.v.item);
    }

    @Override public void exitConstruct(ConstructContext ctx) {
        ctx.item = Construct.with(ctx.ref.item, map(ctx.terms, term -> term.item));
    }

    @Override public void exitEvaluation(EvaluationContext ctx) {
        ctx.item = ctx.value.item.makeEvaluated();
    }

    @Override public void exitAssignment(AssignmentContext ctx) {
        ctx.item = Variable.named(ctx.v.name,
            nullOr(x -> x.item, ctx.type),
            nullOr(x -> x.item, ctx.value))
            .withNotes(map(ctx.n.notes, n -> n.item))
            .makeAssignment();
    }

    // primary

    @Override public void exitTerm(TermContext ctx) { ctx.item = Primary.with(ctx.term.item); }
    @Override public void exitBlock(BlockContext ctx) {
        ctx.item = Primary.with(ctx.block.b.withNest());
    }

    @Override public void exitLitValue(LitValueContext ctx) {
        ctx.item = Primary.with(ctx.value.item);
    }

    @Override public void exitTypeName(TypeNameContext ctx) {
        ctx.item = Primary.with(ctx.type.item.makePrimary());
    }

    @Override public void exitVarName(VarNameContext ctx) {
        ctx.item = Primary.with(LiteralName.with(ctx.var.name));
    }

    @Override public void exitNestedTerm(NestedTermContext ctx) {
        ctx.item = ctx.term.item;
    }

    @Override public void exitExpression(ExpressionContext ctx) {
        ctx.item = Expression.with(ctx.f.item, nullOr(m -> m.item, ctx.kmsg), map(ctx.cmsgs, msg -> msg.m.item));
    }

    @Override public void exitFormula(FormulaContext ctx) {
        ctx.item = Formula.with(ctx.s.item, map(ctx.ops, op -> op.item));
    }

    @Override public void exitUnarySequence(UnarySequenceContext ctx) { ctx.item = message(ctx); }
    @Override public void exitUnarySelector(UnarySelectorContext ctx) { ctx.selector = selector(ctx); }
    @Override public void exitBinaryMessage(BinaryMessageContext ctx) { ctx.item = message(ctx); }
    @Override public void exitBinaryOperator(BinaryOperatorContext ctx) { ctx.op = selector(ctx); }
    @Override public void exitKeywordMessage(KeywordMessageContext ctx) { ctx.item = message(ctx); }

    @Override public void exitKeywordSelection(KeywordSelectionContext ctx) { ctx.item = ctx.kmsg.item; }
    @Override public void exitBinarySelection(BinarySelectionContext ctx) { ctx.item = ctx.bmsg.item; }
    @Override public void exitUnarySelection(UnarySelectionContext ctx) { ctx.item = selector(ctx); }
    
    // values

    @Override public void exitVarValue(VarValueContext ctx) { ctx.name = ctx.v.name; }
    @Override public void exitGlobalValue(GlobalValueContext ctx) { ctx.name = ctx.g.name; }
    @Override public void exitVariableName(VariableNameContext ctx) { ctx.name = ctx.v.getText(); }
    @Override public void exitGlobalName(GlobalNameContext ctx) { ctx.name = ctx.g.getText(); }
    @Override public void exitGlobalReference(GlobalReferenceContext ctx) { ctx.item = value(ctx); }
    @Override public void exitNamedArgument(NamedArgumentContext ctx) { ctx.item = value(ctx); }

    // notations

    @Override public void exitDetailedSignature(DetailedSignatureContext ctx) { ctx.list = notes(ctx); }
    @Override public void exitGenericTypes(GenericTypesContext ctx) { ctx.list = notes(ctx); }
    @Override public void exitTypeNotation(TypeNotationContext ctx) { ctx.item = note(ctx); }
    @Override public void exitExtentItem(ExtentItemContext ctx) { ctx.item = note(ctx); }
    @Override public void exitExtendType(ExtendTypeContext ctx) { ctx.item = note(ctx); }
    @Override public void exitSignedItem(SignedItemContext ctx) { ctx.item = note(ctx); }
    @Override public void exitSignedType(SignedTypeContext ctx) { ctx.item = note(ctx); }
    @Override public void exitNotation(NotationContext ctx) { ctx.item = note(ctx); }
    @Override public void exitNamedPrim(NamedPrimContext ctx) { ctx.item = note(ctx); }
    @Override public void exitNakedPrim(NakedPrimContext ctx) { ctx.item = note(ctx); }
    @Override public void exitNamedGlobal(NamedGlobalContext ctx) { ctx.item = note(ctx); }
    @Override public void exitNakedGlobal(NakedGlobalContext ctx) { ctx.item = note(ctx); }
    
    // keywords
    
    Hoot.Compiler.Scopes.Face faceScope() { return File.currentFile().faceScope(); }
    @Override public void exitSubclassKeyword(SubclassKeywordContext ctx) { faceScope().makeCurrent(); }
    @Override public void exitSubtypeKeyword(SubtypeKeywordContext ctx) { faceScope().makeCurrent(); }
    @Override public void exitHeadText(HeadTextContext ctx) { ctx.selector = ctx.head.getText(); }
    @Override public void exitWordText(WordTextContext ctx) { ctx.selector = ctx.word.getText(); }
    @Override public void exitKeywordTail(KeywordTailContext ctx) { ctx.selector = ctx.tail.getText(); }

    // constants

    @Override public void exitPrimitiveValues(PrimitiveValuesContext ctx) { ctx.list = values(ctx); }
    @Override public void exitElementValues(ElementValuesContext ctx) { ctx.list = values(ctx); }
    @Override public void exitLiteralValue(LiteralValueContext ctx) { ctx.item = ctx.lit.item; }
    @Override public void exitVariableValue(VariableValueContext ctx) { ctx.item = value(ctx); }

    @Override public void exitPrimArray(PrimArrayContext ctx) { ctx.item = ctx.array.list; }
    @Override public void exitPrimBool(PrimBoolContext ctx) { ctx.item = literal(ctx); }
    @Override public void exitPrimChar(PrimCharContext ctx) { ctx.item = literal(ctx); }
    @Override public void exitPrimFloat(PrimFloatContext ctx) { ctx.item = literal(ctx); }
    @Override public void exitPrimInt(PrimIntContext ctx) { ctx.item = literal(ctx); }
    @Override public void exitPrimSymbol(PrimSymbolContext ctx) { ctx.item = literal(ctx); }
    @Override public void exitPrimString(PrimStringContext ctx) { ctx.item = literal(ctx); }

    @Override public void exitSelfSelfish(SelfSelfishContext ctx) { ctx.item = literal(ctx); }
    @Override public void exitSuperSelfish(SuperSelfishContext ctx) { ctx.item = literal(ctx); }
    @Override public void exitSelfLiteral(SelfLiteralContext ctx) { ctx.item = literal(ctx); }
    @Override public void exitSuperLiteral(SuperLiteralContext ctx) { ctx.item = literal(ctx); }

    @Override public void exitArrayLiteral(ArrayLiteralContext ctx) { ctx.item = ctx.array.list; }
    @Override public void exitNilLiteral(NilLiteralContext ctx) { ctx.item = literal(ctx); }
    @Override public void exitBoolLiteral(BoolLiteralContext ctx) { ctx.item = literal(ctx); }
    @Override public void exitCharLiteral(CharLiteralContext ctx) { ctx.item = literal(ctx); }
    @Override public void exitDecimalLiteral(DecimalLiteralContext ctx) { ctx.item = literal(ctx); }
    @Override public void exitFloatLiteral(FloatLiteralContext ctx) { ctx.item = literal(ctx); }
    @Override public void exitIntLiteral(IntLiteralContext ctx) { ctx.item = literal(ctx); }
    @Override public void exitNumLiteral(NumLiteralContext ctx) { ctx.item = literal(ctx); }
    @Override public void exitSymbolLiteral(SymbolLiteralContext ctx) { ctx.item = literal(ctx); }
    @Override public void exitStringLiteral(StringLiteralContext ctx) { ctx.item = literal(ctx); }
    
    // signatures

    KeywordSignature sign(BlockSignatureContext ctx) {
        return KeywordSignature.with(nullOr(t -> t.item, ctx.type), 
            map(ctx.args, arg -> arg.item)); }

    KeywordSignature sign(KeywordSigContext ctx) {
        return KeywordSignature.with(nullOr(t -> t.item, ctx.ks.result), 
                ctx.ks.name.argList, ctx.ks.name.headList, ctx.ks.name.tailList); }

    BinarySignature sign(BinarySigContext ctx) {
        return BinarySignature.with(nullOr(t -> t.item, ctx.bs.result), 
                map(ctx.bs.args, arg -> arg.item), ctx.bs.name.op); }

    UnarySignature sign(UnarySigContext ctx) {
        return UnarySignature.with(nullOr(t -> t.item, ctx.us.result), 
                ctx.us.name.selector); }
    
    // blocks
    
    BlockContent block(BlockContentContext ctx) {
        return BlockContent.with(map(ctx.s, term -> term.item), 
            nullOr(cx -> cx.item, ctx.r), ctx.p.size()); }
    
    // messages

    String selector(UnarySelectorContext ctx) { return Keyword.with(ctx.s.getText()).methodName(); }
    Operator selector(BinaryOperatorContext ctx) { return Operator.with(ctx.s.getText()); }
    UnarySequence selector(UnarySelectionContext ctx) { return UnarySequence.with(ctx.umsg.selector); }

    UnarySequence message(UnarySequenceContext ctx) {
        return UnarySequence.with(ctx.p.item, map(ctx.msgs, m -> m.selector)); }

    BinaryMessage message(BinaryMessageContext ctx) {
        return BinaryMessage.with(ctx.operator.op, Formula.with(ctx.term.item)); }

    KeywordMessage message(KeywordMessageContext ctx) {
        return KeywordMessage.with(
            map(ctx.kh, head -> head.selector),
            map(ctx.kt, tail -> tail.selector),
            map(ctx.fs, term -> term.item)); }
    
    // values

    Variable value(NamedArgumentContext ctx) {
        return Variable.named(ctx.v.name, nullOr(x -> x.item, ctx.type))
            .withNotes(map(ctx.n.notes, n -> n.item)); }

    Global value(GlobalReferenceContext ctx) {
        return Global.withList(map(ctx.names, n -> n.name)); }

    LiteralName value(VariableValueContext ctx) {
        return LiteralName.with(ctx.var.getText(), ctx.start.getLine()); }

    LiteralArray values(PrimitiveValuesContext ctx) {
        return LiteralArray.withItems(map(ctx.array, v -> v.item)); }

    LiteralArray values(ElementValuesContext ctx) {
        return LiteralArray.withItems(map(ctx.array, v -> v.item)); }
    
    // notes

    TypeList notes(GenericTypesContext ctx) { return TypeList.withDetails(map(ctx.types, t -> t.item)); }
    TypeList notes(DetailedSignatureContext ctx) {
        return itemOr(new TypeList(), nullOr(g -> g.list, ctx.generics))
            .withExit(nullOr(x -> x.item, ctx.exit)); }

    DetailedType note(ExtentItemContext ctx) { return ctx.extent.item; }
    DetailedType note(SignedItemContext ctx) { return ctx.signed.item; }
    DetailedType note(TypeNotationContext ctx) { return nullOr(t -> t.item.makeArrayed(ctx.etc != null), ctx.type); }
    DetailedType note(SignedTypeContext ctx) { return DetailedType.with(ctx.g.item, ctx.details.list); }
    DetailedType note(ExtendTypeContext ctx) {
        return DetailedType.with(Global.with(ctx.g.name), ctx.baseType.item).makeExtensive(); }

    KeywordNote note(NotationContext ctx) {
        return KeywordNote.with(ctx.name.name, 
            map(ctx.nakeds, n -> n.item), 
            map(ctx.values, n -> n.item)); }

    NamedValue note(NakedPrimContext ctx) { return NamedValue.with(Empty, ctx.v.item); }
    NamedValue note(NakedGlobalContext ctx) { return NamedValue.with(Empty, Global.with(ctx.g.name)); }

    NamedValue note(NamedPrimContext ctx) { return NamedValue.with(ctx.head.getText(), ctx.v.item); }
    NamedValue note(NamedGlobalContext ctx) {
        return NamedValue.with(ctx.head.getText(), Global.with(ctx.g.name)); }
    
    // literals

    LiteralName literal(SelfSelfishContext ctx) {
        return LiteralName.with(ctx.refSelf.getText(), ctx.start.getLine()); }

    LiteralName literal(SuperSelfishContext ctx) {
        return LiteralName.with(ctx.refSuper.getText(), ctx.start.getLine()); }

    LiteralName literal(SelfLiteralContext ctx) {
        return LiteralName.with(ctx.refSelf.getText(), ctx.start.getLine()); }

    LiteralName literal(SuperLiteralContext ctx) {
        return LiteralName.with(ctx.refSuper.getText(), ctx.start.getLine()); }

    LiteralNil literal(NilLiteralContext ctx) {
        return LiteralNil.with(ctx.refNil.getText(), ctx.start.getLine()); }

    LiteralRadical literal(NumLiteralContext ctx) {
        return LiteralRadical.with(ctx.n.getText(), ctx.start.getLine()); }
    
    LiteralDecimal literal(DecimalLiteralContext ctx) {
        return LiteralDecimal.with(ctx.value.getText(), ctx.start.getLine()); }

    LiteralCharacter literal(PrimCharContext ctx) {
        return LiteralCharacter.with(ctx.value.getText(), ctx.start.getLine()); }

    LiteralCharacter literal(CharLiteralContext ctx) {
        return LiteralCharacter.with(ctx.value.getText(), ctx.start.getLine()); }

    LiteralBoolean literal(PrimBoolContext ctx) {
        return LiteralBoolean.with(ctx.bool.getText(), ctx.start.getLine()); }

    LiteralBoolean literal(BoolLiteralContext ctx) {
        return LiteralBoolean.with(ctx.bool.getText(), ctx.start.getLine()); }

    LiteralFloat literal(PrimFloatContext ctx) {
        return LiteralFloat.with(ctx.value.getText(), ctx.start.getLine()); }

    LiteralFloat literal(FloatLiteralContext ctx) {
        return LiteralFloat.with(ctx.value.getText(), ctx.start.getLine()); }

    LiteralInteger literal(PrimIntContext ctx) {
        return LiteralInteger.with(ctx.value.getText(), ctx.start.getLine()); }

    LiteralInteger literal(IntLiteralContext ctx) {
        return LiteralInteger.with(ctx.value.getText(), ctx.start.getLine()); }

    LiteralSymbol literal(PrimSymbolContext ctx) {
        return LiteralSymbol.with(ctx.value.getText(), ctx.start.getLine()); }

    LiteralSymbol literal(SymbolLiteralContext ctx) {
        return LiteralSymbol.with(ctx.value.getText(), ctx.start.getLine()); }

    LiteralString literal(PrimStringContext ctx) {
        return LiteralString.with(ctx.value.getText(), ctx.start.getLine()); }

    LiteralString literal(StringLiteralContext ctx) {
        return LiteralString.with(ctx.value.getText(), ctx.start.getLine()); }

} // HootFileListener
