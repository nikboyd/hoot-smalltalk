//==================================================================================================
// Copyright 2010,2025 Nikolas S Boyd.
// Permission is granted to copy this work provided this copyright statement is retained in all copies.
//==================================================================================================

grammar Smalltalk; // libs-smalltalk/src/main/resources/antlr4/Smalltalk/Compiler/Parser

//==================================================================================================
// Smalltalk chunk file formats
// Requires:
//      single quotes around header comment, 
//      removal of comment and class declaration before class methods.
//==================================================================================================

compilationUnit : ( unitHeader?  cms+=methodReader )* ;

unitHeader    : ( filedHeader | classHeader ) ;
filedHeader   : fc=filedComment cs=classSignature ch=commentHeader ;
commentHeader : Bang x=expression Bang cc=headComment ;
filedComment  : fc=quotedString Bang ;
headComment   : hc=quotedString Bang ;

classHeader   : cc=codedComment ms=classSignature ;
codedComment  : cc=codeComment Bang ;

//==================================================================================================
// class scope
//==================================================================================================

classSignature : x=expression Bang ;
methodReader   : Bang pr=protoHeader Bang ms=methodScope Bang Bang ;
protoHeader    : p=expression ;

//==================================================================================================
// method scopes
//==================================================================================================

methodScope
: sign=methodSign 
  b=methodBeg ( vs=localVariables )? content=blockFill ;
//  x=methodEnd ;

methodBeg : cc+=codeComment* ;
//methodEnd : ;

methodSign
: ks=keywordSign # keywordSig
| bs=binarySign  # binarySig
| us=unarySign   # unarySig
;

unarySignature    : us=unarySelector ;
binarySignature   : bs=binaryOperator args+=argument ;
keywordSignature  : ks=headsAndTails ;

argument    : v=localName ;
unarySign   : name=unarySelector ;
binarySign  : name=binaryOperator args+=argument ;
keywordSign : name=headsAndTails ;

headsAndTails
:  kh+=keywordHead args+=argument ( 
| (kh+=keywordHead args+=argument)+ 
| ) ;

blockScope : blockBeg sign=blockSign content=blockFill blockEnd ;
blockBeg : BlockInit ;
blockEnd : BlockExit ;

blockSign : ( | ( tails+=keywordTail args+=argument )+ Bar ) ;
blockFill 
: ( s+=statement p+=Period cc+=codeComment* )* 
( s+=statement ( p+=Period cc+=codeComment* )? 
| r=exitResult             cc+=codeComment*
| ) ;

//==================================================================================================
// messages + statements
//==================================================================================================

evaluation : value=expression ;
exitResult : Exit cc+=codeComment* value=expression ;
statement  : ( n=assignment | v=evaluation ) ;
construct  : ref=selfish ( tails+=keywordTail terms+=formula )* ;
assignment : v=valueName Assign cc+=codeComment* value=expression ;

primary
: n=nestedTerm   # term
| b=blockScope   # block
| l=literal      # litValue
| g=globalRefer  # typeName
| v=localName    # variable
;

nestedTerm     : TermInit term=expression TermExit ;
expression     : f=formula ( kmsg=keywordMessage )? ( cmsgs+=messageCascade )* ;
formula        : s=unarySequence ( ops+=binaryMessage )* ;
unarySequence  : p=primary ( msgs+=unarySelector )* ;
binaryMessage  : operator=binaryOperator term=unarySequence ;
keywordMessage 
:  kh+=keywordHead fs+=formula ( 
| (kh+=keywordHead fs+=formula)+ 
| ) ;

messageCascade : Cascade m=message ;
message
: kmsg=keywordMessage # keywordSelection
| bmsg=binaryMessage  # binarySelection
| umsg=unarySelector  # unarySelection
;

//==================================================================================================
// selectors
//==================================================================================================

unarySelector  : ( s=LocalName ) ;
binaryOperator : ( s=BinaryOperator ) ;
keywordTail    : tail=KeywordTail ;
keywordHead    : head=KeywordHead # headText ;

//==================================================================================================
// references
//==================================================================================================

literalNil     : Nil   ;
literalSelf    : Self  ;
literalSuper   : Super ;
literalBoolean : True | False ;

localName   : v=LocalName ;
globalName  : g=GlobalName ;
globalRefer : ( names+=globalName )+ ;
valueName   : v=localName # localValue | g=globalName # globalValue ;

localVariables : Bar ( names+=localName cc+=codeComment? )* Bar ;

//==================================================================================================
// constants
//==================================================================================================

elementValues : Pound TermInit ( array+=elementValue )* TermExit ;

elementValue
: lit=literal   # literalValue
| var=localName # variableValue
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
| value=quotedString      # stringLiteral
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

quotedString : ConstantString ;
codeComment  : cc=CommentsCode ;

ConstantCharacter : '$' . ;
ConstantSymbol    : Pound SymbolString Dot? ;
ConstantString    : SingleString ConstantString? ;
CommentsCode      : CodeComment CommentsCode? ;

fragment CodeComment  : DoubleString -> channel(HIDDEN) ;
//fragment SingleString : SingleQuote ( NonQuote | DoubleString )* SingleQuote ;
//fragment DoubleString : DoubleQuote ( NonQuote | SingleString )* DoubleQuote ;
fragment SingleString : SingleQuote .*? SingleQuote ;
fragment DoubleString : DoubleQuote .*? DoubleQuote ;

fragment SymbolString // SingleString+ ??
: KeywordHead+
| BinaryOperator
| Name
;

fragment NonQuote     : ~['"] ;
fragment DoubleQuote  : '"' ;
fragment SingleQuote  : '\'' ;

//==================================================================================================
// punctuators
//==================================================================================================

banger : Bang ;

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
WhiteSpaces : WhiteSpace* -> skip ;
fragment WhiteSpace : [ \t\r\n\f] ;
fragment Blank : [ ] ;

//==================================================================================================
