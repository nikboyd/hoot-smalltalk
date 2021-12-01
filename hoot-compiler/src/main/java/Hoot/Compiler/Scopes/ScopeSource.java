package Hoot.Compiler.Scopes;

import java.util.*;
import Hoot.Runtime.Values.Variable;

import Hoot.Runtime.Emissions.*;
import static Hoot.Runtime.Emissions.Emission.*;

/**
 * Grafts scopes and their emissions onto items of different kinds.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 * @see <a href="https://gitlab.com/hoot-smalltalk/hoot-smalltalk/tree/master/LICENSE.txt">LICENSE for more details</a>
 */
public interface ScopeSource extends EmissionSource {

    default File file() { return File.from((Item)this); }
    default Face face() { return Face.from((Item)this); }
    default Variable variable() { return Variable.from((Item)this); }
    default Method method() { return Method.from((Item)this); }
    default Block block() { return Block.from((Item)this); }
    default Block parentBlock() { return Block.from(((Item)this).parentItem(Block.class)); }

    default Emission emitOptimizedBlock(Emission locals, int level, String type, Emission arguments, Emission content, List<Emission> argNames) {
        return emit("OptimizedBlock").with("locals", locals).with("argNames", emitList(argNames))
                .with("level", emitItem(level+"")).with("prior", (level-1)+"")
                .with("closureType", type).with("arguments", arguments).with("content", content); }

    default Emission emitBlockSignature(String name, Emission erasure, Emission arguments, Emission exceptions) {
        return emit("BlockSignature").name(name).with("erasure", erasure)
                .with("arguments", arguments).with("exceptions", exceptions); }

    default Emission emitMethodSignature(String name, Emission notes, Emission details, Emission type, List<Emission> arguments) {
        return emit("MethodSignature").name(name).notes(notes).details(details).type(type).with("arguments", arguments); }

    default Emission emitMethodScope(
            String text, List<Emission> locals, String name, Emission s, Emission c, String f, Emission con) {
        return emit("MethodScope").with("comment", text).with("locals", locals)
                .with("type", name).with("signature", s).with("content", c)
                .with("framed", f).with("construct", con); }

    default Emission emitMethodEmpty(String text, String name, Emission s) {
        return emit("MethodEmpty").with("comment", text).with("type", name).with("signature", s); }

    default Emission emitNilSubclassSignature(
            Emission notes, String name, Emission details, Emission faces) {
        return emit("NilSubclassSignature").with("notes", notes).with("className", name)
                .with("details", details).with("faces", faces); }

    default Emission emitClassSignature(
            Emission notes, String name, Emission details, Emission superClass, Emission faces) {
        return emit("ClassSignature").with("notes", notes).with("className", name)
                .with("details", details).with("superClass", superClass).with("faces", faces); }

    default Emission emitTypeSignature(Emission notes, String name, Emission details, Emission heritage) {
        return emit("TypeSignature").with("notes", notes).with("typeName", name)
                .with("details", details).with("superTypes", heritage); }

    default Emission emitLibraryScope(String notice, Emission pkgName, List<Emission> imports) {
        return emit("LibraryScope").with("notice", notice).with("packageName", pkgName).with("imports", imports); }

    default Emission emitLibraryType(Emission libs, Emission sig, List<Emission> locals, Emission metaFace, List<Emission> methods) {
        return emit("LibraryType")
                .with("libs", libs)
                .with("signature", sig)
                .with("metaFace", metaFace)
                .with("locals", locals)
                .with("methods", emitLines(methods));
    }

    default Emission emitMetaclass(String name, String superClass, Emission sig, List<Emission> locals, List<Emission> methods) {
        return emit("Metaclass")
                .with("className", name)
                .with("superClass", superClass)
                .with("signature", sig)
                .with("locals", locals)
                .with("methods", emitLines(methods));
    }

    default Emission emitMetatype(Emission sig, List<Emission> methods) {
        return emit("Metatype")
                .with("signature", sig)
                .with("methods", emitLines(methods));
    }

} // ScopeSource
