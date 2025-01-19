package Hoot.Elements;

import org.junit.*;
import static org.junit.Assert.*;
import java.lang.reflect.Method;

import Hoot.Runtime.Maps.Package;
import Hoot.Runtime.Behaviors.Signed;
import Hoot.Runtime.Behaviors.Typified;
import Hoot.Runtime.Faces.Logging;
import Hoot.Runtime.Names.Signature;
import static Hoot.Runtime.Maps.Library.*;
import static Hoot.Runtime.Maps.ClassPath.*;

/**
 * Confirms proper operation of signatures.
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2025 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
//@Ignore
public class SignatureTest implements Logging {

    @BeforeClass public static void prepare() { CurrentLib.loadHootRuntimeLibs(); CurrentPath.println(2); }

//    @Test public void sigTest() {
//        String sig = "void:sigTest()";
//        String testPackage = getClass().getPackage().getName();
//        Package test = Package.named(testPackage);
//        Typified face = test.faceNamed(getClass().getSimpleName());
//        Signed s = face.getSigned(sig);
//        assertTrue(s.fullSignature().equals(sig));
//        whisper(s.fullSignature());
//    }

    @Test public void overrideTest() {
        String sig = "getSigned(java.lang.String)";
        Typified face = Package.named("Hoot.Runtime.Behaviors").faceNamed("Mirror");
        Typified type = Package.named("Hoot.Runtime.Behaviors").faceNamed("Typified");
        Signed fs = face.getSigned(sig);
        Signed ts = type.getSigned(sig);
        assertTrue(fs.overrides(ts));
        assertTrue(type.overridenBy(fs));
    }

    static final String ReflectReport = "%s -> %s";
    @Test public void reflectedTest() {
        String sig = "Hoot.Runtime.Faces.Signed:getSigned(java.lang.String)";
        Typified face = Package.named("Hoot.Runtime.Behaviors").faceNamed("Mirror");
        Typified type = Package.named("Hoot.Runtime.Behaviors").faceNamed("Typified");
        Method fm = face.typeMirror().methodSigned(sig);
        Method tm = type.typeMirror().methodSigned(sig);
        Class<?> fc = fm.getDeclaringClass();
        Class<?> tc = tm.getDeclaringClass();
        report(format(ReflectReport, fc.getCanonicalName(), tc.getCanonicalName()));
        assertTrue(Signature.from(fm).overrides(Signature.from(tm)));
        assertFalse(Signature.from(tm).overrides(Signature.from(tm)));
        assertFalse(Signature.from(tm).overrides(Signature.from(fm)));
    }

} // SignatureTest
