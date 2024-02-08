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

    @Override public void exitTypeSignature(TypeSignatureContext ctx) {
//        CompilationUnitContext unit = (CompilationUnitContext)ctx.getParent().getParent();
//        Face.currentFace().notes().noteAll(map(unit.n.notes, n -> n.item));
//        Face.currentFace().signature(TypeSignature.with(
//            TypeList.withDetails(map(ctx.h.superTypes, t -> t.item)),
//            DetailedType.with(Global.named(ctx.sub.name), ctx.ds.list),
//            new NoteList().noteAll(map(ctx.n.notes, n -> n.item)), ctx.k.getText(),
//            Comment.findComment(ctx.h.getStart(), ctx.p)
//        ));
    }

    @Override public void exitNamedVariable(NamedVariableContext ctx) {
        ctx.item = Variable.named(ctx.v.name,
            nullOr(x -> x.item, ctx.type),
            nullOr(x -> x.item, ctx.value))
            .withNotes(map(ctx.n.notes, n -> n.item))
            .defineMember();
    }

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

    @Override public void exitTerm(TermContext ctx) {
        ctx.item = Primary.with(ctx.term.item);
    }

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

    @Override public void exitNotation(NotationContext ctx) {
        ctx.item = KeywordNote.with(ctx.name.name, map(ctx.nakeds, n -> n.item), map(ctx.values, n -> n.item));
        report(ctx.item.notice());
    }

    @Override public void exitExpression(ExpressionContext ctx) {
        ctx.item = Expression.with(ctx.f.item, nullOr(m -> m.item, ctx.kmsg), map(ctx.cmsgs, msg -> msg.m.item));
    }

    @Override public void exitFormula(FormulaContext ctx) {
        ctx.item = Formula.with(ctx.s.item, map(ctx.ops, op -> op.item));
    }

    @Override public void exitUnarySelector(UnarySelectorContext ctx) {
        ctx.selector = Keyword.with(ctx.s.getText()).methodName();
    }

    @Override public void exitBinaryOperator(BinaryOperatorContext ctx) {
        ctx.op = Operator.with(ctx.s.getText());
        report("op "+ctx.op.methodName());
    }

    @Override public void exitPrimitiveValues(PrimitiveValuesContext ctx) {
        ctx.list = LiteralArray.withItems(map(ctx.array, v -> v.item));
    }

    @Override public void exitElementValues(ElementValuesContext ctx) {
        ctx.list = LiteralArray.withItems(map(ctx.array, v -> v.item));
    }

    @Override public void exitLiteralValue(LiteralValueContext ctx) {
        ctx.item = ctx.lit.item;
    }

    @Override public void exitVariableValue(VariableValueContext ctx) {
        ctx.item = LiteralName.with(ctx.var.getText(), ctx.start.getLine());
    }

    @Override public void exitPrimArray(PrimArrayContext ctx) {
        ctx.item = ctx.array.list;
    }

    @Override public void exitPrimBool(PrimBoolContext ctx) {
        ctx.item = LiteralBoolean.with(ctx.bool.getText(), ctx.start.getLine());
    }

    @Override public void exitPrimChar(PrimCharContext ctx) {
        ctx.item = LiteralCharacter.with(ctx.value.getText(), ctx.start.getLine());
    }

    @Override public void exitPrimFloat(PrimFloatContext ctx) {
        ctx.item = LiteralFloat.with(ctx.value.getText(), ctx.start.getLine());
    }

    @Override public void exitPrimInt(PrimIntContext ctx) {
        ctx.item = LiteralInteger.with(ctx.value.getText(), ctx.start.getLine());
    }

    @Override public void exitPrimSymbol(PrimSymbolContext ctx) {
        ctx.item = LiteralSymbol.with(ctx.value.getText(), ctx.start.getLine());
    }

    @Override public void exitPrimString(PrimStringContext ctx) {
        ctx.item = LiteralString.with(ctx.value.getText(), ctx.start.getLine());
        report(ctx.item.encodedValue());
    }


    @Override public void exitSelfSelfish(SelfSelfishContext ctx) {
        ctx.item = LiteralName.with(ctx.refSelf.getText(), ctx.start.getLine());
    }

    @Override public void exitSuperSelfish(SuperSelfishContext ctx) {
        ctx.item = LiteralName.with(ctx.refSuper.getText(), ctx.start.getLine());
    }

    @Override public void exitSelfLiteral(SelfLiteralContext ctx) {
        ctx.item = LiteralName.with(ctx.refSelf.getText(), ctx.start.getLine());
    }

    @Override public void exitSuperLiteral(SuperLiteralContext ctx) {
        ctx.item = LiteralName.with(ctx.refSuper.getText(), ctx.start.getLine());
    }

    @Override public void exitNilLiteral(NilLiteralContext ctx) {
        ctx.item = LiteralNil.with(ctx.refNil.getText(), ctx.start.getLine());
    }

    @Override public void exitBoolLiteral(BoolLiteralContext ctx) {
        ctx.item = LiteralBoolean.with(ctx.bool.getText(), ctx.start.getLine());
    }

    @Override public void exitCharLiteral(CharLiteralContext ctx) {
        ctx.item = LiteralCharacter.with(ctx.value.getText(), ctx.start.getLine());
    }

    @Override public void exitDecimalLiteral(DecimalLiteralContext ctx) {
        ctx.item = LiteralDecimal.with(ctx.value.getText(), ctx.start.getLine());
    }

    @Override public void exitFloatLiteral(FloatLiteralContext ctx) {
        ctx.item = LiteralFloat.with(ctx.value.getText(), ctx.start.getLine());
    }

    @Override public void exitIntLiteral(IntLiteralContext ctx) {
        ctx.item = LiteralInteger.with(ctx.value.getText(), ctx.start.getLine());
    }

    @Override public void exitNumLiteral(NumLiteralContext ctx) {
        ctx.item = LiteralRadical.with(ctx.n.getText(), ctx.start.getLine());
    }

    @Override public void exitSymbolLiteral(SymbolLiteralContext ctx) {
        ctx.item = LiteralSymbol.with(ctx.value.getText(), ctx.start.getLine());
    }

    @Override public void exitStringLiteral(StringLiteralContext ctx) {
        ctx.item = LiteralString.with(ctx.value.getText(), ctx.start.getLine());
    }

} // HootFileListener
