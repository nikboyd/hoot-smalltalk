package Hoot.Runtime;

import java.util.*;
import org.junit.*;
import static org.junit.Assert.*;

import Hoot.Runtime.Behaviors.Mirror;
import Hoot.Runtime.Behaviors.Typified;
import Hoot.Runtime.Blocks.Enclosure;
import Hoot.Runtime.Faces.Logging;
import Hoot.Runtime.Faces.Valued;
import Hoot.Runtime.Names.Selector;
import static Hoot.Runtime.Functions.Utils.*;

/**
 * Confirms proper operation of closures.
 * @author nik <nikboyd@sonic.net>
 */
public class ClosureTest implements Logging {

    @Test public void sampleBlockZeroArgument() {
        Enclosure a = Enclosure.withBlock(f -> {
            report("honorable mention");
        });

        a.value();

        Enclosure c = Enclosure.withBlock(f -> {
            return Selector.named("awesome!");
        });
        List<Selector> results = wrap((Selector)c.value());
        report("count: " + results.size());
        results.forEach(s -> report(s.toString()));

        Selector[] unwrapped = { };
        unwrapped = unwrap(results, unwrapped);
        assertFalse(unwrapped == null);
        assertTrue(unwrapped.length == results.size());
    }

    @Test public void sampleBlockOneArgument() {
        String[] samples = { "sample", "text" };
        Enclosure c = Enclosure.withBlock(f -> {
            String s = f.getValue(0).name();
            return Selector.named(s);
        });
        List<Valued> results = c.evaluateEach(wrap(samples), Valued.class);
        report("count: " + results.size());
        results.forEach(s -> report(s.toString()));
    }

    @Test public void sampleBlockFromBehavior() {
        HashSet<Typified> result = emptySet(Typified.class);
        this.allSubclassesDo(Enclosure.withBlock(f -> {
            Typified each = f.getValue(0).value();
            result.add(each);
        }));

        report("eval = " + result.size());
    }

    private Object allSubclassesDo(Enclosure c) {
        c.evaluateWithEach(wrap(Mirror.forClass(getClass())));
        return this;
    }

} // ClosureTest
