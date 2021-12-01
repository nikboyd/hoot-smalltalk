//====================================================================
// Bistro.g - Bistro Grammar
//====================================================================

parser grammar Bistro;

options {
ASTLabelType = CommonTree;
tokenVocab = BistroLex;
output = AST;
k = 2;
}

@header {
package smalltalk.compiler;
import smalltalk.compiler.expression.*;
import smalltalk.compiler.constant.*;
import smalltalk.compiler.element.*;
import smalltalk.compiler.scope.*;
}

//====================================================================
// class source file structure
//====================================================================

compilationUnit : faceContext faceDefinition ;

protected faceDefinition : faceOptions superName faceDeclaration ;
protected faceContext : ( facePackage )?  ( importedFace )* ;
protected faceDeclaration : ( classDeclaration | typeDeclaration ) ;
protected facePackage : Package packageName Cascade ;
protected importedFace : Import importName Cascade ;

//====================================================================
// class definition structure
//====================================================================

protected classDeclaration : classSignature ( metaclassBlock )? classBlock ;
protected classSignature : Subclass className ( interfaces )? ;
protected interfaces : Implements ( interfaceName )+ ;
protected classBlock : Class classMembership ;

protected classMembership :
	NewBlock classMembers EndBlock |
	EmptyBlock
;

protected metaclassToken
@after {// descend into a metaclass scope
Scope.current = Scope.asFace().addMetaface();}
: Metaclass
;

protected metaclassBlock
@after {// ascend into a face scope
Scope.current = Scope.asFace().containerScope();}
: metaclassToken classMembership
;

protected classMembers : ( classMember )* ;
protected classMember :
	( variableOptions variableName ( variableType )? Assign ) => memberVariable |
	( variableOptions variableName ( variableType )? Period ) => memberVariable |
	memberMethod
;

protected handlerClass : innerFace Inner innerMembership ;

protected innerFace
@after {// create an inner class scope
Scope.current = new Face(Scope.asBlock());
Scope.asFace().baseName($innerFace.text);}
: faceName
;

protected innerMembership
@after {// build a nested class
Innard innard = new Innard(Scope.asFace());
// ascend 1 level from an inner class scope to a block scope
Scope.current = Scope.asFace().containerScope();
// push the inner class on the operand stack
Scope.current.operands().push(innard);}
: NewBlock classMembers EndBlock
;

//====================================================================
// type definition structure (interface)
//====================================================================

protected typeDeclaration : typeSignature ( metatypeBlock )? typeBlock ;
protected typeSignature : ( Comma superType )* Subtype typeName ;
protected typeBlock : Type typeMembership ;

protected typeMembership :
	NewBlock typeMembers EndBlock |
	EmptyBlock
;

protected metatypeToken
@after {// descend into a metatype scope
Scope.current = Scope.asFace().addMetaface();}
: Metatype
;

protected metatypeBlock
@after {// ascend into a face scope
Scope.current = Scope.asFace().containerScope();}
: metatypeToken typeMembership
;

protected typeMembers : ( typeMember )* ;
protected typeMember :
	( variableOptions variableName ( variableType )? Assign ) => memberVariable |
	( variableOptions variableName ( variableType )? Period ) => memberVariable |
	methodAbstract
;

//====================================================================
// variables
//====================================================================

protected memberVariable : variableOptions variable variableEnd ;
protected variable : memberVariableName ( memberVariableType )? ( variableInitialization )? ;
protected variableInitialization : variableAssign initialization ;

protected memberVariableName
@after {// consume optional modifiers and a name for a variable
Scope.asFace().currentLocal().modifiers(Scope.current.consumeOptions());
Scope.asFace().currentLocal().name($memberVariableName.text);
Scope.asFace().currentLocal().setLine($start.getLine());}
: variableName
;

protected memberVariableType
@after {// consume a type for a member variable
Scope.asFace().currentLocal().type($memberVariableType.text);}
: variableType
;

protected variableEnd
@after {// consume a local variable for a face
Scope.asFace().addLocal();}
: Period
;

protected variableAssign
@after {// create a method scope for parsing initial value
Scope.current = new Method(Scope.asFace());}
: Assign
;

