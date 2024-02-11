//==================================================================================================
// Copyright 2010,2021 Nikolas S Boyd.
// Permission is granted to copy this work provided this copyright statement is retained in all copies.
//==================================================================================================

grammar Hoot; // located in resources/antlr4/Hoot/Compiler/Parser

@header {
import Hoot.Runtime.Notes.*;
import Hoot.Runtime.Names.*;
import Hoot.Runtime.Values.*;
import static Hoot.Runtime.Functions.Utils.*;

import Hoot.Compiler.Notes.*;
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
// file scopes
//==================================================================================================

compilationUnit : n=notations ( fileImport )* ( classScope | typeScope ) ;
classScope      : sign=classSignature ( scopes+=protocolScope )* ;
typeScope       : sign=typeSignature  ( scopes+=protocolScope )* ;

fileImport      : g=globalReference ( c=caseOption )? m=importSelector Period ;
//{File.currentFile().importFace(Import.from(File.currentFile(), $g.item, $m.text).withLowerCase(hasOne($ctx.c)));} ;

importSelector  : ImportOne | ImportAll | ImportStatics ;
caseOption      : CaseMessage ;

typeSignature   : h=typeHeritage k=subtypeKeyword n=notations sub=globalName ds=detailedSignature p=Period 
{CompilationUnitContext unit = (CompilationUnitContext)$ctx.getParent().getParent();
Face.currentFace().notes().noteAll(map(unit.n.notes, n -> n.item));
Face.currentFace().signature(TypeSignature.with(
TypeList.withDetails(map($ctx.h.superTypes, t -> t.item)),
DetailedType.with(Global.named($ctx.sub.name), $ctx.ds.list),
new NoteList().noteAll(map($ctx.n.notes, n -> n.item)), $ctx.k.getText(),
Comment.findComment($ctx.h.getStart(), $ctx.p)));} ;

classSignature  : h=classHeritage k=subclassKeyword n=notations ts=typeNotes sub=globalName ds=detailedSignature p=Period
{CompilationUnitContext unit = (CompilationUnitContext)$ctx.getParent().getParent();
Face.currentFace().notes().noteAll(map(unit.n.notes, n -> n.item));
Face.currentFace().signature(ClassSignature.with(
nullOr(t -> t.item, $ctx.h.superClass),
DetailedType.with(Global.named($ctx.sub.name), $ctx.ds.list),
TypeList.withDetails(map($ctx.ts.types, t -> t.item)),
new NoteList().noteAll(map($ctx.n.notes, n -> n.item)), $ctx.k.getText(),
Comment.findComment($ctx.h.getStart(), $ctx.p)));} ;

protocolScope   : sign=protocolSignature b=BlockInit ( members+=classMember )* x=BlockExit ;
classMember     : v=namedVariable | m=methodScope ;

protocolSignature returns
[String selector = ""]  : n=notations g=globalName s=metaUnary k=membersKeyword
{$selector = $s.text; Face.currentFace().selectFace($selector);} ;

namedVariable returns [Variable item]         
: n=notations type=typeNotation v=valueName ( Assign value=expression )? p=Period ;
//{$item = Variable.named($ctx.v.name, nullOr(x -> x.item, $ctx.type), nullOr(x -> x.item, $ctx.value)).withNotes(map($ctx.n.notes, n -> n.item)).defineMember();} ;

//==================================================================================================
// methods + blocks
//==================================================================================================

methodScope returns
[Method item] : n=notations sign=methodSignature b=methodBeg ( c=construct Period )? content=blockContent x=methodEnd ;

methodBeg : BlockInit
{$methodScope::item = new Method().makeCurrent();
MethodScopeContext scope = (MethodScopeContext)$ctx.getParent();
$methodScope::item.notes().noteAll(map(scope.n.notes, n -> n.item));
$methodScope::item.signature(scope.sign.item);} ;

methodEnd : BlockExit
{MethodScopeContext scope = (MethodScopeContext)$ctx.getParent();
$methodScope::item.content(scope.content.item);
$methodScope::item.construct(nullOr(r -> r.item, nullOr(ctx -> ctx.c, scope)));
$methodScope::item.popScope();} ;

methodSignature returns [BasicSignature item]
: ks=keywordSignature   {$item=KeywordSignature.with(nullOr(t -> t.item, $ctx.ks.result), $ctx.ks.name.argList, $ctx.ks.name.headList, $ctx.ks.name.tailList);}
| bs=binarySignature    {$item=BinarySignature.with(nullOr(t -> t.item, $ctx.bs.result), map($ctx.bs.args, arg -> arg.item), $ctx.bs.name.op);}
| us=unarySignature     {$item=UnarySignature.with(nullOr(t -> t.item, $ctx.us.result), $ctx.us.name.selector);}
;

unarySignature   : result=typeNotation name=unarySelector ;
binarySignature  : result=typeNotation name=binaryOperator args+=namedArgument ;
keywordSignature : result=typeNotation name=headsAndTails ;

headsAndTails returns
[List<Variable> argList, List<String> headList, List<String> tailList]
  : kh+=keywordHead args+=namedArgument ( | (kh+=keywordHead args+=namedArgument)+ | (kt+=keywordTail args+=namedArgument)+ )
{$argList = map($ctx.args, arg -> arg.item);
$headList = map($ctx.kh, head -> head.selector);
$tailList = map($ctx.kt, tail -> tail.selector);} ;

namedArgument returns
[Variable item]         : n=notations type=typeNotation v=variableName
{$item = Variable.named($ctx.v.name, nullOr(x -> x.item, $ctx.type)).withNotes(map($ctx.n.notes, n -> n.item));} ;

blockScope returns      [Block b] : blockBeg sign=blockSignature content=blockContent blockEnd ;
blockBeg : BlockInit    {$blockScope::b = new Block().makeCurrent();} ;
blockEnd : BlockExit    {$blockScope::b.content(((BlockScopeContext)$ctx.getParent()).content.item);
                         $blockScope::b.popScope();} ;

blockSignature returns
[KeywordSignature item] : ( | type=typeNotation ( tails+=keywordTail args+=namedArgument )+ Bar )
{$item = KeywordSignature.with(nullOr(t -> t.item, $ctx.type), map($ctx.args, arg -> arg.item));
$blockScope::b.signature($item);} ;

blockContent returns
[BlockContent item]     : ( s+=statement p+=Period )* ( s+=statement ( p+=Period )? | r=exitResult | )
{$item = BlockContent.with(map($ctx.s, term -> term.item), nullOr(ctx -> ctx.item, $ctx.r), $ctx.p.size());} ;

//==================================================================================================
// values + messages
//==================================================================================================

exitResult returns [Expression item] : Exit value=expression ;
statement  returns [Statement item]  : ( x=assignment | v=evaluation ) ;
construct  returns [Construct item]  : ref=selfish ( tails+=keywordTail terms+=formula )* ;
evaluation returns [Expression item] : value=expression ;
assignment returns [Variable item]   : n=notations type=typeNotation v=valueName Assign value=expression ;

primary returns         [Primary item = null]
: term=nestedTerm       # term //{$item = Primary.with($term.item);}
| block=blockScope      # block //{$item = Primary.with($block.b.withNest());}
| value=literal         # litValue //{$item = Primary.with($value.item);}
| type=globalReference  # typeName //{$item = Primary.with($type.item.makePrimary());}
| var=variableName      # varName //{$item = Primary.with(LiteralName.with($var.name));}
;

nestedTerm returns [Expression item] : TermInit term=expression TermExit ;
expression returns [Expression item] : f=formula ( kmsg=keywordMessage )? ( cmsgs+=messageCascade )* ;
formula    returns [Formula item]    : s=unarySequence ( ops+=binaryMessage )* ;
unarySequence returns [UnarySequence item] : p=primary ( msgs+=unarySelector )* ;
binaryMessage returns [BinaryMessage item] : operator=binaryOperator term=unarySequence ;
keywordMessage returns [KeywordMessage item]
: kh+=keywordHead fs+=formula ((kh+=keywordHead fs+=formula)+ | (kt+=keywordTail fs+=formula)+ | ) ;
//{$item = KeywordMessage.with(map($ctx.kh, head -> head.selector), map($ctx.kt, tail -> tail.selector), map($ctx.fs, term -> term.item));} ;

messageCascade          : Cascade m=message ;
message returns         [Message item = null]
: kmsg=keywordMessage   {$item = $kmsg.item;} # KeywordSelection
| bmsg=binaryMessage    {$item = $bmsg.item;} # BinarySelection
| umsg=unarySelector    {$item = UnarySequence.with($umsg.selector);} # UnarySelection
;

//==================================================================================================
// notations
//==================================================================================================

classHeritage : Nil | superClass=signedType ;
typeHeritage  : Nil | superTypes+=detailedType ( Comma superTypes+=detailedType )* ;

detailedSignature returns [TypeList list = null] : ( generics=genericTypes )? ( Exit exit=detailedType )?
{$list = itemOr(new TypeList(), nullOr(g -> g.list, $ctx.generics)).withExit(nullOr(x -> x.item, $ctx.exit));} ;

notations                       : ( notes+=notation )* ;
notation returns [KeywordNote item = null]
: At name=globalName ( ( | ( values+=namedValue )+ | ( nakeds+=nakedValue )+ ) Bang )? ;
//{$item = KeywordNote.with($ctx.name.name, map($ctx.nakeds, n -> n.item), map($ctx.values, n -> n.item));} ;

namedValue returns              [NamedValue item = null]
: head=keywordHead v=primitive  {$item = NamedValue.with($head.text, $v.item);}
| head=keywordHead g=globalName {$item = NamedValue.with($head.text, Global.with($g.name));}
;

nakedValue returns              [NamedValue item = null]
: tail=keywordTail v=primitive  {$item = NamedValue.with(Empty, $v.item);}
| tail=keywordTail g=globalName {$item = NamedValue.with(Empty, Global.with($g.name));}
;

typeNotes                       : ( types+=detailedType Bang )* ;
detailedType returns            [DetailedType item = null]
: extent=extendType             {$item = $extent.item;}
| signed=signedType             {$item = $signed.item;}
;

typeNotation returns
[DetailedType item = null]      : ( | type=detailedType Bang ( etc=Etc )? )
{$item = nullOr(t -> t.item.makeArrayed($ctx.etc != null), $ctx.type);} ;

genericTypes returns
[TypeList list = null]          : Quest types+=detailedType ( keywordTail types+=detailedType )*
{$list = TypeList.withDetails(map($types, t -> t.item));} ;

extendType returns
[DetailedType item = null]      : g=globalName Extends baseType=detailedType
{$item = DetailedType.with(Global.with($g.name), $baseType.item).makeExtensive();} ;

signedType returns
[DetailedType item = null]      : g=globalReference details=detailedSignature
{$item = DetailedType.with($g.item, $details.list);} ;

//==================================================================================================
// references
//==================================================================================================

literalNil     : Nil   ;
literalSelf    : Self  ;
literalSuper   : Super ;
literalBoolean : True | False ;

valueName returns       [String name = Empty]
: ( v=variableName      {$name = $v.name;}
  | g=globalName        {$name = $g.name;}
  )
;

variableName returns    [String name = Empty]
: v=LocalName           {$name = $v.text;}
;

globalName returns      [String name = Empty]
: g=GlobalName          {$name = $g.text;}
;

globalReference returns   [Global item = null]
: ( names+=globalName )+  {$item = Global.withList(map($names, n -> n.name));}
;

//==================================================================================================
// keywords
//==================================================================================================

subclassKeyword   : Subclass {File.currentFile().faceScope().makeCurrent();} ;
subtypeKeyword    : Subtype  {File.currentFile().faceScope().makeCurrent();} ;

metaclassKeyword  : Metaclass ;
metatypeKeyword   : Metatype ;
classKeyword      : Class ;
typeKeyword       : Type ;

implementsKeyword : Implements ;
protocolKeyword   : Protocol ;
membersKeyword    : Members ;

reservedWord
: Metaclass  | Subclass | Class
| Metatype   | Subtype  | Type
| Implements | Protocol | Members
| ImportOne  | ImportAll | ImportStatics
;

keywordHead returns [String selector = Empty]
: head=KeywordHead  {$selector = $head.text;}
| word=reservedWord {$selector = $word.text;}
;

keywordTail returns [String selector = Empty]
: tail=KeywordTail  {$selector = $tail.text;}
;

//==================================================================================================
// selectors
//==================================================================================================

metaUnary  : classUnary | typeUnary | ;
classUnary : ClassUnary ;
typeUnary  : TypeUnary ;

unarySelector returns [String selector = Empty]
: ( s=LocalName | s=ClassUnary | s=TypeUnary | s=GlobalName ) ;
//{$selector = Keyword.with($s.text).methodName();} ;

binaryOperator returns [Operator op]
: ( s=At | s=Bar | s=Comma | s=BinaryOperator | s=Usage ) ;
//{$op = Operator.with($s.text);} ;

//==================================================================================================
// constants
//==================================================================================================

primitiveValues returns [LiteralArray list]       
: Pound TermInit ( array+=primitive )* TermExit ;
//{$list = LiteralArray.withItems(map($array, v -> v.item));} ;

elementValues returns [LiteralArray list]       
: Pound TermInit ( array+=elementValue )* TermExit ;
//{$list = LiteralArray.withItems(map($array, v -> v.item));} ;

elementValue returns [Constant item]
: lit=literal             # literalValue // {$item = $lit.item;}
| var=variableName        # variableValue // {$item = LiteralName.with($var.text, $start.getLine());}
;

primitive returns [Constant item]
: array=primitiveValues   # primArray // {$item = $array.list;}
| bool=literalBoolean     # primBool // {$item = LiteralBoolean.with($start.getText(), $start.getLine());}
| value=ConstantCharacter # primChar // {$item = LiteralCharacter.with($start.getText(), $start.getLine());}
| value=ConstantInteger   # primInt // {$item = LiteralInteger.with($start.getText(), $start.getLine());}
| value=ConstantFloat     # primFloat // {$item = LiteralFloat.with($start.getText(), $start.getLine());}
| value=ConstantSymbol    # primSymbol // {$item = LiteralSymbol.with($start.getText(), $start.getLine());}
| value=ConstantString    # primString // {$item = LiteralString.with($start.getText(), $start.getLine());}
;

selfish returns [Constant item]
: refSelf=literalSelf     # selfSelfish //{$item = LiteralName.with($refSelf.text, $start.getLine());}
| refSuper=literalSuper   # superSelfish // {$item = LiteralName.with($refSuper.text, $start.getLine());}
;

literal returns [Constant item]
: array=elementValues     # arrayLiteraal // {$item = $array.list;}
| refNil=literalNil       # nilLiteral // {$item = LiteralNil.with($start.getText(), $start.getLine());}
| refSelf=literalSelf     # selfLiteral // {$item = LiteralName.with($refSelf.text, $start.getLine());}
| refSuper=literalSuper   # superLiteral // {$item = LiteralName.with($refSuper.text, $start.getLine());}
| bool=literalBoolean     # boolLiteral // {$item = LiteralBoolean.with($start.getText(), $start.getLine());}
| value=ConstantCharacter # charLiteral // {$item = LiteralCharacter.with($start.getText(), $start.getLine());}
| value=ConstantDecimal   # decimalLiteral // {$item = LiteralDecimal.with($start.getText(), $start.getLine());}
| value=ConstantFloat     # floatLiteral  // {$item = LiteralFloat.with($start.getText(), $start.getLine());}
| value=ConstantInteger   # intLiteral // {$item = LiteralInteger.with($start.getText(), $start.getLine());}
| n=radixedNumber         # numLiteral // {$item = LiteralRadical.with($start.getText(), $start.getLine());}
| value=ConstantSymbol    # symbolLiteral  // {$item = LiteralSymbol.with($start.getText(), $start.getLine());}
| value=ConstantString    # stringLiteral // {$item = LiteralString.with($start.getText(), $start.getLine());}
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

constantFloat   : ConstantFloat ;
constantDecimal : ConstantDecimal ;
constantInteger : ConstantInteger ;
radixedNumber   : ConstantBinary | ConstantOctal | ConstantHex ;

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

Metaclass   : 'metaclass:' ;
Subclass    : 'subclass:' ;
Class       : 'class:' ;
ClassUnary  : 'class' ;

Metatype    : 'metatype:' ;
Subtype     : 'subtype:' ;
Type        : 'type:' ;
TypeUnary   : 'type' ;

Implements  : 'implements:' ;
Protocol    : 'protocol:' ;
Members     : 'members:' ;

CaseMessage : 'lowerCase' ;
ImportOne   : 'import' ;
ImportAll   : 'importAll' ;
ImportStatics : 'importStatics' ;

KeywordHead : Name Colon ;
KeywordTail : Colon ;
GlobalName  : UpperCase Tail* ;
LocalName   : Score? LowerCase Tail* ;

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

fragment QuotedString  : SingleQuote .*? SingleQuote ;
fragment QuotedComment : DoubleQuote .*? DoubleQuote ;

fragment SymbolString
: KeywordHead+ KeywordTail*
| ConstantString
| BinaryOperator
| Name
;

fragment DoubleQuote : '"' ;
fragment SingleQuote : '\'' ;

//==================================================================================================
// operators
//==================================================================================================

BinaryOperator :
  ComparisonOperator | MathOperator | LogicalOperator | ExtraOperator | FastMath | ShiftOperator ;

fragment ComparisonOperator :
  More | Less | Equal | More Equal | Less Equal | Not Equal | Equal Equal | Not Not | Equal Equal Equal ;

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

fragment Score          : [_];
fragment UpperCase      : [A-Z] ;
fragment LowerCase      : [a-z] ;
fragment DecimalDigit   : [0-9] ;
fragment OrdinalDigit   : [1-9] ;
fragment BinaryDigit    : [0-1] ;
fragment OctalDigit     : [0-7] ;
fragment HexDigit       : DecimalDigit | [A-F] ;
fragment Zero           : '0' ;

//==================================================================================================
// white space
//==================================================================================================

WhiteSpaces : WhiteSpace+ -> skip ;
fragment WhiteSpace : [ \t\r\n\f] ;

//==================================================================================================
