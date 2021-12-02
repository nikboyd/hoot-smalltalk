package Hoot.Runtime.Exceptions;

import Hoot.Runtime.Faces.Valued;
import Hoot.Runtime.Behaviors.Mirror;
import Hoot.Runtime.Behaviors.Typified;

/**
 * An abstract base (adapter) exception class.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public abstract class ExceptionBase extends RuntimeException implements Valued {

    // Java root exception classes
    public static final String[] RootExceptions = { "Throwable", "Exception", "RuntimeException", };

    protected ExceptionBase() { super(); }
    protected ExceptionBase(String message) { super(message); }
    public Metaclass $class() { return (Metaclass)Metaclass.$class; }
    public static Metaclass type() { return (Metaclass)Metaclass.$class; }
    public static void raiseNullPointer() { throw new NullPointerException(); }

    /**
     * Base meta-class for classes derived from ExceptionBase.
     */
    public static class Metaclass implements Typified {
        static final ExceptionBase.Metaclass $class = new ExceptionBase.Metaclass();
        public Metaclass() { this(ExceptionBase.Metaclass.class); }
        public Metaclass(java.lang.Class aClass) { }
        public void initialize() { }

        @Override public Typified $class() { return $class; }
        @Override public Typified superclass() { return Mirror.forClass(RuntimeException.class); }
        @Override public Class<?> primitiveClass() { return ExceptionBase.Metaclass.class; }

    } // Metaclass

} // ExceptionBase
