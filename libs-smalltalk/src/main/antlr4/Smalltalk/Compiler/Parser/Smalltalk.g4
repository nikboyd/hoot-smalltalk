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
//==================================================================================================

compilationUnit : fc=ConstantString Bang cs=classSignature Bang cr=commentReader ( mrs+=methodReader )* ;

//chunkReader : Bang ( methodReader Bang | commentReader ) ; // starts with a bang!
commentReader : cl=globalName Commented v=literal Prior literal Bang s=Sentences Bang ;
methodReader  : cl=globalName Protocol v=literal ( Stamp literal )? Bang ms=methodScope Bang ;

//chunks      : ( chs+=chunk Bang )+ ; // starts with a chunk
//chunk       : ( literal | classSignature ) Bang ;

//==================================================================================================
// class scope
//==================================================================================================

classSignature  : x=expression
// h=globalName k=Subclass sub=literal InstaVars iVars=literal ClassVars cVars=literal PoolNames pools=literal Category cat=literal
{
CompilationUnitContext unit = (CompilationUnitContext)$ctx.getParent();
//Global superGlobal = Global.named($ctx.h.name); Global subGlobal = Global.named($ctx.sub.item.encodedValue());
//Face.currentFace().signature(ClassSignature.with(superGlobal, subGlobal, $ctx.k.getText()));
Face.currentFace().makeCurrent();
} ;

//==================================================================================================
// method scopes
//==================================================================================================

methodScope returns [Method item] :
sign=methodSignature b=methodBeg ( vs=localVariables )? content=blockContent x=methodEnd ;

methodBeg : WhiteSpaces {
MethodScopeContext scope = (MethodScopeContext)$ctx.getParent();
$methodScope::item = new Method().makeCurrent();
$methodScope::item.signature(scope.sign.item);} ;

methodEnd : Bang {
MethodScopeContext scope = (MethodScopeContext)$ctx.getParent();
$methodScope::item.content(scope.content.item);
$methodScope::item.popScope();} ;

methodSignature returns  [BasicSignature item = null]
: ksign=keywordSignature {$item = KeywordSignature.with(null, $ctx.ksign.ks.argList, $ctx.ksign.ks.headList, $ctx.ksign.ks.tailList);}
| bsign=binarySignature  {$item = BinarySignature.with(null, map($ctx.bsign.args, arg -> arg.item), $ctx.bsign.bs.op);}
| usign=unarySignature   {$item = UnarySignature.with(null, $ctx.usign.us.selector);}
;

unarySignature    : us=unarySelector ;
binarySignature   : bs=binaryOperator args+=namedArgument ;
keywordSignature  : ks=headsAndTails ;

headsAndTails returns   [List<Variable> argList, List<String> headList, List<String> tailList]
: heads+=keywordHead args+=namedArgument (
| ( heads+=keywordHead args+=namedArgument )+
| ( tails+=keywordTail args+=namedArgument )+ ) {
$argList  = map($ctx.args,  arg -> arg.item);
$headList = map($ctx.heads, head -> head.selector);
$tailList = map($ctx.tails, tail -> tail.selector);}
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
: ( s+=statement p+=Period )* ( s+=statement ( p+=Period )? | r=exitResult | ) {
$item = BlockContent.with(map($ctx.s, term -> term.item), nullOr(ctx -> ctx.item, $ctx.r), $ctx.p.size());}
;

//==================================================================================================
// selectors
//==================================================================================================

unarySelector returns             [String selector = Empty]
: ( s=LocalName | s=GlobalName )  {$selector = Keyword.with($s.text).methodName();}
;

binaryOperator returns            [Operator op]
: ( s=BinaryOperator | s=Usage )  {$op = Operator.with($s.text);}
;

//==================================================================================================
// messages + statements
//==================================================================================================

exitResult returns                  [Expression item = null]
: Exit value=expression             {$item = $value.item.makeExit();}
;

statement returns                   [Statement item = null]
: ( x=assignment | v=evaluation )   {$item = Statement.with($ctx.v == null ? $ctx.x.item : $ctx.v.item);}
;

evaluation returns                  [Expression item = null]
: value=expression                  {$item = $value.item.makeEvaluated();}
;

assignment returns                  [Variable item = null]
: v=valueName Assign value=expression {
$item = Variable.named($ctx.v.name, null, nullOr(x -> x.item, $ctx.value)).makeAssignment();}
;

primary returns         [Primary item = null]
:( term=nestedTerm      {$item = Primary.with($term.item);}
| block=blockScope      {$item = Primary.with($block.item.withNest());}
| value=literal         {$item = Primary.with($value.item);}
| type=globalReference  {$item = Primary.with($type.item.makePrimary());}
| var=variableName      {$item = Primary.with(LiteralName.with($var.name));}
);

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
| ( tails+=keywordTail terms+=formula )+
| ) {
List<String> heads = map($ctx.heads, head -> head.selector);
List<String> tails = map($ctx.tails, tail -> tail.selector);
List<Formula> terms = map($ctx.terms, term -> term.item);
$item = KeywordMessage.with(heads, tails, terms);}
;

