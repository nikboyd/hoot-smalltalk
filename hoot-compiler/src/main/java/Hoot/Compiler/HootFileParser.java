package Hoot.Compiler;

import Hoot.Runtime.Faces.UnitFile;
import Hoot.Runtime.Faces.FileParser;
import static Hoot.Runtime.Functions.Exceptional.*;
import static Hoot.Runtime.Functions.Utils.*;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * Parses Hoot code and provides the resulting AST and token stream.
 * @author Nik Boyd <nik.boyd@educery.dev>
 */
public class HootFileParser implements FileParser {

    public HootFileParser() { }
    @Override public void parseTokens(UnitFile aFile) {
        tokenFile = aFile; source = createLexer(); parser = createParser(); resultUnit = parser.compilationUnit(); }

    HootParser.CompilationUnitContext resultUnit;
    @Override public ParserRuleContext parseResult() { return resultUnit; }
    @Override public ParseTreeListener listener() { return new HootBaseListener(); }

    CommonTokenStream tokenStream; // cached token stream
    @Override public CommonTokenStream tokenStream() { return tokenStream; }
    CommonTokenStream tokenStream(CommonTokenStream stream) { this.tokenStream = stream; return tokenStream(); }

    TokenSource source;
    TokenStream createTokenStream() { return tokenStream(new CommonTokenStream(source)); }
    TokenSource createLexer() { return new HootLexer(createInputStream()); }
    CharStream createInputStream() { return nullOrTryLoudly(() -> new ANTLRFileStream(tokenFilepath())); }

    UnitFile tokenFile;
    @Override public UnitFile tokenFile() { return tokenFile; }
    String tokenFilepath() { return tokenFile.sourceFile().getAbsolutePath(); }

    HootParser parser;
    HootParser createParser() { return new HootParser(createTokenStream()); }
    @Override public boolean wasParsed() { return hasOne(parser); }

} // HootLanguageParse
