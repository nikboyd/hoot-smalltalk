package Hoot.Elements;

import org.junit.*;
import static org.junit.Assert.*;

import Hoot.Runtime.Maps.Library;
import Hoot.Runtime.Faces.Logging;
import Hoot.Runtime.Behaviors.Signed;
import Hoot.Runtime.Behaviors.Typified;
import static Hoot.Compiler.HootMain.*;
import static Hoot.Runtime.Functions.Utils.*;

/**
 * Compiles a few sample classes.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 * @see <a href="https://gitlab.com/hoot-smalltalk/hoot-smalltalk/tree/master/LICENSE.txt">LICENSE for more details</a>
 */
//@Ignore
public class CompilerTest implements Logging {

    static final Object TestMain = mainly();
    @Test public void helpSample() { main("--help"); report(Empty); }
    @Test public void sampleCompile() { main(buildSampleCommand()); sampleLookups(); }

    static final String Code = "../sample-code";
    String[] buildSampleCommand() { return buildCommand(Code, "-p", "*", "-s", "src/test/hoot"); }

    void sampleLookups() {
        Typified point = Library.findFace("Samples.Geometry.Point");
        Typified subject = Library.findFace("Samples.Core.Subject");
        if (hasOne(point)) {
            Signed ps = point.getSigned("equals(Subject)");
            Signed ss = subject.getSigned("equals(Subject)");
            assertTrue(ps.overrides(ss));
            assertTrue(ps.overridesHeritage());
        }
    }

} // CompilerTest
