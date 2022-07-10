package Hoot.Runtime.Faces;

/**
 * Defines protocols for parsing a language file.
 * @author Nik Boyd <nik.boyd@educery.dev>
 */
public interface FileParser extends LanguageParser {

    UnitFile tokenFile();
    void parseTokens(UnitFile aFile);

    default boolean wasParsed() { return !notParsed(); }
    default boolean notParsed() { return !wasParsed(); }

} // FileParser
