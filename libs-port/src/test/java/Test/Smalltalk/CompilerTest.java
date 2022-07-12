package Test.Smalltalk;

import org.junit.*;
import static org.junit.Assert.*;

import Hoot.Runtime.Maps.Library;
import Hoot.Runtime.Faces.Logging;
import Hoot.Runtime.Behaviors.Signed;
import Hoot.Runtime.Behaviors.Typified;
import static Hoot.Compiler.HootMain.*;
import static Hoot.Runtime.Functions.Utils.*;

/**
 *
 * @author Nik Boyd <nik.boyd@educery.dev>
 */
public class CompilerTest implements Logging {

    static final Object TestMain = mainly();
//    @Test public void helpSample() { main("--help"); report(Empty); }
    @Test public void sampleCompile() { main(buildSampleCommand()); }

    static final String Code = "../libs-port";
    String[] buildSampleCommand() { return buildCommand(Code, "-s", "src/main/hoot", "-l", ".st"); }

} // CompilerTest