messageCascade : Cascade m=message ;

message returns         [Message item = null]
: kmsg=keywordMessage   {$item = $kmsg.item;} # KeywordSelection
| bmsg=binaryMessage    {$item = $bmsg.item;} # BinarySelection
| umsg=unarySelector    {$item = UnarySequence.with($umsg.selector);} # UnarySelection
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

localVariables : Bar ( names+=variableName )* Bar ;
variableName returns [String name = Empty]
: v=LocalName        {$name = $v.text;}
;

globalName returns   [String name = Empty]
: g=GlobalName       {$name = $g.text;}
;

globalReference returns [Global item = null]
: ( ns+=globalName )+   {$item = Global.withList(map($ns, n -> n.name));}
;

//==================================================================================================
// constants
//==================================================================================================

elementValues returns [LiteralArray list = null]
: Pound TermInit ( array+=elementValue )* TermExit  {$list = LiteralArray.withItems(map($array, v -> v.item));}
;

elementValue returns    [Constant item = null]
: lit=literal           {$item = $lit.item;}
| var=variableName      {$item = LiteralName.with($var.text, $start.getLine());}
;

selfish returns         [Constant item = null;]
: refSelf=literalSelf   {$item = LiteralName.with($refSelf.text, $start.getLine());}
| refSuper=literalSuper {$item = LiteralName.with($refSuper.text, $start.getLine());}
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
// punctuators
//==================================================================================================

Assign  : Colon Equal ;
Extends : Minus More ;
Usage   : Less Minus ;
Etc     : '...' ;

Exit    : '^' ;
Cascade : ';' ;
Bang    : '!' ;
Quest   : '?' ;
Pound   : '#' ;
Comma   : ',' ;
Bar     : '|' ;
At      : '@' ;

//==================================================================================================
// literal numbers
//==================================================================================================

radixedNumber  : ConstantBinary | ConstantOctal | ConstantHex ;

ConstantBinary  : BinaryRadix  BinaryDigit+ ;
ConstantOctal   : OctalRadix   OctalDigit+ ;
ConstantHex     : HexRadix     HexDigit+ ;

ConstantDecimal : CardinalNumber ( Dot CardinalFraction )? Scale ;
ConstantFloat   : CardinalNumber Dot CardinalFraction Exponent? | CardinalNumber Exponent ;
ConstantInteger : CardinalNumber ;

Period : Dot ;

fragment OrdinaryNumber   : OrdinalDigit DecimalDigit* ;
fragment OrdinaryFraction : DecimalDigit* OrdinalDigit ;
fragment CardinalNumber   : Zero | OrdinaryNumber ;
fragment CardinalFraction : Zero | OrdinaryFraction ;

fragment Dot      : '.' ;
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

//Subclass    : 'subclass:' ;
//InstaVars   : 'instanceVariableNames:' ;
//ClassVars   : 'classVariableNames:' ;
//PoolNames   : 'poolDictionaries:' ;
//Category    : 'category:' ;
Protocol    : 'methodsFor:' ;
Commented   : 'commentStamp:' ;
Stamp       : 'stamp:' ;
Prior       : 'prior:' ;

KeywordHead : Name Colon ;
KeywordTail : Colon ;
GlobalName  : UpperCase Tail* ;
LocalName   : LowerCase Tail* ;

fragment Colon  : ':' ;
fragment Name   : Letter Tail* ;
fragment Tail   : Letter | DecimalDigit ;
fragment Letter : UpperCase | LowerCase ;

//==================================================================================================
// strings
//==================================================================================================

ConstantCharacter : '$' . ;
ConstantSymbol    : Pound SymbolString ;
ConstantString    : QuotedString ConstantString? ;
CodeComment       : QuotedComment -> channel(HIDDEN) ;
Sentences         : ( Name WhiteSpace? )* Dot? ;

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
// operators
//==================================================================================================

BinaryOperator
: ComparisonOperator
| MathOperator
| LogicalOperator
| ExtraOperator
| FastMath
| ShiftOperator
| At | Bar | Comma
;

fragment ComparisonOperator
: More | Less | Equal | More Equal | Less Equal | Not Equal | Equal Equal | Not Not | Equal Equal Equal ;

fragment LogicalOperator : And | Or | Less Less | More More ;
fragment MathOperator    : Times | Times Times | Divide | Divide Divide | Modulo | Plus | Minus ;
fragment FastMath        : Plus Equal | Minus Equal | Times Equal | Divide Equal ;
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

WhiteSpaces : WhiteSpace+ -> skip ;
fragment WhiteSpace : [ \t\r\n\f] ;

//==================================================================================================
