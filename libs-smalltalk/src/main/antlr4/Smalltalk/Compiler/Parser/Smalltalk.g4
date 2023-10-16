//==================================================================================================
// Copyright 2010,2021 Nikolas S Boyd.
// Permission is granted to copy this work provided this copyright statement is retained in all copies.
//==================================================================================================

grammar Smalltalk;

@header {
import Hoot.Runtime.Notes.*;
import Hoot.Runtime.Names.*;
import Hoot.Runtime.Values.*;
import Hoot.Runtime.Emissions.Item;
import Hoot.Runtime.Behaviors.Scope;
import static Hoot.Runtime.Functions.Utils.*;

//import Hoot.Compiler.Notes.*;
import Hoot.Compiler.Scopes.*;
import Hoot.Compiler.Scopes.File;
import Hoot.Compiler.Scopes.Block;
import Hoot.Compiler.Expressions.*;
import Hoot.Compiler.Constants.*;
}

@members {
static final String Empty = "";
}

//==================================================================================================
// Smalltalk chunk file formats
// Requires:
//      single quotes around header comment, 
//      removal of comment and class declaration before class methods.
//==================================================================================================

compilationUnit : ( unitHeader?  cms+=methodReader )* ;

unitHeader : ( filedHeader | classHeader ) ;
filedHeader : fc=filedComment banger cs=classSignature cr=commentHeader ;
filedComment : fc=ConstantString {
String c = $fc.text;
Scope s = Scope.current();
//s.report("filedComment");
//s.report(c);
} ;

classHeader : cc=codedComment banger ms=classSignature ;
codedComment : cc=codeComment {
String c = $cc.text;
Scope s = Scope.current();
//s.report("codedComment");
//s.report(c);
} ;

commentHeader : banger x=expression banger c=ConstantString banger {
// Global commentStamp: String prior: Integer ! comment text !
Global classGlobal = $x.item.formula().primaryTerm().primary().asGlobal();
Face face = Face.currentFace();
//face.report("commentHeader");
} ;

//==================================================================================================
// class scope
//==================================================================================================

classSignature  : x=expression banger {
// Global subclass: Symbol instanceVariableNames: String classVariableNames: String poolDictionaries: String category: String
// CompilationUnitContext unit = (CompilationUnitContext)$ctx.getParent();
Global superGlobal = $x.item.formula().primaryTerm().primary().asGlobal();
LiteralSymbol subName = $x.item.keywordMessage().formulas().get(0).primaryTerm().primary().asSymbol();
Global subGlobal = Global.named(subName.encodedValue());
String message = $x.item.keywordMessage().methodName();
Face f = Face.currentFace().signature(ClassSignature.with(superGlobal, subGlobal, message));
LiteralString iVars = $x.item.keywordMessage().formulas().get(1).primaryTerm().primary().asString();
String[] vars = iVars.unquotedValue().split(" ");
for (String v : vars) Variable.from(Face.currentFace(), v, DetailedType.RootType).defineMember();
Face.currentFace().makeCurrent(); // no op
//f.report("classSignature");
//f.report(message);
} ;

methodReader : banger pr=protoHeader banger ms=methodScope banger ;
protoHeader : p=expression { // protocol
// Global methodsFor: String stamp: String
Global classGlobal = $p.item.formula().primaryTerm().primary().asGlobal();
KeywordMessage kmsg = $p.item.keywordMessage();
LiteralString name = kmsg.formulas().get(0).primaryTerm().primary().asString();
Face face = Face.currentFace();
//face.report("method proto: "+name.unquotedValue());
} ;

//==================================================================================================
// method scopes
//==================================================================================================

methodScope returns [Method item = new Method().makeCurrent()] :
sign=methodSignature b=methodBeg ( vs=localVariables )? content=blockContent x=methodEnd ;

methodBeg : whiteSpaces? cc=codeComment* {
MethodScopeContext scope = (MethodScopeContext)$ctx.getParent();
$methodScope::item.signature(scope.sign.item);
//$methodScope::item.report("found block start");
} ;

methodEnd : banger {
MethodScopeContext scope = (MethodScopeContext)$ctx.getParent();
$methodScope::item.content(scope.content.item);
$methodScope::item.popScope();
//$methodScope::item.report("found block end");
} ;

methodSignature returns  [BasicSignature item = null]
: ksign=keywordSignature {$item = KeywordSignature.with(DetailedType.RootType, $ctx.ksign.ks.argList, $ctx.ksign.ks.headList, new ArrayList());}
| bsign=binarySignature  {$item = BinarySignature.with(DetailedType.RootType, map($ctx.bsign.args, arg -> arg.item), $ctx.bsign.bs.op);}
| usign=unarySignature   {$item = UnarySignature.with(DetailedType.RootType, $ctx.usign.us.selector);}
;

