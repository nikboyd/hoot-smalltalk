package Hoot.Compiler.Constants;

import Hoot.Runtime.Emissions.*;
import Hoot.Runtime.Faces.Named;
import static Hoot.Runtime.Functions.Utils.hasOne;

/**
 * A named value.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class NamedValue extends Constant implements Named {

    public NamedValue() { super(); }
    public NamedValue(String name, Item value) { this(); this.name = name; this.value = value; }
    public static NamedValue with(String name, Item value) { return new NamedValue(name, value); }

    protected String name;
    @Override public String name() { return name; }

    protected Item value;
    public Item itemValue() { return hasOne(value)? value: LiteralString.with("", sourceLine); }
    public Scalar scalarValue() { return (Scalar)itemValue(); }

    @Override public Emission emitItem() {
        return (name().isEmpty()) ? itemValue().emitItem() : emitPair(name(), itemValue().emitItem()); }

} // NamedValue
