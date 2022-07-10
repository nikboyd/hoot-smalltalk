package Hoot.Compiler.Scopes;

import java.util.*;
import org.codehaus.janino.*;

import Hoot.Compiler.HootParser;
import Hoot.Runtime.Behaviors.Scope;
import Hoot.Compiler.HootBlockParser;
import static Hoot.Runtime.Functions.Exceptional.*;
import static Hoot.Runtime.Functions.Utils.*;
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
    public static Object evaluate(String code) { return new ClosureCompiler(code).parseClosure().evaluate(); }
    public ClosureCompiler(String closureCode) { makeScriptScope(closureCode); }

    Method m; // wait to initialize
    File fakeFile = new File("Hoot.Scripts", "Scripted");
    private void makeScriptScope(String code) {
        fakeFile.makeCurrent();
        fakeFile.addStandardImports();
        fakeFile.faceScope().makeCurrent();
        m = new Method(); // initialize with faked up scope
        m.makeCurrent(); // push upper scopes
        m.signature(KeywordSignature.emptyNiladic());
        blockParser = new HootBlockParser(code);
    }

    static final Object[] NoArgs = { };
    public Object evaluate() { return nullOrTryLoudly(() ->
        buildEvaluator().evaluate(NoArgs), () -> Scope.currentFile().popScope()); }

    HootBlockParser blockParser;
    ClosureCompiler parseClosure() { this.blockParser.parseTokens(); return this; }
    HootParser.BlockScopeContext blockScope() { return this.blockParser.blockScope(); }
    String compiledCode() { return blockScope().b.emitContents().render(); }

    static final String[] NoNames = {};
    private String[] importedTypes() { // only non-static imports
        List<String> results = map(select(fakeFile.faceImports(), imp -> !imp.importsStatics()), imp -> imp.importedName());
        return unwrap(results, NoNames); }

    String[] argNames = {};
    Class<?>[] argTypes = {};
    Class<?>[] thrownTypes = {};
    Class<?> returnType = Object.class;
    private ScriptEvaluator buildEvaluator() throws Exception {
        ScriptEvaluator result = new ScriptEvaluator();
        result.setReturnType(returnType);
        result.setDefaultImports(importedTypes());
        result.setParameters(argNames, argTypes);
        result.setThrownExceptions(thrownTypes);
        result.cook(compiledCode());
        return result;
    }

} // ClosureCompiler
