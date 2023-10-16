package Hoot.Runtime.Faces;

import java.io.FileWriter;
import java.io.PrintWriter;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.stringtemplate.v4.AutoIndentWriter;
import static Hoot.Runtime.Functions.Exceptional.*;
import Hoot.Runtime.Emissions.Emission;

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

    default void writeCode(Emission scope) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(tokenFile().targetFile()))) {
            scope.result().write(new AutoIndentWriter(writer));
        }
        catch (Exception ex) {
            error(ex.getMessage(), ex);
        }
    }

} // FileParser