protected initialization
@after {// consume an initial value for a variable
Operand value = Scope.current.operands().pop();
Scope.current = Scope.asMethod().containerScope();
Scope.asFace().currentLocal().value(value);}
: evaluation
;

//====================================================================
// methods
//====================================================================

protected memberMethod : methodPreparation methodCompletion ;
protected methodAbstract : methodPreparation methodSignature methodAbstraction ;
protected methodSignature : ( methodType )? methodPattern ( thrownExceptions )? ;
protected methodPattern : ( unaryPattern | binaryPattern | keywordPattern ) ;
protected method : methodSignature ( emptyMethod | methodBlock | primitiveBlock ) ;
protected thrownExceptions : Cascade throwsClause ;
protected throwsClause : Throws ( exceptionClass )+ ;

protected exceptionClass
@after {// consume the name of an exception thrown by the current method
Scope.asBlock().throwsException($exceptionClass.text);}
: faceName
;

protected methodPreparation
@after {// consume optional modifiers for a method
Method method = Scope.asFace().currentMethod();
method.modifiers(Scope.current.consumeOptions());
// descend 1 level from a face scope into a method scope
Scope.current = method;}
: methodOptions
;

protected methodCompletion
@after {// ascend 1 level from a method scope into a class scope
Scope.current = Scope.asMethod().containerScope();
Scope.asFace().addMethod();}
: method
;

protected methodAbstraction
@after {// ascend 1 level from a method scope into a type scope
Scope.current = Scope.asMethod().containerScope();
Scope.asFace().addMethod();}
: EmptyBlock
;

protected primitiveBlock
@after {// consume the source code for a primitive method
Scope.asMethod().code($primitiveBlock.text);}
: PrimitiveBlock
;

//====================================================================
// method signatures
//====================================================================

protected keywordPattern : keywordArgument ( keywordArguments | blockArguments )? ;
protected keywordArguments : ( keywordArgument )+ ;
protected keywordArgument : keywordMethodSelector argument ;
protected binaryPattern : binaryMethodSelector argument ;

protected unaryPattern
@after {// consume a unary selector for a method
Scope.asMethod().name($unaryPattern.text.trim());
Scope.asMethod().setLine($start.getLine());}
: unarySelector
;

protected binaryMethodSelector
@after {// consume a binary selector for a method
Scope.asMethod().name($binaryMethodSelector.text);
Scope.asMethod().setLine($start.getLine());}
: binarySelector
;

protected keywordMethodSelector
@after {// consume a keyword selector for a method
Scope.asMethod().name($keywordMethodSelector.text);
Scope.asMethod().setLine($start.getLine());}
: keyword
;

//====================================================================
// blocks
//====================================================================

protected emptyMethod
@after {// build an empty method
Message.endingBlock(Scope.asBlock());}
: EmptyBlock
;

protected methodBlock : NewBlock blockContents EndBlock ;
protected nestedBlock : newBlock nestedContents EndBlock | emptyBlock ;

protected newBlock
@after {// descend 1 level from a block scope into another block scope
Block block = new Block(Scope.asBlock());
Scope.current = block;}
: NewBlock
;

protected nestedContents
@after {// build a nested block
Nest nest = new Nest(Scope.asBlock());
// ascend 1 level from a block scope into another block scope
Scope.current = Scope.asBlock().containerScope();
// push the inner scope on the operand stack
Scope.current.operands().push(nest);}
: ( blockPattern )? blockContents
;

protected emptyBlock
@after {// builds an empty block
Block block = new Block(Scope.asBlock());
Message.endingBlock(block);
// build a nested block
Nest nest = new Nest(block);
// push the inner scope on the operand stack
Scope.current.operands().push(nest);}
: EmptyBlock
;

protected blockPattern :
	throwsClause tokenBar |
	( blockSignature | blockArguments ) ( thrownExceptions )? tokenBar
;

protected tokenBar : Or | Bang ;
protected blockSignature : blockType ( blockArguments )? ;
protected blockArguments : ( Colon argument )+ ;

protected argument
@after {// consume an argument for a block (or method) signature
Scope.asBlock().addArgument();}
: argumentName ( argumentType )?
;

//====================================================================
// statements
//====================================================================

