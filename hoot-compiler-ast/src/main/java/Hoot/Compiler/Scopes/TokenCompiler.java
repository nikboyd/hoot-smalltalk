package Hoot.Compiler.Scopes;

import org.antlr.v4.runtime.*;
import Hoot.Runtime.Faces.UnitFile;
import Hoot.Runtime.Faces.FileParser;
import Hoot.Runtime.Behaviors.Invoke;
import Hoot.Runtime.Names.Selector;
import Hoot.Runtime.Faces.Logging;
import static Hoot.Compiler.Notes.Comment.*;
import static Hoot.Runtime.Functions.Exceptional.*;
import static Hoot.Runtime.Functions.Utils.*;
import static Hoot.Runtime.Maps.Library.ChunkFileType;
import static Hoot.Runtime.Maps.Library.SourceFileType;
import java.util.HashMap;

/**
 * Compiles a Hoot file from a token stream with a parser.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class TokenCompiler implements Logging {

    static final HashMap<String, String> ParserTypes = new HashMap<>();
    static {
        ParserTypes.put(SourceFileType, "Hoot.Compiler.Parser.HootFileParser");
        ParserTypes.put(ChunkFileType,  "Smalltalk.Compiler.Parser.SmalltalkFileParser");
    }

    Selector selectedParser(String fileType) { return Selector.named(ParserTypes.get(fileType)); }
    private FileParser createParser(String fileType) {
        return Invoke.with().with(selectedParser(fileType)).call(); }

    public TokenCompiler(File aFile, String fileType) {
        this.tokenFile = aFile; this.fileParser = createParser(fileType); }

    public boolean compile() {
        java.io.File sourceFile = tokenFile().sourceFile();
        if (hasNo(sourceFile) || !sourceFile.exists()) { reportMissing(); return false; }
        parseTokens(); if (wasParsed()) { emitCode(); return true; }
        return false; // should not arrive here
    }

    public void parseTokens() { if (notParsed()) runLoudly(
        () -> { makeFileCurrent(); parseUnit(); }, () -> { popScope(); }); }

    void emitCode() { tokenFile().writeCode(); }
    void parseUnit() {
        reportParsing();
        fileParser().parseTokens(tokenFile);
        captureComments();
        fileParser().walkResult();
    }

    UnitFile tokenFile;
    UnitFile tokenFile() { return tokenFile; }

    FileParser fileParser;
    FileParser fileParser() { return this.fileParser; }

    public CommonTokenStream tokenStream() { return fileParser().tokenStream(); }
    public boolean wasParsed() { return fileParser().wasParsed(); }
    public boolean notParsed() { return fileParser().notParsed(); }

    void captureComments() { tokenFile().acceptComments(buildComments(fileParser().tokenStream())); }
    void makeFileCurrent() { tokenFile().makeCurrent(); }
    void popScope() { tokenFile().popScope(); }

    void reportParsing() { report("parsing " + tokenFile().sourceFile().getName()); }
    void reportMissing() { error("can't find source file for " + tokenFile().name()); }

} //  TokenCompiler
