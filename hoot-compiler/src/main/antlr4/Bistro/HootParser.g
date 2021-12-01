//==================================================================================================
// Copyright 2010,2017 Nikolas S. Boyd. All rights reserved.
//==================================================================================================

parser grammar HootParser;

options {
language = Java;
tokenVocab = HootLexer;
}

@header {
import java.util.*;
import org.slf4j.*;

//import Hoot.Coders.*;
import Hoot.Notes.*;
import Hoot.Values.*;
import Hoot.Scopes.*;
import Hoot.Methods.*;
import Hoot.Messages.*;
import Hoot.Runtime.*;
import Hoot.Compiler.Scopes.*;
import Hoot.Compiler.Elements.*;
import Hoot.Compiler.Constants.*;
import Hoot.Compiler.Expressions.*;
import Hoot.Messages.Expression;
import Hoot.Messages.Assignment;
import Hoot.Values.Reference;
import Hoot.Runtime.Behaviors.Scope;
import Hoot.Runtime.Names.Keyword;
import Hoot.Runtime.Names.Operator;
import Hoot.Runtime.Emissions.Item;
}

@members {
static final String Empty = "";
}

compilationUnit returns [Item item = null]
locals [FileScope fileScope]
@init {$ctx.fileScope = new FileScope();}
: c=classScope {$item = $c.item;}
| t=typeScope  {$item = $t.item;}
;

fileImport returns [Item item = null]
@after {$item = FileImport.with($ctx);}
: g=globalReference m=( Import | ImportAll ) Period ;

typeScope returns [Item item = null]
@after {$item = TypeScope.with($ctx);}
: ( notes+=notation )* ( imps+=fileImport )*
    sign=typeSignature p=Period ( scopes+=groupingScope )* ;

typeSignature returns [Item item = null]
@after {$item = TypeSignature.with($ctx);}
: heritage=typeHeritage k=subtypeKeyword
( notes+=notation )* subType=globalName sign=detailedSignature ;

groupingScope returns [Item item = null]
@after {$item = GroupingScope.with($ctx);}
: sign=groupingSignature BlockInit ( members+=typeMember )* BlockExit ;

groupingSignature returns [String selector = ""]
@after {$selector = $metaType.text;}
: ( notes+=notation )* g=globalName ( metaType=typeUnary )? membersKeyword ;

typeMember returns [Item item = null]
: v=namedVariable {$item = $v.item;}
| m=methodScope   {$item = $m.item;}
;


classScope returns [Item item = null]
@after {$item = ClassScope.with($ctx);}
: ( notes+=notation )* ( imps+=fileImport )*
    sign=classSignature p=Period ( scopes+=protocolScope )* ;

classSignature returns [Item item = null]
@after {$item = ClassSignature.with($ctx);}
: ( superClass=signedBase | Nil ) k=subclassKeyword
  ( notes+=notation )* ( types+=detailedType Bang )*
    subClass=globalName details=detailedSignature ;

protocolScope returns [Item item = null]
@after {$item = ProtocolScope.with($ctx);}
: sign=protocolSignature b=BlockInit ( members+=classMember )* BlockExit ;

protocolSignature returns [String selector = ""]
@after {$selector = $metaClass.text;}
: ( notes+=notation )* g=globalName ( metaClass=classUnary )? membersKeyword ;

classMember returns [Item item = null]
: v=namedVariable {$item = $v.item;}
| s=methodScope   {$item = $s.item;}
;

namedVariable returns [Item item = null]
@after {$item = NamedVariable.with($ctx);}
: ( notes+=notation )* type=typeNotation
  ( v=variableName | g=globalName ) ( Assign value=expression )? p=Period ;


methodScope returns [Item item = null]
locals [StackScope stackScope]
@init {$ctx.stackScope = new StackScope(); $item = MethodScope.with($ctx);}
: ( notes+=notation )* sign=methodSignature
    b=BlockInit ( content=blockContent | ) x=BlockExit ;

methodSignature returns [Item item = null]
: ksign=keywordSignature {$item = $ksign.item;}
| bsign=binarySignature  {$item = $bsign.item;}
| usign=unarySignature   {$item = $usign.item;}
;

unarySignature returns [Item item = null]
@after {$item = UnarySignature.with($ctx);}
: type=methodResult name=unarySelector ;


binarySignature returns [Item item = null]
@after {$item = BinarySignature.with($ctx);}
: type=methodResult name=binarySelector args+=namedArgument ;


keywordSignature returns [Item item = null]
@after {$item = KeywordSignature.with($ctx);}
: type=methodResult
    heads+=keywordHead args+=namedArgument
( ( heads+=keywordHead args+=namedArgument )+
| ( tails+=keywordTail args+=namedArgument )+
| )
;


