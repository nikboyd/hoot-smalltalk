package Hoot.Compiler.Scopes;

import java.io.*;
import java.util.*;
import org.codehaus.janino.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import Hoot.Compiler.HootLexer;
import Hoot.Compiler.HootParser;
import Hoot.Compiler.HootBaseListener;
import Hoot.Runtime.Faces.Logging;
import static Hoot.Compiler.HootParser.*;
import Hoot.Runtime.Behaviors.Scope;
import static Hoot.Runtime.Functions.Exceptional.*;
import static Hoot.Runtime.Functions.Utils.*;

/**
 * Compiles a Hoot closure from a closure token stream with a parser.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 * @see <a href="https://gitlab.com/hoot-smalltalk/hoot-smalltalk/tree/master/LICENSE.txt">LICENSE for more details</a>
 */
public class ClosureCompiler implements Logging {

    File fakeFile = new File("Hoot.Scripts", "Scripted");
    public ClosureCompiler(String closureCode) { this();  parseClosure(closureCode); }
    public ClosureCompiler() {
        fakeFile.makeCurrent();
        fakeFile.addStandardImports();
        fakeFile.faceScope().makeCurrent();
        Method m = new Method();
        m.makeCurrent();
        m.signature(KeywordSignature.emptyNiladic());
    }

    String blockCode;
    private StringReader blockReader() { return new StringReader(blockCode); }
    private CharStream createInputStream() throws Exception { return new ANTLRInputStream(blockReader()); }
    private HootParser createParser() throws Exception { return new HootParser(createTokenStream()); }
    private TokenSource createLexer() throws Exception { return new HootLexer(createInputStream()); }

    CommonTokenStream tokenStream;
    public CommonTokenStream tokenStream() { return tokenStream; }
    private CommonTokenStream tokenStream(CommonTokenStream stream) { this.tokenStream = stream; return tokenStream; }
    private TokenStream createTokenStream() throws Exception { return tokenStream(new CommonTokenStream(createLexer())); }

    HootParser parser;
    BlockScopeContext blockScope;
    private String compiledCode() { return blockScope.b.emitContents().render(); }
    public final void parseClosure(String blockCode) {
        this.blockCode = blockCode;
        runLoudly(() -> {
            parser = createParser();
            blockScope = parser.blockScope();
            ParseTreeWalker walker = new ParseTreeWalker();
            walker.walk(new HootBaseListener(), blockScope);
        });
    }

    static final Object[] NoArgs = { };
    public static Object evaluate(String code) { return new ClosureCompiler(code).evaluate(); }
    public Object evaluate() { return nullOrTryLoudly(() ->
        buildEvaluator().evaluate(NoArgs), () -> Scope.currentFile().popScope()); }

    static final String[] EmptyNames = {};
    private String[] importedTypes() { // only non-static imports
        List<String> results = map(select(fakeFile.faceImports(), imp -> !imp.importsStatics()), imp -> imp.importedName());
        return unwrap(results, EmptyNames); }

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