unarySignature    : us=unarySelector ;
binarySignature   : bs=binaryOperator args+=namedArgument ;
keywordSignature  : ks=headsAndTails ;

headsAndTails returns   [List<Variable> argList, List<String> headList]
: heads+=keywordHead args+=namedArgument (
| ( heads+=keywordHead args+=namedArgument )+ ) {
$argList  = map($ctx.args,  arg -> arg.item);
$headList = map($ctx.heads, head -> head.selector);}
;

namedArgument returns [Variable item = null]
: v=variableName {
$item = Variable.named($ctx.v.name, null)
//.withNotes(map($ctx.notes, n -> n.item))
;}
;

blockScope returns [Block item = null]
: blockBeg sign=blockSignature content=blockContent blockEnd
;

blockBeg : BlockInit {$blockScope::item = new Block().makeCurrent();} ;
blockEnd : BlockExit {
BlockScopeContext scope = (BlockScopeContext)$ctx.getParent();
$blockScope::item.content(scope.content.item);
$blockScope::item.popScope();}
;

blockSignature returns [KeywordSignature item = null]
: ( | ( tails+=keywordTail args+=namedArgument )+ Bar ) {
$item = KeywordSignature.with(null, map($ctx.args, arg -> arg.item));
$blockScope::item.signature($item);}
;

blockContent returns [BlockContent item = null]
: ( s+=statement p+=Period )* ( r=exitResult ( p+=Period )? | s+=statement )? {
//Scope s = Scope.current(); s.report("block");
$item = BlockContent.with(map($ctx.s, term -> term.item), nullOr(ctx -> ctx.item, $ctx.r), $ctx.p.size());
} ;

//==================================================================================================
// messages + statements
//==================================================================================================

exitResult returns                  [Expression item = null]
: Exit value=expression             {
//Scope s = Scope.current(); s.report("exit");
$item = $value.item.makeExit();}
;

statement returns                   [Statement item = null]
: ( x=assignment | v=evaluation )   {
//Scope s = Scope.current(); s.report("statement");
$item = Statement.with($ctx.v == null ? $ctx.x.item : $ctx.v.item);}
;

assignment returns                  [Variable item = null]
: v=valueName Assign value=expression {
//Scope s = Scope.current(); s.report("assignment");
$item = Variable.named($ctx.v.name, null, nullOr(x -> x.item, $ctx.value)).makeAssignment();}
;

evaluation returns                  [Expression item = null]
: value=expression                  {
//Scope s = Scope.current(); s.report("evaluation");
$item = $value.item.makeEvaluated();}
;

primary returns         [Primary item = null]
:
( var=variableName      {$item = Primary.with(LiteralName.with($var.name));}
| type=globalReference  {$item = Primary.with($type.item.makePrimary());}
| block=blockScope      {$item = Primary.with($block.item.withNest());}
| term=nestedTerm       {$item = Primary.with($term.item);}
| value=literal         {$item = Primary.with($value.item);}
) {
//Scope s = Scope.current(); s.report($item.itemClassName());
} ;

nestedTerm returns                   [Expression item = null]
: TermInit term=expression TermExit  {$item = $ctx.term.item;}
;

expression returns                  [Expression item = null]
: f=formula ( kmsg=keywordMessage )? ( cmsgs+=messageCascade )* {
$item = Expression.with($ctx.f.item, nullOr(m -> m.item, $ctx.kmsg), map($ctx.cmsgs, msg -> msg.m.item));}
;

formula returns                             [Formula item = null]
: s=unarySequence ( ops+=binaryMessage )*   {$item = Formula.with($ctx.s.item, map($ctx.ops, op -> op.item));}
;

unarySequence returns                       [UnarySequence item = null]
: p=primary ( msgs+=unarySelector )*        {$item = UnarySequence.with($ctx.p.item, map($ctx.msgs, m -> m.selector));}
;

binaryMessage returns                         [BinaryMessage item = null]
: operator=binaryOperator term=unarySequence  {$item = BinaryMessage.with($ctx.operator.op, Formula.with($ctx.term.item));}
;

