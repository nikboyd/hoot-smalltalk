package Hoot.Runtime;

import org.junit.Test;
import static org.junit.Assert.*;

import Hoot.Runtime.Behaviors.MethodCall;
import Hoot.Runtime.Blocks.Enclosure;
import Hoot.Runtime.Faces.IntegerValue;
import Hoot.Runtime.Faces.Logging;
import Hoot.Runtime.Faces.Valued;
import Hoot.Runtime.Names.Selector;
import Hoot.Runtime.Values.Frame;
import Hoot.Runtime.Values.Value;

/**
 * Confirms proper operation of stack frames.
 * @author nik <nikboyd@sonic.net>
 */
public class FrameTest implements Logging {

    @Test public void sampleFrame() {
        Frame f = new Frame();
        f.bind("sample", 5);
        report(f.getValue("sample").toString());

        f.with(Value.named("example"));
        report(f.getValue("example").toString());

        f.bind("example", 5);
        Value<Integer> v5 = f.getValue("example");
        report(v5.toString());
        Integer five = v5.value();

        Value v = f.getValue("example");
        report(v.toString());
        five = Value.as(Integer.class, v);
//        Value vi = v.asType(Integer.class);
//        five = Value.as(Integer.class, vi);

        f.bind("example", "test");
        report(f.getValue("example").toString());
        report(f.toString());

        {
            Frame f0 = new Frame();
            f0.push(5);
            ((Integer)f0.topValue()).toString();
        }
    }

    @Test public void sampleMethodCall() throws Throwable {
        Selector s = Selector.named("sampleCall");
        Frame f = new Frame();
        f.push(Value.self(this));
        f.push(Value.with('5'));
        f.push(Value.with(5));
        Object result = f.perform(s);
        assertFalse(result == null);
    }

    @Test public void sampleExit() {
        final String exitID = "sampleExit";
        Frame f0 = new Frame(exitID);
        Valued result = f0.evaluate(() -> {
            return samplePassThrough(Enclosure.withBlock(f1 -> {
                return (Valued)f0.exit(exitID, Selector.named("awesome!"));
            }));
        });

        report(result.toString());
    }

    public Valued samplePassThrough(Enclosure c) {
        final String exitID = "samplePassThrough";
        Frame f0 = new Frame(exitID);
        return f0.evaluate(() -> {
            report("before block call");
            return Enclosure.withBlock(f1 -> { return c.value(); }).value(c);
//            report("after call");
//            assertFalse("early closure return failed!", true);
        });
    }

    private static final String More = "more";
    private static final String Less = "less";
    @Test public void sampleBlockTwoArguments() throws Throwable {
        Frame frame = Frame.withValues(Value.named(More, 5), Value.named(Less, 2));

        IntegerValue r = frame.evaluate(Enclosure.withBlock(f -> {
            Integer less = f.getValue(Less).value();
            Integer more = f.getValue(More).value();
            return IntegerValue.with(less + more);
        }));
        report("eval = " + r);
    }

    @Test public void sampleStack() {
        Frame f = new Frame();
        f.push(5);
        f.push('5');
        report(Empty + f.pop());
        report(Empty + f.pop());

        f.push(5);
        f.push('5');
        sampleCall((Character)f.popValue(), (Integer)f.popValue());

        f.push(5);
        f.push('5');
        Value<?>[] vs = f.popReversed(2);
        sampleCall((Integer)vs[0].value(), (Character)vs[1].value());

        // x + y => push y, push x, pop x.plus( pop y )
        // x + y => ops.push(x), msgs.push(+), ops.push(y), s.push(ops.pop()), s.push(ops.pop()), s.pop().plus(s.pop())
    }

    @Test public void sampleInstance() throws Throwable {
        Selector c = Selector.named("Hoot.Runtime.Values.Frame");
        Class<?> type = c.toClass();
        assertFalse(type == null);

        Object result = MethodCall.make(c);
        assertFalse(result == null);
        report(result.getClass().getSimpleName());
    }

    @Test public void samplePerform() throws Throwable {
        Value[] values = { Value.self(this), Value.with('5'), Value.with(5) };
        Object result = MethodCall.with(values).call(Selector.named("sampleCall"));
        assertFalse(result == null);
        report(result.getClass().getSimpleName());
    }

    public Object sampleCall(Character c, Integer x) {
        report(c + " = " + x);
        Integer result = x + c;
        return result;
    }

    private void sampleCall(Integer x, Character c) {
        report(c + " = " + x);
    }

} // FrameTest