protected blockContents :
(	( Exit ) => result |
	( EndBlock ) => blockEnd |
	expression ( ( Period ) => statementEnd blockContents | blockEnd )
);

protected blockEnd
@after {// process the end of a block scope
Message.endingBlock( Scope.asBlock() );
if (false) throw new RecognitionException();}
:
;

protected statementEnd
@after {// add the recent statement to the current block
Scope.asBlock().addStatement(Scope.current.operands().pop());}
: Period
;

protected result
@after {// add a method exit expression to the current block
Scope.asBlock().addStatement(Message.from(Selector.forExit, 1, Scope.asBlock()));
// mark for later optimization check
Scope.asBlock().exits(true);}
: Exit expression ( Period )?
;

//====================================================================
// assignments
//====================================================================

protected expression : ( ( assignment ) => assignation | evaluation ) ;
protected assignments : ( ( assignment ) => assignment assignments | ) ;

protected assignation
@after {// process any assignments on the current block stacks
Message.assignments(Scope.asBlock());}
: assignments evaluation
;

protected assignment
@after {// push an assignment onto a selector stack
Scope.current.selectors().push(Selector.forAssignment);}
: ( ( identifier variableType ) => initializedVariable | assignedVariable ) Assign
;

protected assignedVariable
@after {// force early resolution of the reference
Reference r = Reference.named($assignedVariable.text, Scope.current);
Scope.current.operands().push(r);
//if (r.needsLocalResolution()) r.resolveUndefined();
}
: scopedName
;

protected initializedVariable : assignedVariableName assignedVariableType ;

protected assignedVariableName
@after {// push an assigned variable reference onto an operand stack
Reference r = Reference.named($assignedVariableName.text, Scope.current);
Scope.current.operands().push(r);}
: identifier
;

protected assignedVariableType
@after {// consume a type for an assigned variable and
// define a local for an assigned variable in the current block
Reference r = (Reference) Scope.current.operands().peek();
Variable v = Variable.named(r.name(), $assignedVariableType.text, Scope.current);
Scope.asBlock().addLocal(v);}
: variableType
;

//====================================================================
// messages (method invocations)
//====================================================================

protected evaluation : primary ( messages cascades )? ;
protected formula : binaryOperand binaryMessage* ;
protected binaryOperand : primary unaryMessage* ;
protected message : unaryMessage | binaryMessage | keywordMessage ;
protected cascades : ( ( Cascade ) => cascadedMessages |  ) ;
protected cascadedMessages : firstCascade message ( Cascade message )* ;
protected keywordPhrase : headKeywordSelector formula ;
protected keywordPhrases : ( tailKeywordSelector formula )+ ;
protected extraPhrases : extraPhrase+ ;

protected messages :
	( keyword ) => keywordMessage |
	( binarySelector ) => binaryMessage+ keywordMessage? |
	( unarySelector ) => unaryMessage+ binaryMessage* keywordMessage?
;

protected firstCascade
@after {// consume a message and convert it to a message cascade
Operand top = Scope.current.operands().pop();
Cascade cascade = new Cascade(Scope.asBlock(), top);
Scope.current.operands().push(cascade);}
: Cascade
;

protected unaryMessage
@after {// consume a unary message and push it onto an operand stack
Selector selector = Selector.from($unaryMessage.text.trim());
Scope.current.operands().push(Message.from(selector, 1, Scope.asBlock()));}
: unarySelector
;

protected binaryMessage
@after {// consume a binary message and push it onto an operand stack
Selector selector = Scope.current.selectors().pop();
Scope.current.operands().push(Message.from(selector, 2, Scope.asBlock()));}
: binaryMessageSelector binaryOperand
;

protected keywordMessage
@after {// consume a keyword message and push it onto an operand stack
Selector selector = Scope.current.selectors().pop();
Scope.current.operands().push(Message.from(selector, Scope.asBlock()));}
: keywordPhrase ( keywordPhrases | extraPhrases )?
;

protected binaryMessageSelector
@after {// push a binary selector onto a selector stack
Selector selector = Selector.from($binaryMessageSelector.text);
Scope.current.selectors().push(selector);}
: binarySelector
;