keywordMessage returns [KeywordMessage item = null]
:   heads+=keywordHead terms+=formula
( ( heads+=keywordHead terms+=formula )+
| ) {
//Scope s = Scope.current(); s.report("keyword msg");
List<String> heads = map($ctx.heads, head -> head.selector);
List<Formula> terms = map($ctx.terms, term -> term.item);
$item = KeywordMessage.with(heads, new ArrayList(), terms);}
;

messageCascade : Cascade m=message ;

message returns         [Message item = null]
: kmsg=keywordMessage   {$item = $kmsg.item;} # KeywordSelection
| bmsg=binaryMessage    {$item = $bmsg.item;} # BinarySelection
| umsg=unarySelector    {$item = UnarySequence.with($umsg.selector);} # UnarySelection
;

//==================================================================================================
// selectors
//==================================================================================================

unarySelector returns   [String selector = Empty]
: s=LocalName           {$selector = Keyword.with($s.text).methodName();}
;

binaryOperator returns  [Operator op]
: s=BinaryOperator      {$op = Operator.with($s.text);}
;

//==================================================================================================
// keywords
//==================================================================================================

keywordHead returns  [String selector = Empty]
: head=KeywordHead   {$selector = $head.text;}
;

keywordTail returns  [String selector = Empty]
: tail=KeywordTail   {$selector = $tail.text;}
;

//==================================================================================================
// references
//==================================================================================================

literalNil     : Nil   ;
literalSelf    : Self  ;
literalSuper   : Super ;
literalBoolean : True | False ;

valueName returns    [String name = Empty]
: ( v=variableName   {$name = $v.name;}
  | g=globalName     {$name = $g.name;}
  )
;

//==================================================================================================
// constants
//==================================================================================================

selfish returns           [Constant item = null;]
: refSelf=literalSelf     {$item = LiteralName.with($refSelf.text, $start.getLine());}
| refSuper=literalSuper   {$item = LiteralName.with($refSuper.text, $start.getLine());}
;

literal returns           [Constant item = null]
: array=elementValues     {$item = $array.list;}
| refNil=literalNil       {$item = LiteralNil.with($start.getText(), $start.getLine());}
| refSelf=literalSelf     {$item = LiteralName.with($refSelf.text, $start.getLine());}
| refSuper=literalSuper   {$item = LiteralName.with($refSuper.text, $start.getLine());}
| bool=literalBoolean     {$item = LiteralBoolean.with($start.getText(), $start.getLine());}
| value=ConstantCharacter {$item = LiteralCharacter.with($start.getText(), $start.getLine());}
| value=ConstantDecimal   {$item = LiteralDecimal.with($start.getText(), $start.getLine());}
| value=ConstantFloat     {$item = LiteralFloat.with($start.getText(), $start.getLine());}
| value=ConstantInteger   {$item = LiteralInteger.with($start.getText(), $start.getLine());}
| n=radixedNumber         {$item = LiteralRadical.with($start.getText(), $start.getLine());}
| value=ConstantSymbol    {$item = LiteralSymbol.with($start.getText(), $start.getLine());}
| value=ConstantString    {$item = LiteralString.with($start.getText(), $start.getLine());}
;

elementValues returns [LiteralArray list = null]
: Pound TermInit ( array+=elementValue )* TermExit  {$list = LiteralArray.withItems(map($array, v -> v.item));}
|       NoteInit ( array+=elementValue )* NoteExit  {$list = LiteralArray.withItems(map($array, v -> v.item));}
;

elementValue returns    [Constant item = null]
: lit=literal           {$item = $lit.item;}
| var=variableName      {$item = LiteralName.with($var.text, $start.getLine());}
;


//==================================================================================================
// scopes
//==================================================================================================

BlockInit : '[' ;
BlockExit : ']' ;

TermInit  : '(' ;
TermExit  : ')' ;

NoteInit  : '{' ;
NoteExit  : '}' ;

//==================================================================================================
// literal numbers
//==================================================================================================

radixedNumber  : ConstantBinary | ConstantOctal | ConstantHex ;

ConstantBinary  : BinaryRadix  BinaryDigit+ ;
ConstantOctal   : OctalRadix   OctalDigit+ ;
ConstantHex     : HexRadix     HexDigit+ ;

ConstantDecimal : CardinalNumber ( Dot CardinalFraction )? Scale ;
ConstantFloat   : CardinalNumber   Dot CardinalFraction Exponent? | CardinalNumber Exponent ;
ConstantInteger : CardinalNumber ;

Period : Dot ;

fragment OrdinaryNumber   : OrdinalDigit DecimalDigit* ;
fragment OrdinaryFraction : DecimalDigit* OrdinalDigit ;
fragment CardinalNumber   : Zero | Minus? OrdinaryNumber ;
fragment CardinalFraction : Zero | OrdinaryFraction ;

