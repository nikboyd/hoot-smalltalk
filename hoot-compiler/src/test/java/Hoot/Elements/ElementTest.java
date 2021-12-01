package Hoot.Elements;

import java.util.*;
import org.junit.*;
import java.lang.reflect.*;

import Hoot.Compiler.HootMain;
import Hoot.Compiler.Scopes.Face;
import Hoot.Runtime.Faces.Logging;
import Hoot.Runtime.Behaviors.Typified;
import static Hoot.Runtime.Maps.Library.*;
import static Hoot.Runtime.Maps.ClassPath.*;

/**
 * Confirms proper operation of certain essential elements.
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 * @see <a href="https://gitlab.com/hoot-smalltalk/hoot-smalltalk/tree/master/LICENSE.txt">LICENSE for more details</a>
 */
@Ignore
public class ElementTest implements Logging {

    @BeforeClass public static void prepare() { HootMain.mainly();
        CurrentLib.loadHootCompilerLibs(); CurrentPath.println(2); }

    @Test public void variableTest() {
        Typified sample = Face.from(Face.class);

        report(sample.fullName());
        report(sample.instanceFields());
        report(Empty);
        report(sample.staticFields());

//        report(Empty);
//        Map<String, Field> map = sample.staticFields();
//        Variable v = Variable.from(map.get("ClassType"));
//        report(v.description());
//        report(markup().with(v).injectValues().trim());
    }

    static final String FieldReport = "  %s: %s";
    private void report(Map<String, Field> fieldMap) {
        fieldMap.forEach((name,field) -> {
            report(format(FieldReport, name, field.getType().getCanonicalName()));
        });
    }

} // ElementTest
