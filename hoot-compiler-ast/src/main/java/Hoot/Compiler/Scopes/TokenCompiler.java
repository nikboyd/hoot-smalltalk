package Hoot.Compiler.Scopes;

import java.util.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import Hoot.Runtime.Names.Selector;
import Hoot.Runtime.Faces.UnitFile;
import Hoot.Runtime.Faces.FileParser;
import Hoot.Runtime.Faces.Logging;
import Hoot.Runtime.Faces.LanguageParser;
import Hoot.Runtime.Behaviors.MethodCall;
import static Hoot.Compiler.Notes.Comment.*;
import static Hoot.Runtime.Functions.Exceptional.*;
import static Hoot.Runtime.Functions.Utils.*;
import static Hoot.Runtime.Maps.Library.ChunkFileType;
import static Hoot.Runtime.Maps.Library.SourceFileType;

/**
 * Compiles a Hoot file from a token stream with a parser.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2025 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class TokenCompiler implements Logging, LanguageParser {

    static final HashMap<String, String> ParserTypes = new HashMap<>();
    static final HashMap<String, String> ListenerTypes = new HashMap<>();
    static {
        ParserTypes.put(SourceFileType, "Hoot.Compiler.Parser.HootFileParser");
        ParserTypes.put(ChunkFileType,  "Smalltalk.Compiler.Parser.SmalltalkFileParser");
        ListenerTypes.put(SourceFileType, "Hoot.Compiler.Parser.HootFileListener");
        ListenerTypes.put(ChunkFileType,  "Smalltalk.Compiler.Parser.SmalltalkFileListener");
    }

    Selector selectedParser(String fileType) { return Selector.named(ParserTypes.get(fileType)); }
    private FileParser createParser(String fileType) { return MethodCall.make(selectedParser(fileType)); }

    Selector selectedListener(String fileType) { return Selector.named(ListenerTypes.get(fileType)); }
    private ParseTreeListener createListener(String fileType) { return MethodCall.make(selectedListener(fileType)); }
    @Override public ParseTreeListener listener() { return createListener(fileType()); }

    public TokenCompiler(File aFile) {
        this.tokenFile = aFile;
        this.fileParser = createParser(fileType());
    }

    public boolean compile() {
        java.io.File sourceFile = tokenFile().sourceFile();
        if (hasNo(sourceFile) || !sourceFile.exists()) { reportMissing(); return false; }
        parseTokens(); if (wasParsed()) { emitCode(); return true; }
        return false; // should not arrive here
    }

    @Override public void parseTokens() { if (notParsed()) runLoudly(
        () -> { makeFileCurrent(); parseUnit(); }, () -> { popScope(); }); }

    void emitCode() { tokenFile().writeCode(); }
    void parseUnit() {
        reportParsing();
        parseTokenFile();
        captureComments();
        walkParsedResults();
    }

    UnitFile tokenFile;
    UnitFile tokenFile() { return tokenFile; }
    private String fileType() { return tokenFile().fileType(); }

    FileParser fileParser;
    FileParser fileParser() { return this.fileParser; }
    @Override public ParserRuleContext parseResult() { return fileParser().parseResult(); }
    void parseTokenFile() { fileParser().parseTokens(tokenFile()); }

    @Override public CommonTokenStream tokenStream() { return fileParser().tokenStream(); }
    public boolean wasParsed() { return fileParser().wasParsed(); }
    public boolean notParsed() { return fileParser().notParsed(); }

    void captureComments() { tokenFile().acceptComments(buildComments(fileParser().tokenStream())); }
    void makeFileCurrent() { tokenFile().makeCurrent(); }
    void popScope() { tokenFile().popScope(); }

    void reportParsing() { report("parsing " + tokenFile().sourceFile().getName()); }
    void reportMissing() { error("can't find source file for " + tokenFile().name()); }

} //  TokenCompiler
