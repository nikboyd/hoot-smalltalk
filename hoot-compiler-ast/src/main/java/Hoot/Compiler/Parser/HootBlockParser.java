package Hoot.Compiler.Parser;

import java.io.*;
import java.util.*;
import java.nio.charset.Charset;
import org.apache.commons.io.IOUtils;
import org.antlr.v4.runtime.*;
import Hoot.Compiler.Scopes.*;
import Hoot.Compiler.Scopes.File;
import Hoot.Runtime.Faces.LanguageParser;
import Hoot.Compiler.Expressions.Statement;
import static Hoot.Runtime.Functions.Exceptional.*;
import static Hoot.Runtime.Functions.Utils.*;

/**
 * Parses Hoot code and provides the resulting AST and token stream.
 * @author Nik Boyd <nik.boyd@educery.dev>
 */
public class HootBlockParser implements LanguageParser {

    static public HootBlockParser create(String code) { return new HootBlockParser(code); }
    public HootBlockParser(String blockCode) { initialize(blockCode); }
    @Override public void parseTokens() { 
        parser = createParser();
        resultUnit = parser.compilationUnit(); }

    File fakeFile = new File("Hoot.Scripts", "Scripted");
    static final String[] NoNames = {};
    public String[] importedTypes() { return unwrap(selectedTypes(), NoNames); }
    List<String> selectedTypes() { // only non-static imports
        return map(select(fakeFile.faceImports(), 
            imp -> !imp.importsStatics()), imp -> imp.importedName()); }

    String fileCode;
    private void initialize(String blockCode) {
        fakeFile.makeCurrent();
        fakeFile.addStandardImports();
        fileCode = injectBlock(blockCode);
    }
    
    String injectBlock(String blockCode) { return String.format(loadScript(), blockCode); }
    public void parseClosure() {
        parseTokens();
        walkResult();
    }

    
    static final String HootFrame = "/script.template.txt";
    String loadScript() { return nullOrTryLoudly(() -> 
        IOUtils.resourceToString(HootFrame, Charset.forName("UTF-8"))); }
    
    Method m; // wait to initialize
    void prepareMethod() {
        fakeFile.makeCurrent();
        fakeFile.clean();
        m = fakeFile.face().methods().get(0);
        m.makeCurrent();
    }

    Statement getMethodStatement() { return m.content().statements().get(0); }
    String generatedCode() { return getMethodStatement().emitItem().render().trim().replace(";", ""); }
    public String compiledCode() {
        prepareMethod();
        try { return generatedCode(); }
        finally { m.popScope(); }
    }

    HootParser.CompilationUnitContext resultUnit;
    @Override public ParserRuleContext parseResult() { return resultUnit; }
    @Override public HootFileListener listener() { return new HootFileListener(); }

    CommonTokenStream tokenStream;
    @Override public CommonTokenStream tokenStream() { return tokenStream; }
    CommonTokenStream tokenStream(CommonTokenStream stream) {
        this.tokenStream = stream; return tokenStream(); }

    TokenSource createLexer() { return new HootLexer(createInputStream()); }
    TokenStream createTokenStream() { return tokenStream(new CommonTokenStream(createLexer())); }
    CharStream createInputStream() { return nullOrTryLoudly(() -> CharStreams.fromReader(blockReader())); }
    StringReader blockReader() { return new StringReader(fileCode); }

    HootParser parser;
    HootParser createParser() { return new HootParser(createTokenStream()); }
    public boolean wasParsed() { return parser != null; }
    public boolean notParsed() { return parser == null; }

} // HootBlockParser
