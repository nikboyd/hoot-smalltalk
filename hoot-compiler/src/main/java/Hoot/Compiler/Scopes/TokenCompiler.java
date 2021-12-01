package Hoot.Compiler.Scopes;

import java.io.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import Hoot.Compiler.*;
import static Hoot.Compiler.HootParser.*;
import static Hoot.Compiler.Notes.Comment.*;
import static Hoot.Runtime.Functions.Exceptional.*;
import org.stringtemplate.v4.AutoIndentWriter;
import Hoot.Runtime.Faces.Logging;

/**
 * Compiles a Hoot file from a token stream with a parser.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 * @see <a href="https://gitlab.com/hoot-smalltalk/hoot-smalltalk/tree/master/LICENSE.txt">LICENSE for more details</a>
 */
public class TokenCompiler implements Logging {

    File tokenFile;
    public TokenCompiler(File aFile) { tokenFile = aFile; }
    private String tokenFilepath() { return tokenFile.sourceFile().getAbsolutePath(); }
    private CharStream createInputStream() throws Exception { return new ANTLRFileStream(tokenFilepath()); }
    private HootParser createParser() throws Exception { return new HootParser(createTokenStream()); }
    private TokenSource createLexer() throws Exception { return new HootLexer(createInputStream()); }
    private void popScope() { tokenFile.popScope(); }

    CommonTokenStream tokenStream;
    public CommonTokenStream tokenStream() { return tokenStream; }
    private CommonTokenStream tokenStream(CommonTokenStream stream) { this.tokenStream = stream; return tokenStream; }
    private TokenStream createTokenStream() throws Exception { return tokenStream(new CommonTokenStream(createLexer())); }

    public boolean compile() {
        java.io.File sourceFile = tokenFile.sourceFile();
        if (sourceFile == null || !sourceFile.exists()) {
            reportMissingSource();
            return false;
        }

        parseTokens();
        if (wasParsed()) {
            emitCode();
            return true;
        }
        else {
            return false;
        }
    }

    HootParser parser;
    public boolean wasParsed() { return parser != null; }
    public boolean notParsed() { return parser == null; }

    CompilationUnitContext resultUnit;
    private void parseUnit() throws Exception {
        tokenFile.makeCurrent();
        report("parsing " + tokenFile.name());

        parser = createParser();
        resultUnit = parser.compilationUnit();
        tokenFile.cacheComments(buildComments(tokenStream()));
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(new HootBaseListener(), resultUnit);
    }

    public void parseTokens() { if (wasParsed()) return;
        runLoudly(() -> { parseUnit(); }, () -> { popScope(); }); }

    private void emitCode() {
        tokenFile.clean();
        java.io.File targetFolder = tokenFile.facePackage().createTarget();
        if (targetFolder == null) return; // failure already reported

        report("writing " + tokenFile.targetFilename());
        emitCodeIn(new java.io.File(targetFolder, tokenFile.targetFilename()));
    }
    
    private void emitCodeIn(java.io.File targetFile) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(targetFile))) {
            tokenFile.emitScope().result().write(new AutoIndentWriter(writer));
        }
        catch (Exception ex) {
            error(ex.getMessage(), ex);
        }
    }

    private void reportMissingSource() {
        String message = "can't find source file for " + tokenFile.faceName();
        error(message);
    }

} //  TokenCompiler