fragment Scale    : ScaleMark OrdinaryNumber ;
fragment Exponent : PowerMark Minus? OrdinaryNumber ;

fragment BinaryRadix :  '2r' ;
fragment OctalRadix  :  '8r' ;
fragment HexRadix    : '16r' ;
fragment ScaleMark   :   's' ;
fragment PowerMark   :   'e' ;

//==================================================================================================
// keywords + identifiers
//==================================================================================================

Nil         : 'nil' ;
Self        : 'self' ;
Super       : 'super' ;
True        : 'true' ;
False       : 'false' ;

localVariables : Bar ( names+=variableName )* Bar ;
variableName returns [String name = Empty]
: v=LocalName        {$name = $v.text;}
;

globalReference returns [Global item = null]
: ( ns+=globalName )+   {$item = Global.withList(map($ns, n -> n.name));}
;

globalName returns   [String name = Empty]
: g=GlobalName       {$name = $g.text;}
;

KeywordHead : Name Colon ;
KeywordTail : Colon ;
GlobalName  : UpperCase Tail* ;
LocalName   : LowerCase Tail* ;
Words       : Word+ ;

fragment Colon  : ':' ;
fragment Name   : Letter Tail* ;
fragment Tail   : Letter | DecimalDigit ;
fragment Letter : UpperCase | LowerCase ;
fragment Word   : Letter+ ;

//==================================================================================================
// strings
//==================================================================================================

ConstantCharacter : '$' . ;
ConstantSymbol    : Pound SymbolString Dot? ;
ConstantString    : QuotedString ConstantString? ;

codeComment : CodeComment ;
CodeComment : QuotedComment -> channel(HIDDEN) ;

fragment QuotedString  : SingleQuote .*? SingleQuote ;
fragment QuotedComment : DoubleQuote .*? DoubleQuote ;

fragment SymbolString
: KeywordHead+
| ConstantString
| BinaryOperator
| Name
;

fragment DoubleQuote : '"' ;
fragment SingleQuote : '\'' ;

//==================================================================================================
// punctuators
//==================================================================================================

banger : Bang {
Face face = Face.currentFace();
//face.report("bang!");
} ;

Assign  : Colon Equal ;
Etc     : '...' ;

Exit    : '^' ;
Cascade : ';' ;
Bang    : '!' ;
Quest   : '?' ;
Pound   : '#' ;
Bar     : '|' ;

//==================================================================================================
// operators
//==================================================================================================

BinaryOperator
: ComparisonOperator
| MathOperator
| LogicalOperator
| ExtraOperator
| ShiftOperator
| At | Bar | Comma
;

fragment ComparisonOperator
: More | Less | Equal | More Equal | Less Equal | Not Equal | Equal Equal | Not Not | Equal Equal Equal ;

fragment LogicalOperator : And | Or | Less Less | More More ;
fragment MathOperator    : Times | Times Times | Divide | Divide Divide | Modulo | Plus | Minus ;
fragment ExtraOperator   : Percent | Quest ;
fragment ShiftOperator   : More More | Less Less | Less Less Equal | Exit Equal ;

//==================================================================================================
// operators
//==================================================================================================

fragment Percent  : '%' ;

fragment Truncate : '//' ;
fragment Modulo   : '\\\\' ;
fragment Divide   : '/' ;
fragment Times    : '*' ;
fragment Plus     : '+' ;
fragment Minus    : '-' ;

fragment Equal    : '=' ;
fragment More     : '>' ;
fragment Less     : '<' ;
fragment Not      : '~' ;

fragment And      : '&' ;
fragment Or       : '|' ;

fragment Dot      : '.' ;
fragment Comma    : ',' ;
fragment At       : '@' ;

//==================================================================================================
// letters + digits
//==================================================================================================

fragment UpperCase : [A-Z] ;
fragment LowerCase : [a-z] ;

fragment DecimalDigit : [0-9] ;
fragment OrdinalDigit : [1-9] ;
fragment BinaryDigit  : [0-1] ;
fragment OctalDigit   : [0-7] ;
fragment HexDigit     : DecimalDigit | [A-F] ;
fragment Zero         : '0' ;

//==================================================================================================
// white space
//==================================================================================================

whiteSpaces : WhiteSpaces ;
WhiteSpaces : WhiteSpace+ -> skip ;
fragment WhiteSpace : [ \t\r\n\f] ;
fragment Blank : [ ] ;

//==================================================================================================