protected headKeywordSelector
@after {// push a keyword selector onto a selector stack
Selector selector = Selector.from($headKeywordSelector.text);
Scope.current.selectors().push(selector);}
: keyword
;

protected tailKeywordSelector
@after {// append a keyword selector for a keyword message
Selector selector = Scope.current.selectors().pop();
selector.append($tailKeywordSelector.text);
Scope.current.selectors().push(selector);}
: keyword
;

protected extraPhrase
@after {// append an argument separator (anonymous keyword) for a keyword message
Selector selector = Scope.current.selectors().pop();
selector.append(Selector.Colon);
Scope.current.selectors().push(selector);}
: Colon formula
;

//====================================================================
// primaries
//====================================================================

protected primary : primaryVariable | nestedTerm | nestedBlock | literal ;
protected nestedTerm : NewTerm ( ( faceName Inner ) => handlerClass | expression ) EndTerm ;

protected primaryVariable
@after {// push a variableName onto an operand stack
String variableName = $primaryVariable.text.trim();
if (variableName.equals("nil"))
    Scope.current.operands().push(LiteralNil.from($start, Scope.current));
else if (variableName.equals("true") || variableName.equals("false"))
    Scope.current.operands().push(LiteralBoolean.from($start, Scope.current));
else
    Scope.current.operands().push(Reference.named($primaryVariable.text, Scope.current));
}
: ( variableName ) => variableName | namedPart
;

//====================================================================
// literals
//====================================================================

protected literal : ( Minus ) => negativeNumber | literalNumber | literalCharacter | literalCollection ;
protected literalCollection : literalString | literalSymbol | literalArray ;
protected literalArray : Pound newArray ( arrayLiteral )* endArray ;
protected literalNumber :
  scaledFloat | scaledDecimal | literalFloat | radixedInteger | scaledInteger | literalInteger
;

protected newArray
@after {// push a new literal array onto an operand stack
Scope.current.operands().push(new ObjectArray(Scope.current));}
: NewTerm
;

protected endArray
@after {// optimize a literal array on top of an operand stack (if possible)
ObjectArray array = (ObjectArray) Scope.current.operands().pop();
Scope.current.operands().push(array.optimized());}
: EndTerm
;

protected arrayLiteral
@after {// pop an array literal from an operand stack and
Constant literal = (Constant) Scope.current.operands().pop();
// add it to a literal array on top of an operand stack
ObjectArray array = (ObjectArray) Scope.current.operands().peek();
array.add(literal);}
: literal
;

protected literalCharacter
@after {// push a literal character onto an operand stack
Scope.current.operands().push(LiteralCharacter.from($start, Scope.current));}
: ConstantCharacter
;

protected negativeNumber
@after {// negate the top operand onto an operand stack
((LiteralNumber)Scope.current.operands().peek()).negate();}
: Minus literalNumber
;

protected literalInteger
@after {// push a literal integer onto an operand stack
Scope.current.operands().push(LiteralInteger.from($start, Scope.current));}
: ConstantInteger
;

protected radixedInteger
@after {// push a literal integer onto an operand stack
Scope.current.operands().push(LiteralInteger.from($start, Scope.current));}
: RadixedInteger
;

protected literalFloat
@after {// push a literal float onto an operand stack
Scope.current.operands().push(LiteralFloat.from($start, Scope.current));}
: ConstantFloat
;

protected scaledFloat
@after {// push a literal float onto an operand stack
Scope.current.operands().push(LiteralFloat.from($start, Scope.current));}
: ScaledFloat
;

protected scaledDecimal
@after {// push a literal fixed point decimal onto an operand stack
Scope.current.operands().push(LiteralDecimal.from($start, Scope.current));}
: ScaledDecimal
;

protected scaledInteger
@after {// push a literal fixed point decimal onto an operand stack
Scope.current.operands().push(LiteralDecimal.from($start, Scope.current));}
: ScaledInteger
;

protected literalSymbol
@after {// push a literal symbol onto an operand stack
Scope.current.operands().push(LiteralSymbol.from($start, Scope.current));}
: Symbol
;

protected literalString
@after {// push a literal string onto an operand stack
Scope.current.operands().push(LiteralString.from($start, Scope.current));}
: ConstantString
;

