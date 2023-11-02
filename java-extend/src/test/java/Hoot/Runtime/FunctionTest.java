package Hoot.Runtime;

import org.junit.Test;
import static org.junit.Assert.*;

import Hoot.Runtime.Faces.*;
import static Hoot.Runtime.Functions.Utils.*;

/**
 * Confirms proper operation of functional extensions.
 * @author Copyright 2010,2023 Nikolas S. Boyd. All rights reserved.
 * @author nik <nikboyd@sonic.net>
 */
public class FunctionTest implements Logging {

    @Test public void sampleList() {
        String[] listA = { "a", "b", "c", "d", "e", };
        String[] listB = { "a", "b", "c", "d", "e", };
        String[] listC = { "A", "B", "C", "D", "E", };
        String[] listD = { "A", "B", "C", };

        assertTrue(wrap(listA).equals(wrap(listA)));
        assertTrue(wrap(listA).equals(wrap(listB)));
        assertFalse(wrap(listA).equals(wrap(listC)));
        assertFalse(wrap(listA).equals(wrap(listD)));
        report("wrap tests passed");
    }

} // FunctionTest