namedArgument returns [Item item = null]
@after {$item = NamedArgument.with($ctx);}
: ( notes+=notation )* type=typeNotation var=variableName ;

blockScope returns [Item item = null]
locals [StackScope stackScope]
@init  {$ctx.stackScope = new StackScope(); $item = BlockScope.with($ctx);}
: BlockInit ( sign=blockSignature content=blockContent | ) BlockExit ;

blockSignature returns [Item item = null]
@after {$item = BlockSignature.with($ctx);}
: type=typeNotation ( keywordTail args+=namedArgument )+ Bar | ;

blockContent returns [Item item = null]
@after {$item = BlockContent.with($ctx);}
: ( s+=statement p+=Period )* ( s+=statement ( p+=Period )? | value=result )
| ;

result returns [Item item = null]
@after {$item = Result.with($ctx);}
: Exit value=expression ;

statement returns [Item item = null]
@after {$item = Statement.with($ctx);}
: x=assignment | v=evaluation | c=construct ;

construct returns [Item item = null]
@after {$item = Construct.with($ctx);}
: ref=selfish ( tails+=keywordTail terms+=formula )*
;

evaluation returns [Item item = null]
@after {$item = Expression.from($ctx);}
: value=expression ;

assignment returns [Item item = null]
@after {$item = Assignment.with($ctx);}
: ( notes+=notation )* type=typeNotation
  ( v=variableName | g=globalName ) Assign value=expression ;

//====================================================================
// values + messages
//====================================================================

primary returns [Item item = null]
: term=nestedTerm     {$item = Primary.with($ctx);}
| block=blockScope    {$item = Primary.with($ctx);}
| value=literal       {$item = Primary.with($ctx);}
| type=detailedType   {$item = Primary.with($ctx);}
| var=variableName    {$item = Primary.with($ctx);}
;

nestedTerm returns [Item item = null]
@after {$item = Expression.with($ctx.value);}
: TermInit value=expression TermExit ;

expression returns [Item item = null]
@after {$item = Expression.with($ctx);}
: term=formula ( kmsg=keywordMessage )? ( cmsgs+=messageCascade )* ;

formula returns [Item item = null]
@after {$item = Formula.with($ctx);}
: term=unarySequence ( ops+=binaryMessage )* ;

unarySequence returns [Item item = null]
@after {$item = UnarySequence.with($ctx);}
: term=primary ( msgs+=unarySelector )* ;

binaryMessage returns [Item item = null]
@after {$item = BinaryMessage.with($ctx);}
: operator=binarySelector term=unarySequence ;

keywordMessage returns [Item item = null]
@after {$item = KeywordMessage.with($ctx);}
:   heads+=keywordHead terms+=formula
( ( heads+=keywordHead terms+=formula )+
| ( tails+=keywordTail terms+=formula )+
| ) ;

messageCascade : Cascade message ;

message returns [Item item = null]
: kmsg=keywordMessage {$item = $kmsg.item;} # KeywordSelection
| bmsg=binaryMessage  {$item = $bmsg.item;} # BinarySelection
| umsg=unarySelector  {$item = SelectorList.withAll($umsg.selector);} # UnarySelection
;

//==================================================================================================
// notations
//==================================================================================================

notation returns [Item item = null]
@after {$item = Note.with($ctx);}
: At name=globalName note=keywordNotation Bang ;

keywordNotation returns [Item item = null]
: ( values+=namedValue )+ {$item = KeywordNote.with($ctx);}
| ( nakeds+=nakedValue )+ {$item = KeywordNote.with($ctx);}
|
;

namedValue returns [Item item = null]
: head=keywordHead v=primitive  {$item = NamedValue.with($head.text, $v.item);}
| head=keywordHead g=globalName {$item = NamedValue.with($head.text, Reference.withNames($g.name));}
;

nakedValue returns [Item item = null]
: tail=keywordTail v=primitive  {$item = NamedValue.with("", $v.item);}
| tail=keywordTail g=globalName {$item = NamedValue.with("", Reference.withNames($g.name));}
;


typeHeritage returns [Item item = null]
@after {$item = TypeHeritage.with($ctx);}
: superTypes+=detailedType ( Comma superTypes+=detailedType )*
| Nil ;

methodResult returns [Item item = null]
@after {$item = MethodResult.with($ctx);}
: type=typeNotation ;

typeNotation returns [Item item = null]
@after {$item = TypeNote.with($ctx);}
: type=detailedType Bang ( etc=Etc )?
| ;

detailedSignature returns [Item item = null]
@after {$item = DetailedSignature.with($ctx);}
: Quest types+=detailedType ( keywordTail types+=detailedType )* ( Exit exit=detailedType )?
| Exit exit=detailedType
| ;

