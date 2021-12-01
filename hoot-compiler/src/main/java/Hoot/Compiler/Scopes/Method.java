package Hoot.Compiler.Scopes;

import java.util.*;
import Hoot.Runtime.Emissions.*;
import Hoot.Runtime.Behaviors.*;
import Hoot.Runtime.Values.Variable;
import static Hoot.Runtime.Names.Name.*;
import static Hoot.Runtime.Functions.Utils.*;
import static Hoot.Runtime.Names.Keyword.Object;
import static Hoot.Runtime.Behaviors.HootRegistry.*;
import static Hoot.Runtime.Names.Signature.formatTerm;
import static Hoot.Runtime.Notes.Note.OverrideNote;
import static Hoot.Runtime.Names.Keyword.Colon;

import Hoot.Compiler.Expressions.*;

/**
 * A method.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 1999,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 * @see <a href="https://gitlab.com/hoot-smalltalk/hoot-smalltalk/tree/master/LICENSE.txt">LICENSE for more details</a>
 */
public class Method extends Block {

    public Method() { this(Face.currentFace()); }
    public Method(Face face) { super(face); this.content = new BlockContent(this); }

    @Override public void clean() { super.clean(); noteOverride(); }
    protected void noteOverride() { if (this.needsOverrideNote()) notes().note(OverrideNote); }
    public boolean needsOverrideNote() { return overridesHeritage() || matchesStandardOverride(); }

    @Override public Method makeCurrent() { super.makeCurrent(); Face.currentFace().addMethod(this); return this; }
    public static Method currentMethod() { return from(Scope.current()); }
    public static Method from(Item item) { return nullOr(m -> (Method)m, item.methodScope()); }
    public void construct(Construct c) { content().add(c); }

    @Override public String faceName() { return face().typeName(); }
    @Override public String description() { return isSigned() ? signature().description() : faceName() + ">>unknown"; }
    public String scopeID() { return Quote + description() + Quote; }

    @Override public void signature(BasicSignature s) {
        super.signature(s);
        this.sig.mergeDetails();

        addLocal(Variable.exitFrom(this, UnarySequence.exitFrom(this)));
        addLocal(Variable.frame(this, Expression.frameNew()));
    }

    public boolean isVoid() { return notes().isVoid() || signature().returnsVoid(); }

    @Override public boolean isMethod() { return true; }
    @Override public boolean isAbstract() { return (face().isInterface()) ? true : super.isAbstract(); }
    @Override public boolean isConstructor() { return name().equals(face().defaultName()) || Metaclass.equals(name()); }
    @Override public boolean isPrimitive() { return notes().isPrimitive(); }

    @Override public Method method() { return this; }
    @Override public String qualify(String identifier) {
        // will this method use an inner class for its block?
        return (this.hasLocals() ? super.qualify(identifier) : identifier); }


    static final String NewSignature = Object + ":$new";
    static final String InitSignature = "void:initialize";
    static final String[] OverrideSignatures = {
        "int:hashCode",
//        "boolean:equals",
        "void:printStackTrace",
    };

    public boolean matchesStandardOverride() {
        String shortSig = shortSignature();
        if (shortSig.equals(InitSignature)) return !BehaviorType().equals(face());
        return matchAny(wrap(OverrideSignatures), sig -> shortSig.startsWith(sig)); }

    @Override public String matchSignature() { return methodName() + formatTerm(argumentSignatures()); }
    @Override public String matchErasure() { return methodName() + formatTerm(argumentErasures()); }
    @Override public String shortSignature() { return signature().resolvedType().simpleName() + Colon + methodName(); }
    @Override public String fullSignature() { return shortSignature() + formatTerm(argumentSignatures()); }
    @Override public String erasedSignature() { return shortSignature() + formatTerm(argumentErasures()); }

    private List<String> argumentErasures() { return map(arguments(), arg -> Object); }
    private List<String> argumentSignatures() { return map(arguments(), arg -> arg.typeResolver().shortName()); }

    public Emission emitNotes() { return emitSequence(notes().methodNotesWithoutDecor()); }
    @Override public Emission emitScope() { return isAbstract() ? emitAbstract() : emitSimple(); }

    @Override public boolean needsFrame() {
        if (isEmpty()) return false;
        if (isPrimitive()) return false;
        if (isVoidedContext()) return false;

        return super.needsFrame();
    }

    private String emptyIf(boolean value) { return value ? Empty : null; }

    public List<Emission> emitExitVariables() {
        return mapList(
                locals().definedSymbols(),
                v -> v.isForExiting(),
                v -> emitStatement(v.emitLocal()));
    }

    public Emission emitConstruct() { return content().emitConstruct(); }

    @Override public Emission emitSignature() {
        if (signature() == null) return emitEmpty();
        return emitSequence(emitNotes(), signature().emitItem()); }

    public Emission emitMethodContents() {
        if (this.hasLocals()) {
            if (this.returnsVoid()) {
                return emitStatement(emitClosureValue(emitClosure()));
            }
            else {
                return emitContents();
            }
        }
        else {
            return emitContents();
        }
    }

    public Emission emitSimple() {
        return emitMethodScope(comment(), emitExitVariables(), emptyIf(signature().hasResult()), 
                emitSignature(), emitContents(), emptyIf(needsFrame()), emitConstruct()); }

    public Emission emitAbstract() {
        return emitMethodEmpty(comment(), emptyIf(signature().hasResult()), emitSignature()); }

} // Method
