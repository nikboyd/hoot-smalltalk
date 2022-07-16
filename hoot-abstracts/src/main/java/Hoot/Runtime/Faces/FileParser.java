package Hoot.Runtime.Faces;

import static Hoot.Runtime.Functions.Exceptional.*;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;

/**
 * Defines protocols for parsing a language file.
 * @author Nik Boyd <nik.boyd@educery.dev>
 */
public interface FileParser extends LanguageParser {

    UnitFile tokenFile();
    void parseTokens(UnitFile aFile);

    default boolean wasParsed() { return !notParsed(); }
    default boolean notParsed() { return !wasParsed(); }

    default CharStream createInputStream() { return nullOrTryLoudly(() ->
        CharStreams.fromFileName(tokenFile().sourceFile().getAbsolutePath())); }

} // FileParser