detailedType returns [Item item = null]
@after {$item = DetailedType.with($ctx);}
: type=globalName Extends baseType=detailedType
| signed=signedBase
;

signedBase returns [Item item = null]
@after {$item = SignedBase.with($ctx);}
: refType=globalReference details=detailedSignature
;

globalReference returns [Item item = null]
@after {$item = Reference.withList(Reference.listFrom($ctx));}
: ( names+=globalName )+ ;

//==================================================================================================
// constants
//==================================================================================================

primitiveValues returns [List<Item> list = null]
@after {$list = PrimitiveValue.arrayValues($array);}
: Pound TermInit ( array+=primitive )* TermExit ;

elementValues returns [List<Item> list = null]
@after {$list = LiteralValue.arrayValues($array);}
: Pound TermInit array+=elementValue* TermExit ;

elementValue returns [Item item = null]
: lit=literal      {$item = $lit.item;}
| var=variableName {$item = Reference.withNames($var.name);} ;

primitive returns [Item item = null]
: array=primitiveValues   {$item = PrimitiveValue.with($ctx, "Array");}
| bool=literalBoolean     {$item = PrimitiveValue.with($ctx, "Boolean");}
| value=ConstantCharacter {$item = PrimitiveValue.with($ctx, "Character");}
| value=ConstantInteger   {$item = PrimitiveValue.with($ctx, "Integer");}
| value=ConstantFloat     {$item = PrimitiveValue.with($ctx, "Float");}
| value=ConstantSymbol    {$item = PrimitiveValue.with($ctx, "Symbol");}
| value=ConstantString    {$item = PrimitiveValue.with($ctx, "String");}
;

selfish returns [Item item = null]
: refSelf=literalSelf     {$item = LiteralValue.with($ctx, "Self");}
| refSuper=literalSuper   {$item = LiteralValue.with($ctx, "Super");}
;

literal returns [Item item = null]
: array=elementValues     {$item = LiteralValue.with($ctx, "Array");}
| refNil=literalNil       {$item = LiteralValue.with($ctx, "Nil");}
| refSelf=literalSelf     {$item = LiteralValue.with($ctx, "Self");}
| refSuper=literalSuper   {$item = LiteralValue.with($ctx, "Super");}
| bool=literalBoolean     {$item = LiteralValue.with($ctx, "Boolean");}
| value=ConstantCharacter {$item = LiteralValue.with($ctx, "Character");}
| value=ConstantInteger   {$item = LiteralValue.with($ctx, "Integer"); Scope.Current.operands().push(LiteralInteger.from($start, Scope.Current));}
| value=ConstantFloat     {$item = LiteralValue.with($ctx, "Float");}
| value=ConstantSymbol    {$item = LiteralValue.with($ctx, "Symbol");}
| value=ConstantString    {$item = LiteralValue.with($ctx, "String");}
| value=ConstantDecimal   {$item = LiteralValue.with($ctx, "Decimal");}
| n=radixedNumber         {$item = LiteralValue.with($ctx, "Radical");}
;

//==================================================================================================
// references
//==================================================================================================

literalNil     : Nil   ;
literalSelf    : Self  ;
literalSuper   : Super ;
literalBoolean : True | False ;
radixedNumber  : ConstantBinary | ConstantOctal | ConstantHex ;

//==================================================================================================
// keywords
//==================================================================================================

metaclassKeyword  : Metaclass ;
subclassKeyword   : Subclass ;
classKeyword      : Class ;

metatypeKeyword   : Metatype ;
subtypeKeyword    : Subtype ;
typeKeyword       : Type ;

implementsKeyword : Implements ;
protocolKeyword   : Protocol ;
membersKeyword    : Members ;

reservedWord
: Metaclass  | Subclass | Class
| Metatype   | Subtype  | Type
| Implements | Protocol | Members
| Import     | ImportAll
;

keywordHead returns [String selector = Empty]
: head=KeywordHead  {$selector = $head.text;}
| word=reservedWord {$selector = $word.text;}
;

keywordTail returns [String selector = Empty]
@after {$selector = $tail.text;}
: tail=KeywordTail ;

//==================================================================================================
// selectors
//==================================================================================================

classUnary : ClassUnary ;
typeUnary  : TypeUnary ;

unarySelector returns [String selector = Empty]
@after {$selector = Keyword.with($s.text).methodName();}
: s=LocalName | s=ClassUnary | s=TypeUnary | s=GlobalName ;

binarySelector returns [String selector = Empty]
@after {$selector = Operator.with($s.text).methodName();}
: s=At | s=Bar | s=Comma | s=BinaryOperator | s=Usage ;

globalName returns [String name = Empty]
@after {$name = $g.text;}
: g=GlobalName ;

variableName returns [String name = Empty]
@after {$name = $v.text;}
: v=LocalName ;

//==================================================================================================
