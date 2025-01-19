package Hoot.Compiler.Constants;

import Hoot.Runtime.Emissions.Emission;
import static Hoot.Runtime.Functions.Utils.*;
import static Hoot.Runtime.Emissions.Emission.*;

/**
 * A literal object array.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 1999,2025 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class ObjectArray extends LiteralArray {

    public ObjectArray() { super(); }

    public int size() { return values.size(); }
    public void add(Constant aConstant) { values.add(aConstant); }
    public LiteralArray optimized() { return this; }

    @Override public Emission emitOperand() {
        return emit("NewArray").values(map(values, v -> v.emitOperand())); }

} // ObjectArray
