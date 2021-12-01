package Hoot.Runtime.Values;

import Hoot.Runtime.Faces.Valued;

/**
 * A method exit with a value.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 * @see <a href="https://gitlab.com/hoot-smalltalk/hoot-smalltalk/tree/master/LICENSE.txt">LICENSE for more details</a>
 */
public class Exit extends RuntimeException implements Valued {

    protected Exit(String scope, Object value) { this.value = value; this.scope = scope; }
    public static <R> Exit with(String scope, R value) { return new Exit(scope, value); }

    protected String scope = "";
    public String scope() { return this.scope; }
    public boolean exits(Frame f) { return this.scope.equals(f.scope()); }

    @SuppressWarnings("unchecked")
    @Override public <R> R value() { return (R)this.value; }
    protected Object value = null;

    public static final String FrameIdType = "java.lang.String";
    public static final String FrameId = "exitID";

} // Exit
