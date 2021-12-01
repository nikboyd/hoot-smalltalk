package Hoot.Elements;

import org.junit.*;
import static org.junit.Assert.*;
import org.codehaus.janino.ScriptEvaluator;
import org.codehaus.janino.ExpressionEvaluator;
import Hoot.Runtime.Faces.Logging;

/**
 * Confirms proper operation of the evaluators.
 * @author nik <nikboyd@sonic.net>
 */
public class EvaluatorTest implements Logging {
    
    static final Object[] NoArgs = {};

    static final String EvalEquals = "%s %s = %s";
    static final String EvalFailed = "%s failed: %s != %s";
    @Test public void simpleMath() throws Exception {
        String expected = "7";
        String test = "3 + 4";
        ExpressionEvaluator ee = new ExpressionEvaluator(); ee.cook(test);
        String result = ee.evaluate(NoArgs).toString();
        assertTrue(format(EvalFailed, "simpleMath", test, expected), result.equals(expected));
        report(format(EvalEquals, "simpleMath", test, result));
    }
    
    @Test public void simpleScript() throws Exception {
        String expected = "7";
        String test = "{ int a = 3; int b = 4; return a + b; }";
        ScriptEvaluator s = new ScriptEvaluator(test, int.class);
        String result = s.evaluate(NoArgs).toString();
        assertTrue(format(EvalFailed, "simpleScript", test, expected), result.equals(expected));
        report(format(EvalEquals, "simpleScript", test, result));
    }

} // EvaluatorTest
