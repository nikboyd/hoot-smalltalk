package Hoot.Runtime.Values;

import Hoot.Runtime.Emissions.Emission;
import Hoot.Runtime.Emissions.EmissionSource;
import static Hoot.Runtime.Emissions.Emission.*;

/**
 * A source of value emissions.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public interface ValueSource extends EmissionSource {

    default Emission emitStackedBind(String name, String value) { return emit("StackedBind").name(name).value(value); }
    default Emission emitStackedBind(String name, Emission value) { return emit("StackedBind").name(name).value(value); }

    default Emission emitBlockArgument(String name, Emission type, int index, int level) {
        return emit("BlockArgument").name(name).with("type", type)
            .with("index", emitItem(index+"")).with("level", emitItem(level+"")); }

    default Emission emitArgument(String name, String type, boolean useFinal) {
        return emit("Argument").name(name).type(type).with("useFinal", useFinal ? emitEmpty() : null); }

    default Emission emitNamedArgument(String name, Emission type, Emission notes) {
        return emit("NamedArgument").name(name).type(type).with("notes", notes); }

    default Emission emitProperty(
            String name, Emission type, String className, Emission value, Emission notes, String comment) {
        return emit("Property").name(name).type(type)
                .with("upper", capitalize(name)).with("className", className)
                .with("notes", notes).value(value).comment(comment); }

    default Emission emitVariable(String name, Emission type, Emission value, Emission notes, String comment) {
        return emit("Variable").name(name).type(type).with("notes", notes).value(value).comment(comment); }

    default Emission emitTransient(String name, Emission type, Emission value, boolean needsCast) {
        return emit("TransientLocal").name(name).type(type).value(value).with("cast", needsCast ? type : emitEmpty()); }

} // ValueSource
