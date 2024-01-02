package Hoot.Runtime;

import org.junit.Test;
import static org.junit.Assert.*;

import Hoot.Runtime.Faces.*;
import Hoot.Runtime.Behaviors.*;
import Hoot.Runtime.Names.Selector;
import Hoot.Runtime.Values.Value;
import Hoot.Runtime.Values.Variable;
import static Hoot.Runtime.Functions.Utils.*;

/**
 * Confirms proper operation of named values.
 * @author Copyright 2010,2017 Nikolas S. Boyd. All rights reserved.
 * @author nik <nikboyd@sonic.net>
 */
public class ValueTest implements Logging {

    @Test public void sampleVariable() {
        Variable v = Variable.named("sample", null);
        v.emitOperand();
        v.emitVariable();
        v.emitArgument(true);
        v.emitArgumentType();
        v.emitType();
//        v.emitProperty();

        Scope s = new Scope(null) {};
        s.addLocal(v);
        s.localNamed("sample");
        s.locals().definedSymbols();
        s.locals().hasElementaryNames();
        s.locals().hasErasableTypes();
        s.locals().clean();
    }

    @Test public void sampleNumber() {
        Integer x = 5;
    }

    @Test public void sampleList() {
        String[] listA = { "a", "b", "c", "d", "e", };
        String[] listB = { "a", "b", "c", "d", "e", };
        String[] listC = { "A", "B", "C", "D", "E", };
        String[] listD = { "A", "B", "C", };

        assertTrue(wrap(listA).equals(wrap(listA)));
        assertTrue(wrap(listA).equals(wrap(listB)));
        assertFalse(wrap(listA).equals(wrap(listC)));
        assertFalse(wrap(listA).equals(wrap(listD)));
    }

    @Test public void sampleArray() throws Throwable {
        String[] sample = { "a", "b" };
        Value v = Value.with(sample);
        report(v.toString());
    }

    @Test public void sampleStatic() throws Throwable {
        Selector c = Selector.named("Hoot.Runtime.Names.Primitive");
        Selector m = Selector.named("elementaryTrue");
        Boolean result = MethodCall.withWrapped(c.toClass()).call(m);
        assertFalse(result == null);
        report(result.getClass().getSimpleName() + ": " + result);
    }

    public static interface SampleFace extends Valued {
        default boolean isNil() { return false; }
    }

    public static class SampleNil implements SampleFace {
        @Override public boolean isNil() { return true; }
    }

    public static interface SampleFake extends SampleFace {
        default boolean notNil() { return true; }
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testNil() throws Throwable {
        SampleFace nil = new SampleNil();
        assertTrue(nil.isNil());
        SampleFake fake = nil.as(SampleFake.class);
        assertTrue(fake.isNil());
        assertFalse(fake.notNil());
    }

} // ValueTest