//====================================================================
// names + types
//====================================================================

protected methodType
@after {// consume the type for method
Scope.asMethod().type($methodType.text);}
: typeAnnotation
;

protected blockType
@after {// consume the type for a block
Scope.asBlock().type($blockType.text);}
: typeAnnotation
;

protected typeAnnotation
@after {// remove the outer parentheses for a type name
String typeName = $typeAnnotation.text;
$start.setText(typeName.substring(1, typeName.length() - 1));}
: namedType
;

protected packageName
@after {// consume the name for a package
Scope.asFile().namePackage($packageName.text);}
: ( Identifier | namedPart )
;

protected importName
@after {// consume an imported face name for a library
Scope.asFile().importFace($importName.text);}
: ( NamedPackage | namedPart )
;

protected superName
@after {// consume the name of a superclass or supertype
Scope.asFile().importCurrentPackage();
Scope.asFile().nameBase($superName.tree);}
: faceName
;

protected superType
@after {// consume the name of a supertype
Scope.asFile().nameSuper($superType.text.trim());}
: faceName
;

protected typeName
@after {// consume the name of a type
Scope.asFile().nameSubtype($typeName.text.trim());
Scope.current = Scope.asFile().faceScope();}
: Identifier
;

protected className
@after {// consume the name of a class
Scope.asFile().nameSubclass($className.text.trim());
Scope.current = Scope.asFile().faceScope();}
: Identifier
;

protected interfaceName
@after {// consume an interface implemented by a class
Scope.asFace().implementsInterface($interfaceName.text);}
: faceName
;

protected argumentName
@after {// consume the name of an argument
Scope.asBlock().currentArgument().name($argumentName.text);}
: Identifier
;

protected argumentType
@after {// consume the type of an argument
Scope.asBlock().currentArgument().type($argumentType.text);}
: typeAnnotation
;

//====================================================================
// selectors + identifiers
//====================================================================

protected unarySelector : identifier ;
protected binarySelector : BinarySelector | Minus | Comma | Or ;
protected faceName     : ( identifier | namedPart ) ;
protected variableName : scopedName ;
protected variableType : typeAnnotation ;

protected scopedName : ( partName ) => partName | identifier ;
protected partName   : PartName ;
protected identifier : Identifier ;
protected namedPart  : NamedPart ;
protected namedType  : ( NamedType | PartType ) ;

protected keyword :
 Keyword | Package | Import | Implements |
 Subclass | Metaclass | Class |
 Subtype | Metatype | Type
;

//====================================================================
// options + reserved words
//====================================================================

protected extendOption : ( abstractOption | finalOption ) ;

protected variableOptions :
	( accessOption )? ( staticOption )? ( finalOption )? ;

protected methodOptions   :
	( accessOption )? ( staticOption | wrappedOption )? ( abstractOption | concreteOptions ) ;

protected concreteOptions :
	( finalOption )? ( synchronizedOption )? ( nativeOption )? ;

protected faceOptions
@after {// consume optional modifiers for a face (class or interface)
Scope.asFile().faceScope().modifiers(Scope.current.consumeOptions());}
: ( accessOption )? ( extendOption )?
;

protected accessOption
@after {// preserve an optional access modifier
Scope.current.addOption($accessOption.text.trim());}
: ( Public | Protected | Private )
;

protected staticOption
@after {// preserve an optional static modifier
Scope.current.addOption($staticOption.text.trim());}
: Static
;

protected abstractOption
@after {// preserve an optional abstract modifier
Scope.current.addOption($abstractOption.text.trim());}
: Abstract
;

protected finalOption
@after {// preserve an optional final modifier
Scope.current.addOption($finalOption.text.trim());}
: Final
;

protected nativeOption
@after {// preserve an optional native modifier
Scope.current.addOption($nativeOption.text.trim());}
: Native
;

protected synchronizedOption
@after {// preserve an optional synchronized modifier
Scope.current.addOption($synchronizedOption.text.trim());}
: Synchronized
;

protected wrappedOption
@after {// preserve an optional wrapped modifier
Scope.current.addOption($wrappedOption.text.trim());}
: Wrapped
;

//==================================================================================================
