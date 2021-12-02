package Hoot.Compiler.Constants;

import java.util.*;
import Hoot.Runtime.Emissions.*;
import static Hoot.Runtime.Names.Keyword.*;
import static Hoot.Runtime.Emissions.Emission.*;

/**
 * A source of literal emissions.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public interface LiteralSource extends EmissionSource {

    default Emission emitArrayPrim(List<Emission> values) { return emit("Array").values(values); }
    default Emission emitArray(List<Emission> values) { return emit("ArrayWith").values(values); }
    default Emission emitNewArray(List<Emission> values) { return emit("NewArray").values(values); }
    default Emission emitNamedValue(String name, Emission type) { return emit("NamedValue").name(name).type(type); }
    default Emission emitScalar(String s, String factory) { return emit("Scalar").value(s).with("factory", factory); }
    default Emission emitBoolean(String value) { return emit(value.equals("true") ? "True" : "False"); }
    default Emission emitInteger(String value) { return (value.length() > 9) ? emitLong(value) : emitSmall(value); }
    default Emission emitSmall(String value) { return emit(Integer).value(value); }
    default Emission emitLong(String value) { return emit("Long").value(value); }

} // LiteralSource
