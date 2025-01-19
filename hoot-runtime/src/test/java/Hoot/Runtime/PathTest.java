package Hoot.Runtime;

import java.util.*;
import org.junit.*;

import Hoot.Runtime.Maps.Package;
import Hoot.Runtime.Behaviors.Mirror;
import Hoot.Runtime.Behaviors.Typified;
import static Hoot.Runtime.Functions.Utils.*;
import static Hoot.Runtime.Behaviors.Typified.*;
import static Hoot.Runtime.Maps.ClassPath.*;
import static Hoot.Runtime.Maps.Library.*;
import Hoot.Runtime.Faces.Logging;

/**
 * Confirms proper operation of path loading.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2025 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class PathTest implements Logging {

    @BeforeClass public static void prepare() { CurrentLib.loadHootRuntimeLibs(); CurrentPath.println(2); }

    @Test public void loadPaths() {
        CurrentLib.reportPackagedFaces();

        report("");
        report("reflecting");
        String testPackage = getClass().getPackage().getName();
        Package test = Package.named(testPackage);
        test.loadFace(getClass().getSimpleName());
        reflectOn(test);

        // confirm that a Mirror can be replaced
        test.addFace(new Submirror());
        report("");
        report("after replacing Submirror,");
        reflectOn(test);

        // enumerate types in Emissions
        String[] packageNames = {
            "Hoot.Runtime.Emissions",
            "Hoot.Runtime.Maps",
            "Hoot.Runtime.Names",
        };
        wrap(packageNames).forEach(name -> {
            Package p = Package.named(name);
            if (hasSome(p)) {
                p.reportReflectively();
            }
        });
    }

    static final String Reflection = "  %s reflects: %s";
    private void reflectOn(Package p) {
        report(p.fullName() + " has:");
        p.faceNames().forEach(n -> {
            Typified type = p.faceNamed(n);
            report(format(Reflection, type.name(), type.isReflective()+""));
        });
    }

    public static class Submirror extends Mirror {
        public Submirror() { this(PathTest.class); }
        public Submirror(Class<?> reflectedClass) { super(reflectedClass); }
        @Override public boolean isReflective() { return false; }

    } // Submirror

} // PathTest
