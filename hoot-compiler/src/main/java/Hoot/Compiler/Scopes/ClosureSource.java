package Hoot.Compiler.Scopes;

import java.util.*;
import Hoot.Runtime.Emissions.Emission;
import static Hoot.Runtime.Emissions.Emission.*;
import static Hoot.Runtime.Functions.Utils.*;

/**
 * A source of emissions for closures.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 * @see <a href="https://gitlab.com/hoot-smalltalk/hoot-smalltalk/tree/master/LICENSE.txt">LICENSE for more details</a>
 */
public interface ClosureSource extends ScopeSource {

    default Emission wrapErasedCall() { return emit("WrapErasedCall"); }
    default Emission emitClosureValue(Emission aBlock) { return emit("ClosureValue").with("closure", aBlock); }

    default Emission emitErasedVoid(String name, List<Emission> args) { 
        return emit("ErasedVoid").name(name).with("arguments", emitList(args)); }

    default Emission emitErasedCall(String name, List<Emission> args) {
        return emit("ErasedCall").name(name).with("arguments", emitList(args)) ; }
    
    default Emission emitErasedBlock(String name, List<Emission> args, Emission content) {
        return emit("ErasedBlock").name(name).with("arguments", emitList(args)).with("content", content); }

    default Emission emitOnlyTry(List<Emission> locals, Emission content) {
        return emit("OnlyTry").with("locals", locals).with("content", content); }

    default Emission emitOnlyCatch(List<Emission> args, List<Emission> locals, Emission content) {
        return emit("OnlyCatch").with("caught", args).with("locals", locals).with("content", content); }

    default Emission emitOnlyEnsure(List<Emission> locals, Emission content) {
        return emit("OnlyEnsure").with("locals", locals).with("content", content); }

    default Emission emitNewClosure(String type, int level, List<Emission> argNames) {
        return emit("NewClosure").with("level", emitItem(level+"")).with("closureType", type).with("argNames", emitList(argNames)); }

    default String closureType(boolean needsPredicate) { return needsPredicate ? "Predicate" : "Closure"; }

} // ClosureSource
