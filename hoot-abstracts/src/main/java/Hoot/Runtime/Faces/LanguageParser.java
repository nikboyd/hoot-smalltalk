package Hoot.Runtime.Faces;

import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeListener;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

/**
 * Defines protocols for parsing and walking parse results.
 * @author Nik Boyd <nik.boyd@educery.dev>
 */
public interface LanguageParser extends Logging {

    /**
     * A parser factory.
     */
    public static interface Factory {

        LanguageParser createParser();

    } // Factory

    default void parseTokens() {}
    CommonTokenStream tokenStream();

    ParseTreeListener listener();
    ParserRuleContext parseResult();
    default void walkResult() { new ParseTreeWalker().walk(listener(), parseResult()); }

} // LanguageParser
