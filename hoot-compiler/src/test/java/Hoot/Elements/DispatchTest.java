package Hoot.Elements;

import org.junit.Test;
import static org.junit.Assert.*;
import Hoot.Runtime.Faces.Logging;

/**
 * Confirms proper operation of interface dispatch.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class DispatchTest implements Logging {

    @Test public void samples() {
        assertTrue(Ample.with(4).lessThan(Sample.with(5)));
        assertFalse(Sample.with(5).lessThan(new Case()));
        assertTrue(new Case().lessThan(Sample.with(5)));
    }

    public static interface Ordered extends Logging {
        public Boolean lessThan(Ordered sample);
    }

    public static class Case implements Ordered {
        @Override public Boolean lessThan(Ordered value) {
            report(value);
            return !value.lessThan(this);
        }

        public void report(Ordered sample) {
            String message = "Case comparing " + this.getClass().getSimpleName();
            report(message + " < " + sample.getClass().getSimpleName());
        }
    }

    public static class Base implements Ordered {
        protected int value = 0;

        @Override public Boolean lessThan(Ordered value) {
            String message = "Base comparing " + this.getClass().getSimpleName();
            report(message + " < " + value.getClass().getSimpleName());
            return !value.lessThan(this);
        }

        public void report(Ordered sample) {
            String message = "comparing " + this.getClass().getSimpleName();
            report(message + " < " + sample.getClass().getSimpleName());
        }
    }

    public static class Sample extends Base {
        @Override public Boolean lessThan(Ordered aSample) {
            report(aSample);
            if (aSample instanceof Base) {
                Base candidate = (Base)aSample;
                return this.value < candidate.value;
            }

            return false;
        }

        public static Sample with(int value) {
            return new Sample(value);
        }

        private Sample(int value) {
            this.value = value;
        }
    }

    public static class Ample extends Base {

        public static Ample with(int value) {
            return new Ample(value);
        }

        private Ample(int value) {
            this.value = value;
        }
    }

} // DispatchTest
