package Hoot.Runtime;

import java.util.*;
import org.junit.Test;
import static org.junit.Assert.*;

import Hoot.Runtime.Names.Operator;
import Hoot.Runtime.Names.Keyword;
import static Hoot.Runtime.Names.Keyword.*;
import static Hoot.Runtime.Functions.Utils.*;

/**
 * Confirms proper operation of names and keywords.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class KeywordTest {

    @Test
    public void operators() {
        assertTrue(Operator.with(null).methodName().equals(""));
        assertTrue(Operator.with("").methodName().equals(""));
        assertTrue(Operator.with("===").methodName().equals("isKindOf"));
        assertTrue(Operator.with(">>").methodName().equals("associateWith"));
    }

    @Test
    public void methodNames() {
        String none = null;
        List<String> noList = null;
        assertTrue(Keyword.with(none).methodName().equals(""));
        assertTrue(Keyword.with(noList).methodName().equals(""));
        assertTrue(Keyword.with("").methodName().equals(""));

        assertTrue(Keyword.with("do").methodName().equals(DoMessage));
        assertTrue(Keyword.with("do:").methodName().equals(DoMessage));
        assertTrue(Keyword.with("new").methodName().equals(NewMessage));
        assertTrue(Keyword.with("new:").methodName().equals(NewMessage));
        assertTrue(Keyword.with("class").methodName().equals(ClassMessage));
        assertTrue(Keyword.with("basicNew:").methodName().equals("basicNew"));

        String[] samples = { "with:" };
        assertTrue(Keyword.with(wrap(samples)).methodName().equals("with"));

        String[] keywords = { "to:", "by:", "do:" };
        assertTrue(Keyword.with(wrap(keywords)).methodName().equals("to_by_do"));

        String[] news = { "basicNew:", ":" };
        assertTrue(Keyword.with(wrap(news)).methodName().equals("basicNew"));
    }

} // KeywordTest
