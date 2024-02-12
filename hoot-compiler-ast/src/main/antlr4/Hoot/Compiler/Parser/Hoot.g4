//==================================================================================================
// Copyright 2010,2024 Nikolas S Boyd.
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
importSelector  : ImportOne | ImportAll | ImportStatics ;
caseOption      : CaseMessage ;

typeSignature   : h=typeHeritage k=subtypeKeyword n=notations sub=globalName ds=detailedSignature p=Period ;
classSignature  : h=classHeritage k=subclassKeyword n=notations ts=typeNotes sub=globalName ds=detailedSignature p=Period ;

protocolSignature returns [String selector = ""]  : n=notations g=globalName s=metaUnary k=membersKeyword ;
protocolScope   : sign=protocolSignature b=BlockInit ( members+=classMember )* x=BlockExit ;
classMember     : v=namedVariable | m=methodScope ;

namedVariable returns [Variable item]
: n=notations type=typeNotation v=valueName ( Assign value=expression )? p=Period ;

//==================================================================================================
// methods + blocks
//==================================================================================================

methodScope returns [Method item] 
: n=notations sign=methodSignature b=methodBeg ( c=construct Period )? content=blockContent x=methodEnd ;

methodBeg : BlockInit ;
methodEnd : BlockExit ;
methodSignature returns [BasicSignature item]
: ks=keywordSignature  # keywordSig
| bs=binarySignature   # binarySig
| us=unarySignature    # unarySig
;

unarySignature   : result=typeNotation name=unarySelector ;
binarySignature  : result=typeNotation name=binaryOperator args+=namedArgument ;
keywordSignature : result=typeNotation name=headsAndTails ;

headsAndTails returns [List<Variable> argList, List<String> headList, List<String> tailList]
:    kh+=keywordHead args+=namedArgument 
( | (kh+=keywordHead args+=namedArgument)+ 
  | (kt+=keywordTail args+=namedArgument)+
)
;

namedArgument returns [Variable item] : n=notations type=typeNotation v=variableName ;
blockScope returns [Block b] : blockBeg sign=blockSignature content=blockContent blockEnd ;
blockBeg : BlockInit ;
blockEnd : BlockExit ;

blockSignature returns [KeywordSignature item]
: ( | type=typeNotation ( tails+=keywordTail args+=namedArgument )+ Bar ) ;

blockContent returns [BlockContent item]
: ( s+=statement p+=Period )* ( s+=statement ( p+=Period )? | r=exitResult | ) ;

//==================================================================================================
// values + messages
//==================================================================================================

evaluation returns [Expression item] : value=expression ;
exitResult returns [Expression item] : Exit value=expression ;
statement  returns [Statement item]  : ( x=assignment | v=evaluation ) ;
construct  returns [Construct item]  : ref=selfish ( tails+=keywordTail terms+=formula )* ;
assignment returns [Variable item]   : n=notations type=typeNotation v=valueName Assign value=expression ;

primary returns [Primary item = null]
: term=nestedTerm       # term
| block=blockScope      # block
| value=literal         # litValue
| type=globalReference  # typeName
| var=variableName      # varName
;

nestedTerm     returns [Expression item]    : TermInit term=expression TermExit ;
expression     returns [Expression item]    : f=formula ( kmsg=keywordMessage )? ( cmsgs+=messageCascade )* ;
formula        returns [Formula item]       : s=unarySequence ( ops+=binaryMessage )* ;
unarySequence  returns [UnarySequence item] : p=primary ( msgs+=unarySelector )* ;
binaryMessage  returns [BinaryMessage item] : operator=binaryOperator term=unarySequence ;
keywordMessage returns [KeywordMessage item]
: kh+=keywordHead fs+=formula ((kh+=keywordHead fs+=formula)+ | (kt+=keywordTail fs+=formula)+ | ) ;

messageCascade : Cascade m=message ;
message returns [Message item = null]
: kmsg=keywordMessage   # keywordSelection
| bmsg=binaryMessage    # binarySelection
| umsg=unarySelector    # unarySelection
;

//==================================================================================================
// notations
//==================================================================================================

