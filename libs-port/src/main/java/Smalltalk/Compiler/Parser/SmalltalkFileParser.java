package Smalltalk.Compiler.Parser;

import Hoot.Runtime.Faces.UnitFile;
import Hoot.Runtime.Faces.FileParser;
import static Hoot.Runtime.Functions.Utils.*;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * Parses Smalltalk code and provides the resulting AST and token stream.
 * @author Nik Boyd <nik.boyd@educery.dev>
 */
public class SmalltalkFileParser implements FileParser {
    
    public SmalltalkFileParser() { }
    @Override public void parseTokens(UnitFile aFile) { // order matters
        tokenFile = aFile; tokenStream = createTokenStream();
        parser = createParser(); resultUnit = parser().compilationUnit(); }

    SmalltalkParser.CompilationUnitContext resultUnit;
    @Override public ParserRuleContext parseResult() { return resultUnit; }
    @Override public ParseTreeListener listener() { return new SmalltalkFileListener(); }

    TokenSource createLexer() { return new SmalltalkLexer(createInputStream()); }
    SmalltalkParser createParser() { return new SmalltalkParser(tokenStream()); }
    CommonTokenStream createTokenStream() { return new CommonTokenStream(createLexer()); }

    @Override public CommonTokenStream tokenStream() { return tokenStream; }
    CommonTokenStream tokenStream; // cached token stream

    @Override public UnitFile tokenFile() { return tokenFile; }
    UnitFile tokenFile; // cached file

    @Override public boolean wasParsed() { return hasOne(parser()); }
    SmalltalkParser parser() { return parser; }
    SmalltalkParser parser; // cached parser

} // SmalltalkFileParser
