package Hoot.Compiler.Parser;

import java.io.StringReader;
import Hoot.Runtime.Faces.LanguageParser;
import static Hoot.Runtime.Functions.Exceptional.*;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * Parses Hoot code and provides the resulting AST and token stream.
 * @author Nik Boyd <nik.boyd@educery.dev>
 */
public class HootBlockParser implements LanguageParser {

    public HootBlockParser(String blockCode) {this.blockCode = blockCode; }
    @Override public void parseTokens() {
        source = createLexer(); parser = createParser(); blockScope = parser.blockScope(); }

    HootParser.BlockScopeContext blockScope;
    public HootParser.BlockScopeContext blockScope() { return this.blockScope; }
    @Override public ParserRuleContext parseResult() { return blockScope(); }
    @Override public ParseTreeListener listener() { return new HootFileListener(); }

    CommonTokenStream tokenStream;
    @Override public CommonTokenStream tokenStream() { return tokenStream; }
    CommonTokenStream tokenStream(CommonTokenStream stream) { this.tokenStream = stream; return tokenStream(); }

    TokenSource source;
    TokenSource createLexer() { return new HootLexer(createInputStream()); }
    TokenStream createTokenStream() { return tokenStream(new CommonTokenStream(source)); }
    CharStream createInputStream() { return nullOrTryLoudly(() -> CharStreams.fromReader(blockReader())); }
    StringReader blockReader() { return new StringReader(blockCode); }
    String blockCode;

    HootParser parser;
    HootParser createParser() { return new HootParser(createTokenStream()); }
    public boolean wasParsed() { return parser != null; }
    public boolean notParsed() { return parser == null; }

} // HootBlockParser
