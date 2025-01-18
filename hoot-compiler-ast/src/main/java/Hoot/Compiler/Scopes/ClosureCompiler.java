package Hoot.Compiler.Scopes;

import org.codehaus.janino.*;
import Hoot.Runtime.Behaviors.Scope;
import Hoot.Compiler.Parser.HootBlockParser;
import static Hoot.Runtime.Functions.Exceptional.*;
import static Hoot.Runtime.Faces.Logging.*;
import Hoot.Runtime.Faces.Logging;

/**
 * Compiles a Hoot closure from a closure token stream with a parser.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class ClosureCompiler implements Logging {

    /**
     * Evaluate a block of code and return its result.
     * @param code a code block
     * @return a result
     */
    public static Object evaluate(String code) {
        return create(code).parseClosure().evaluate(); }

    static ClosureCompiler create(String code) { return new ClosureCompiler(code); }
    public ClosureCompiler(String code) { blockParser = HootBlockParser.create(code); }
    ClosureCompiler parseClosure() { parser().parseClosure(); return this; }

    public Object evaluate() {
        String javaCode = compiledCode();
        return isEmpty(javaCode)? null: evaluateJava(javaCode); }

    static final Object[] NoArgs = {};
    Object evaluateJava(String javaCode) {
        return nullOrTryLoudly(() -> 
            buildEvaluator(javaCode).evaluate(NoArgs), 
            () -> Scope.popFileScope()); }

    HootBlockParser blockParser;
    HootBlockParser parser() { return this.blockParser; }
    String compiledCode() { return parser().compiledCode().trim(); }

    Class<?> returnType = Object.class;
    // uses Janino - https://janino-compiler.github.io/janino/
    private ExpressionEvaluator buildEvaluator(String javaCode) throws Exception {
        ExpressionEvaluator result = new ExpressionEvaluator();
        result.setReturnType(returnType);
        result.setDefaultExpressionType(returnType);
        result.setDefaultImports(parser().importedTypes());
        result.setStaticMethod(true);
        result.cook(javaCode);
        return result;
    }

} // ClosureCompiler