classHeritage : Nil | superClass=signedType ;
typeHeritage  : Nil | superTypes+=detailedType ( Comma superTypes+=detailedType )* ;
detailedSignature returns [TypeList list = null] : ( generics=genericTypes )? ( Exit exit=detailedType )? ;

notations : ( notes+=notation )* ;
notation returns [KeywordNote item = null]
: At name=globalName ( ( | ( values+=namedValue )+ | ( nakeds+=nakedValue )+ ) Bang )? ;

namedValue returns [NamedValue item = null]
: head=keywordHead v=primitive  # namedPrim
| head=keywordHead g=globalName # namedGlobal
;

nakedValue returns [NamedValue item = null]
: tail=keywordTail v=primitive  # nakedPrim
| tail=keywordTail g=globalName # nakedGlobal
;

typeNotes : ( types+=detailedType Bang )* ;
detailedType returns [DetailedType item = null]
: extent=extendType # extentItem | signed=signedType # signedItem ;

typeNotation returns [DetailedType item = null] : ( | type=detailedType Bang ( etc=Etc )? ) ;
extendType   returns [DetailedType item = null] : g=globalName Extends baseType=detailedType ;
signedType   returns [DetailedType item = null] : g=globalReference details=detailedSignature ;
genericTypes returns [TypeList list = null]     : Quest types+=detailedType ( keywordTail types+=detailedType )* ;

//==================================================================================================
// references
//==================================================================================================

literalNil     : Nil   ;
literalSelf    : Self  ;
literalSuper   : Super ;
literalBoolean : True | False ;

variableName    returns [String name = Empty] : v=LocalName ;
globalName      returns [String name = Empty] : g=GlobalName ;
globalReference returns [Global item = null]  : ( names+=globalName )+ ;
valueName       returns [String name = Empty] : v=variableName # varValue | g=globalName # globalValue ;

//==================================================================================================
// keywords
//==================================================================================================

subclassKeyword   : Subclass ;
subtypeKeyword    : Subtype  ;
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

keywordTail returns [String selector = Empty] : tail=KeywordTail ;
keywordHead returns [String selector = Empty] : head=KeywordHead # headText | word=reservedWord # wordText ;

//==================================================================================================
// selectors
//==================================================================================================

metaUnary  : classUnary | typeUnary | ;
classUnary : ClassUnary ;
typeUnary  : TypeUnary ;

unarySelector  returns [String selector = Empty] : ( s=LocalName | s=ClassUnary | s=TypeUnary | s=GlobalName ) ;
binaryOperator returns [Operator op] : ( s=At | s=Bar | s=Comma | s=BinaryOperator | s=Usage ) ;

//==================================================================================================
// constants
//==================================================================================================

primitiveValues returns [LiteralArray list] : Pound TermInit ( array+=primitive )*    TermExit ;
elementValues   returns [LiteralArray list] : Pound TermInit ( array+=elementValue )* TermExit ;

elementValue returns [Constant item]
: lit=literal             # literalValue
| var=variableName        # variableValue
;

primitive returns [Constant item]
: array=primitiveValues   # primArray
| bool=literalBoolean     # primBool
| value=ConstantCharacter # primChar
| value=ConstantInteger   # primInt
| value=ConstantFloat     # primFloat
| value=ConstantSymbol    # primSymbol
| value=ConstantString    # primString
;

selfish returns [Constant item]
: refSelf=literalSelf     # selfSelfish
| refSuper=literalSuper   # superSelfish
;

literal returns [Constant item]
: array=elementValues     # arrayLiteral
| refNil=literalNil       # nilLiteral
| refSelf=literalSelf     # selfLiteral
| refSuper=literalSuper   # superLiteral
| bool=literalBoolean     # boolLiteral
| value=ConstantCharacter # charLiteral
| value=ConstantDecimal   # decimalLiteral
| value=ConstantFloat     # floatLiteral
| value=ConstantInteger   # intLiteral
| n=radixedNumber         # numLiteral
| value=ConstantSymbol    # symbolLiteral
| value=ConstantString    # stringLiteral
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
