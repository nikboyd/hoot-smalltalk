package Hoot.Runtime;

import org.junit.Test;
import static org.junit.Assert.*;
import Hoot.Runtime.Names.Primitive;
import Hoot.Runtime.Faces.Logging;

/**
 * Confirms proper operation of fractions.
 * @author Nik Boyd <nik.boyd@educery.dev>
 */
public class FractionTest implements Logging {

    @Test public void decimals() {
        Long[] values = Primitive.parseDecimal("0.0s4");
        report(Primitive.printDecimal(values[0], values[1], values[2]));
        values = Primitive.parseDecimal("3.14159265s9");
        report(Primitive.printDecimal(values[0], values[1], values[2]));
    }

    @Test public void rationalTest() {
        Primitive.rationalize(0, 1);
        Primitive.rationalize(1, 1);
    }

    @Test public void fractionTest() {
        Primitive.fractionalize(0, 1);
        Primitive.fractionalize(1, 1);
        Primitive.fractionalize(3, 2);
        Primitive.fractionalize(2, 3);
        Primitive.fractionalize(2.5678, 4);
    }

    @Test public void constants() {
        Primitive.radiansPerDegree();
        Primitive.degreesPerRadian();

        Primitive.negativeUnity();
        Primitive.maximumPriority();
        Primitive.minimumPriority();
        Primitive.normalPriority();

        Primitive.elementaryFalse();
        Primitive.elementaryTrue();
        Primitive.elementaryMinInteger();
        Primitive.elementaryMaxInteger();
        Primitive.elementaryMinShort();
        Primitive.elementaryMaxShort();
        Primitive.elementaryMinLong();
        Primitive.elementaryMaxLong();
        Primitive.elementaryMinFloat();
        Primitive.elementaryMaxFloat();
        Primitive.elementaryMinDouble();
        Primitive.elementaryMaxDouble();
    }

} // FractionTest
