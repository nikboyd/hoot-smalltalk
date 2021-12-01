package Hoot.Compiler.Expressions;

import java.util.*;
import Hoot.Runtime.Emissions.*;
import static Hoot.Runtime.Emissions.Emission.*;

/**
 * A source of emissions for messages.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 * @see <a href="https://gitlab.com/hoot-smalltalk/hoot-smalltalk/tree/master/LICENSE.txt">LICENSE for more details</a>
 */
public interface MessageSource extends EmissionSource {

    default Emission emitTrueGuard(Emission value) { return emit("TrueGuard").value(value); }
    default Emission emitFalseGuard(Emission value) { return emit("FalseGuard").value(value); }
    default Emission emitClosureValue(Emission aBlock) { return emit("ClosureValue").with("closure", aBlock); }

    default Emission emitAlternatives(Emission condition, Emission trueBlock, Emission falseBlock) {
        return emit("Alternatives").with("condition", condition)
                .with("trueValue", trueBlock).with("falseValue", falseBlock); }

    default Emission emitGuardedBlock(Emission condition, Emission aBlock) {
        return emit("GuardedBlock").with("condition", condition).with("aBlock", aBlock); }

    default Emission emitGuardedPair(Emission condition, Emission trueBlock, Emission falseBlock) {
        return emit("GuardedPair").with("condition", condition)
                .with("trueValue", trueBlock).with("falseValue", falseBlock); }

    default Emission emitWhileLoop(Emission condition, Emission guardedBlock) {
        return emit("WhileLoop").with("condition", condition).with("guardedBlock", guardedBlock); }

    default Emission emitExclusively(String item, Emission term, Emission aBlock) {
        return emit(item).with("resource", term).with("block", aBlock); }

    default Emission emitExclusively(String item, Emission term, Emission type, Emission name, Emission aBlock) {
        return emit(item).with("resource", term).with("elementType", type).with("elementName", name).with("block", aBlock); }

    default Emission emitOperation(String operator, Emission argument) {
        return emit("Operation").with("operator", operator).with("argument", argument); }

    default Emission emitCall(String methodName, List<Emission> arguments) {
        return emit("Call").with("methodName", methodName).with("arguments", arguments); }

    default Emission emitPerform(Emission item, String name, Emission m, List<Emission> args) {
        return emit("Perform").with("operand", item).with("name", name).with("methodName", m).with("arguments", args); }

    default Emission emitConstruct(Emission reference, List<Emission> arguments) {
        return emit("Construct").with("reference", reference).with("arguments", arguments); }

    default Emission emitTypeNew(Emission className, List<Emission> arguments) {
        return emit("TypeNew").with("className", className).with("arguments", arguments); }

    default Emission emitNew(Emission className, List<Emission> arguments) {
        return emit("New").with("className", className).with("arguments", arguments); }

    default Emission emitNewArray(Emission className) {
        return emit("NewArray").with("className", className).with("size", emitItem("0")); }

    default Emission emitPath(String path) { return emit("Path").value(path); }
    default Emission emitThrow(Emission item) { return emit("Throw").with("item", item); }
    default Emission emitThrowSelf() { return emit("ThrowSelf"); }

    static final String TypeAsClass = "%s.class";
    static final String TypeAssigned = "%s %s =";
    default Emission emitAssignedCascades(List<Emission> messages, String className, String variableName) {
        return emitCascades(messages, className, format(TypeAssigned, className, variableName), null); }

    default Emission emitExitCascades(List<Emission> messages, String className, boolean isResult, boolean framed) {
        return emitCascades(messages, className, isResult ? "return" : null, framed ? "framed" : null); }

    default Emission emitSelfCascades(List<Emission> messages, String className, boolean isResult, boolean framed) {
        return emit("CascadesOnSelf").with("messages", messages)
            .with("resultType", className).with("exit",  isResult ? "return" : null).with("framed", framed ? "framed" : null); }

    default Emission emitCascades(List<Emission> messages, String typeName, String exit, String framed) {
        return emit("Cascades").with("messages", messages).with("resultType", typeName).with("exit", exit).with("framed", framed); }

} // MessageSource
