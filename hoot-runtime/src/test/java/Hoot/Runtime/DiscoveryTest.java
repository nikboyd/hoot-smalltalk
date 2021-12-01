package Hoot.Runtime;

import org.junit.*;
import Hoot.Runtime.Faces.*;
import static Hoot.Runtime.Maps.ClassPath.*;
import static Hoot.Runtime.Maps.Library.CurrentLib;
import static Hoot.Runtime.Names.Primitive.printLine;

/**
 * Confirms proper operation of artifact discovery.
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2020 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class DiscoveryTest implements Logging {

    static final String[] HootLibs = { "hoot-abstracts", "hoot-runtime", };
    @Test public void testDiscovery() {
        CurrentLib.clear();
        CurrentPath.mapLibs(HootLibs);
        printLine();
    }

} // DiscoveryTest
