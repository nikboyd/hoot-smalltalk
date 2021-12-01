package Hoot.Runtime.Emissions;

import java.util.*;
import Hoot.Runtime.Faces.Logging;
import static Hoot.Runtime.Functions.Utils.*;
import static Hoot.Runtime.Emissions.Emission.emit;

/**
 * A source of emissions, and common operations.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2019 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 * @see <a href="https://gitlab.com/hoot-smalltalk/hoot-smalltalk/tree/master/LICENSE.txt">LICENSE for more details</a>
 */
public interface EmissionSource extends Logging {
    
    // 0 = file level
    // 1 = face level = type, meta-type, class, meta-class
    // 2 = member level = variable, method
    // 2+n = block level = method level + block nest level
    default int nestLevel() { return 0; } // overriden where needed

    default Emission emitItem() { return emitEmpty(); }
    default Emission emitOptimized() { return emitEmpty(); }
    default Emission emitPrimitive() { return emitEmpty(); }

    default Emission emitEmpty() { return emit("Empty"); }
    default Emission emitObject() { return emit("Object"); }
    default Emission emitNull() { return emit("Null"); }
    default Emission emitNil() { return emit("Nil"); }
    default Emission emitSelf() { return emit("Self"); }
    default Emission emitSuper() { return emit("Super"); }

    default Emission emitItem(String value) { return emit("Item").item(value == null ? Empty : value); }
    default Emission emitSequence(List<Emission> items) { return emit("Sequence").items(items); }

    default Emission emitList(List<Emission> items) { return emit("List").items(items); }
    default Emission emitLines(List<Emission> items) { return emit("Lines").items(items); }

    default Emission emitMetaItem() { return null; } // override this
    default Emission emitItems(Emission... items) { return emitItems(wrap(items)); }
    default Emission emitItems(List<Emission> items) { return emit("Items").with("items", items); }
    default Emission emitSequence(Emission... items) { return emitSequence(wrap(items)); }
    default Emission emitSequenced(List<String> items) { return emit("Sequence").with("items", items); }
    default Emission emitStatement(Emission value) { return emit("Statement").value(value); }
    default Emission emitStatements(List<Emission> items, Emission value) {
        return emit("Statements").items(items).value(value); }

    default Emission emitQuoted(String value) { return emit("Quoted").value(value); }
    default Emission emitAnswer(Emission value) { return emit("Answer").value(value); }
    default Emission emitResult(Emission type, Emission value) { return emit("Result").type(type).value(value); }
    default Emission emitExit(Emission value, Emission type) { 
        return emit("Exit").type(type).value(value).with("level", (nestLevel()-1)+""); }

    default Emission emitExpression(Emission operand, Emission... messages) {
        return emitExpression(operand, wrap(messages)); }

    default Emission emitExpression(Emission operand, List<Emission> messages) {
        return emit("Expression").with("operand", operand).with("messages", messages); }

    default Emission emitCast(Emission typeName, Emission value) { return emit("Cast").type(typeName).value(value); }
    default Emission emitCast(String typeName, Emission value) { return emit("Cast").type(typeName).value(value); }

    default Emission emitTerm(Emission e) { return emit("Term").value(e.result()); }
    default Emission emitTerm(String value) { return emit("Term").value(value); }
    default Emission emitPair(String name, Emission value) { return emit("Pair").name(name).value(value); }

} // Source
