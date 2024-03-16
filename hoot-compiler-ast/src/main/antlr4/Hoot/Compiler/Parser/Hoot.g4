//==================================================================================================
// Copyright 2010,2024 Nikolas S Boyd.
// Permission is granted to copy this work provided this copyright statement is retained in all copies.
//==================================================================================================

grammar Hoot; // resources/antlr4/Hoot/Compiler/Parser

//==================================================================================================
// file scopes
//==================================================================================================

compilationUnit : n=notations ( fileImport )* ( classScope | typeScope ) ;
classScope : sign=classSign ( scopes+=protocolScope )* ;
typeScope  : sign=typeSign  ( scopes+=protocolScope )* ;

fileImport : g=globalRefer ( c=caseOption )? m=important Period ;
important  : ImportOne | ImportAll | ImportStatics ;
caseOption : CaseMessage ;

typeSign  : h=typeHeritage  k=subtypeKeyword  n=notations              sub=globalName ds=detailedSign p=Period ;
classSign : h=classHeritage k=subclassKeyword n=notations ts=typeNotes sub=globalName ds=detailedSign p=Period ;

protocolSign  : n=notations g=globalName s=metaUnary k=membersKeyword ;
protocolScope : sign=protocolSign b=BlockInit ( members+=classMember )* x=BlockExit ;
classMember   : v=namedVar # varMember | m=methodScope # methodMember ;

//==================================================================================================
// methods + blocks
//==================================================================================================

methodBeg : BlockInit ;
methodEnd : BlockExit ;
methodScope
: n=notations sign=methodSign 
  b=methodBeg ( c=construct Period )? content=blockFill 
  x=methodEnd ;

methodSign
: ks=keywordSign # keywordSig
| bs=binarySign  # binarySig
| us=unarySign   # unarySig
;

namedArg    : n=notations type=typeNote v=varName ;
unarySign   : result=typeNote name=unarySelector ;
binarySign  : result=typeNote name=binaryOperator args+=namedArg ;
keywordSign : result=typeNote name=headsAndTails ;

headsAndTails
:  kh+=keywordHead args+=namedArg ( 
| (kh+=keywordHead args+=namedArg)+ 
| (kt+=keywordTail args+=namedArg)+ ) ;

blockScope : blockBeg sign=blockSign content=blockFill blockEnd ;
blockBeg : BlockInit ;
blockEnd : BlockExit ;

blockSign : ( | type=typeNote ( tails+=keywordTail args+=namedArg )+ Bar ) ;
blockFill : ( s+=statement p+=Period )* ( s+=statement ( p+=Period )? | r=exitResult | ) ;

//==================================================================================================
// values + messages
//==================================================================================================

evaluation : value=expression ;
exitResult : Exit value=expression ;
statement  : ( x=assignment | v=evaluation ) ;
construct  : ref=selfish ( tails+=keywordTail terms+=formula )* ;
assignment : n=notations type=typeNote v=valueName   Assign value=expression ;
namedVar   : n=notations type=typeNote v=valueName ( Assign value=expression )? p=Period ;

primary
: n=nestedTerm   # term
| b=blockScope   # block
| l=literal      # litValue
| g=globalRefer  # typeName
| v=varName      # variable
;

nestedTerm     : TermInit term=expression TermExit ;
expression     : f=formula ( kmsg=keywordMessage )? ( cmsgs+=messageCascade )* ;
formula        : s=unarySequence ( ops+=binaryMessage )* ;
unarySequence  : p=primary ( msgs+=unarySelector )* ;
binaryMessage  : operator=binaryOperator term=unarySequence ;
keywordMessage 
:  kh+=keywordHead fs+=formula ( 
| (kh+=keywordHead fs+=formula)+ 
| (kt+=keywordTail fs+=formula)+ ) ;

messageCascade : Cascade m=message ;
message
: kmsg=keywordMessage # keywordSelection
| bmsg=binaryMessage  # binarySelection
| umsg=unarySelector  # unarySelection
;

//==================================================================================================
// notations
//==================================================================================================

classHeritage : Nil | superClass=signedType ;
typeHeritage  : Nil | superTypes+=detailedType ( Comma superTypes+=detailedType )* ;
detailedSign  : ( generics=genericArgs )? ( Exit exit=detailedType )? ;

notations : ( notes+=notation )* ;
notation  :  At name=globalName ( ( 
| (values+=namedValue)+ 
| (nakeds+=nakedValue)+ ) Bang )? ;

namedValue
: head=keywordHead v=primitive  # namedPrim
| head=keywordHead g=globalName # namedGlobal
;

nakedValue
: tail=keywordTail v=primitive  # nakedPrim
| tail=keywordTail g=globalName # nakedGlobal
;

typeNotes : ( types+=detailedType Bang )* ;
typeNote  : ( | type=detailedType Bang ( etc=Etc )? ) ;

detailedType
: extent=extendType # extentItem 
| signed=signedType # signedItem ;

extendType  : g=globalName Extends baseType=detailedType ;
signedType  : g=globalRefer details=detailedSign ;
genericArgs : Quest types+=detailedType ( keywordTail types+=detailedType )* ;

//==================================================================================================
// references
//==================================================================================================

literalNil     : Nil   ;
literalSelf    : Self  ;
literalSuper   : Super ;
literalBoolean : True | False ;

varName     : v=LocalName ;
globalName  : g=GlobalName ;
globalRefer : ( names+=globalName )+ ;
valueName   : v=varName # varValue | g=globalName # globalValue ;

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

keywordTail : tail=KeywordTail ;
keywordHead : head=KeywordHead # headText | word=reservedWord # wordText ;

//==================================================================================================
// selectors
//==================================================================================================

metaUnary  : classUnary | typeUnary | ;
classUnary : ClassUnary ;
typeUnary  : TypeUnary ;

unarySelector  : ( s=LocalName | s=ClassUnary | s=TypeUnary | s=GlobalName ) ;
binaryOperator : ( s=At | s=Bar | s=Comma | s=BinaryOperator | s=Usage ) ;

//==================================================================================================
// constants
//==================================================================================================

primitiveValues : Pound TermInit ( array+=primitive )*    TermExit ;
elementValues   : Pound TermInit ( array+=elementValue )* TermExit ;

elementValue
: lit=literal # literalValue
| var=varName # variableValue
;

primitive
: array=primitiveValues   # primArray
| bool=literalBoolean     # primBool
| value=ConstantCharacter # primChar
| value=ConstantInteger   # primInt
| value=ConstantFloat     # primFloat
| value=ConstantSymbol    # primSymbol
| value=ConstantString    # primString
;

selfish
: refSelf=literalSelf     # selfSelfish
| refSuper=literalSuper   # superSelfish
;

literal
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
