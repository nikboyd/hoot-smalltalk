package Hoot.Runtime.Exceptions;

import Hoot.Runtime.Faces.Valued;

/**
 * An unhandled exception.
 * 
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 * @see <a href="https://gitlab.com/hoot-smalltalk/hoot-smalltalk/tree/master/LICENSE.txt">LICENSE for more details</a>
 */
public class UnhandledException extends ExceptionBase
{
  public static Metaclass type() { return (Metaclass)Metaclass.$class; }
  @Override public Metaclass $class() { return (Metaclass)Metaclass.$class; }
  public static class Metaclass extends ExceptionBase.Metaclass
  {
    static final UnhandledException.Metaclass $class = new UnhandledException.Metaclass();
    public Metaclass() { this(UnhandledException.Metaclass.class); }
    public Metaclass(java.lang.Class aClass) { super(aClass); }
    @Override public java.lang.Class outerClass() { return UnhandledException.class; }
    public void raise(final Valued anException) { new UnhandledException(anException).raise(); }
  }

  public UnhandledException(final Valued exception) { super(); this.exception = exception; }
  public void raise() { throw this; }

  public Valued unhandledException() { return exception; }
  protected Valued exception;
}