package Hoot.Runtime.Values;

import java.util.*;
import java.math.BigDecimal;
import static org.apache.commons.lang3.StringUtils.*;

import Hoot.Runtime.Names.Global;
import Hoot.Runtime.Names.TypeName;
import Hoot.Runtime.Behaviors.Typified;
import Hoot.Runtime.Behaviors.Scope;
import Hoot.Runtime.Notes.*;

import Hoot.Runtime.Emissions.Item;
import Hoot.Runtime.Emissions.Emission;
import Hoot.Runtime.Emissions.NamedItem;
import static Hoot.Runtime.Names.Primitive.*;
import static Hoot.Runtime.Emissions.Emission.*;
import static Hoot.Runtime.Names.TypeName.RootType;
import static Hoot.Runtime.Names.TypeName.EmptyType;
import static Hoot.Runtime.Names.Keyword.*;
import static Hoot.Runtime.Functions.Utils.*;
import static Hoot.Runtime.Values.Exit.*;

/**
 * A variable, including its name, type, and (optional) initial value.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 1999,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class Variable extends Operand implements ValueSource {

    public Variable(NamedItem scope) { super(scope); }
    protected Variable(NamedItem scope, String name, DetailedType type) { this(scope); name(name); notedType(type); }
//    @Override public void clean() { resolveType(); }

    protected boolean definesValue = false;
    public boolean definesValue() { return this.definesValue; }
    protected void makeDefined() { this.definesValue = true; }

    public Variable makeAssignment() { return defineLocal(); }
    public Variable defineMember() { defineMember(facialScope()); return this; }
    private void defineMember(Scope s) { if (!s.hasLocal(this.name())) scopeLocal(s); }

    public Variable defineLocal() { defineLocal(containerScope()); return this; }
    private void defineLocal(Scope s) { if (!isDefined()) scopeLocal(s); }

    private void scopeLocal(Scope s) {
        s.addLocal(this);
        makeDefined();
        reportLocal(s);
    }

    static final String[] KnownNames = { "f0", "exitID", };
    static final String LocalReport = "defined %s in %s";
    private void reportLocal(Scope s) {
        if (!wrap(KnownNames).contains(name())) {
            whisper(format(LocalReport, name(), s.description()));
        }
    }


    public static Variable from(Item item) { return nullOr(v -> (Variable)v, item.variableContainer()); }
    public static Variable from(Scope scope, String name, DetailedType type) { return new Variable(scope, name, type); }

    public static Variable exitFrom(Scope context, Operand value) {
        return named(FrameId, FrameIdType, context).withValue(value).makeForExiting(); }

    public static Variable frame(Scope context, Operand value) {
        return frameFrom(context).withValue(value).makeForExiting(); }

    private static Variable frameFrom(Scope context) {
        return named(Frame.name(0), Frame.className(), context); }

    public static Variable named(String name, DetailedType type, Operand value) {
        return from(Scope.current(), name, type).withValue(value).resolveType(); }

    public static Variable named(String name, DetailedType type) {
        // for arguments only, infer type from name if needed. --nik
        return from(Scope.current(), name, hasAny(type) ? type : DetailedType.from(TypeName.inferFrom(name))); }

    private static Variable named(String name, String type, Scope container) {
        return from(container, name, DetailedType.with(Global.named(type))); }

    public Variable withErasure() {
        Variable result = new Variable((NamedItem)container());
        result.notedType(DetailedType.RootType);
        result.names.addAll(this.names);
        return result;
    }


    protected final List<String> names = emptyList(String.class);
    private void name(String aName) { names.add(aName); }
    @Override public boolean isEmpty() { return names.isEmpty(); }
    @Override public String name() { return isEmpty() ? Empty : TypeName.with(names).fullName(); }
    protected static final String NameReport = "%s = %s: %s";
    @Override public String description() {
        return format(NameReport, getClass().getSimpleName(), name(), resolvedTypeName()); }

    DetailedType notedType;
    public DetailedType notedType() { return this.notedType; }
    public boolean hasTypeNote() { return notedType() != null; }
    private void notedType(DetailedType notedType) { if (hasAny(notedType)) this.notedType = notedType; }
    public String type() { return typeResolver().typeName(); }
    public Typified typeFace() { return typeResolver().findType(); }
    public boolean valueNeedsCast() { return hasValue() && !type().equals(operandValue().resolvedTypeName()); }

    @Override public TypeName typeResolver() {
        if (this.hasTypeNote()) return this.notedType().toTypeName();
        if (referencesMember()) return referencedMember().typeResolver();
        if (referencesLocal()) {
            Variable ref = referencedLocal(); // same ref without note?
            return (ref == this) ? EmptyType : ref.typeResolver();
        }

        return EmptyType;
    }

//    @SuppressWarnings("unchecked")
//    @Override public Object value() { return operandValue(); }

    protected Operand value = null;
    public Operand operandValue() { return this.value; }

    public void value(Operand aValue) { value = aValue.inside(this); }
    public boolean noValue() { return hasNone(operandValue()); }
    public boolean hasValue() { return hasOne(operandValue()); }
    @Override public boolean containsExit() { return hasValue() && operandValue().exitsMethod(); }
    public Variable withValue(Operand value) { if (hasAny(value)) { value(value); value.makeAssigned(); }  return this; }

    public String defaultValue() {
        String result = Null;
        if (isPrimitiveType(type())) {
            result = 0 + Empty; // as text
            if (Boolean.toLowerCase().equals(type())) {
                result = False.toLowerCase();
            }
        }
        return result;
    }

    protected boolean forExiting = false;
    public boolean isForExiting() { return forExiting; }
    public Variable makeForExiting() { this.forExiting = true; return this; }

    @Override public boolean isVariable() { return true; }
    @Override public boolean isTransient() { return notes().isTransient(); }
    @Override public boolean isPrimitive() { return notes().isPrimitive(); }

    public Variable withNotes(List<Note> notes) { noteAll(notes); return this; }
    @Override public void makeTransient() { notes().makeTransient(); }
    public boolean needsAccess() { return !notes().hasAccess(); }

    public boolean isMember() { return containerScope().isFacial(); }
    public boolean isDefined() { return referencesMember() || referencesLocal(); }
    public String scopeDescription() { return isMember() ? "member" : "local"; }
    public boolean needsTypeResolved() { return !isMember() && !isDefined() && !hasTypeNote(); }

    protected Variable resolveType() {
        if (hasValue()) {
            operandValue().clean();
            if (needsTypeResolved()) {
                TypeName type = operandValue().typeResolver();
                if (!type.isUnknown() && !type.isRootType()) {
                    notedType(DetailedType.from(type));
                }
            }
        }
        return this; }

    protected String comment = Empty;
    public void comment(String aString) { comment = defaultIfEmpty(aString, Empty); }
    public boolean hasComment() { return comment.length() > 0; }
    @Override public String comment() { // remove the surrounding quotes if needed
        return (comment.isEmpty() ? null : comment.substring(1, comment.length() - 1)); }

    public boolean referencesStacked() { return falseOr(v -> v.isStacked(), referencedLocal()); }
    public boolean referencesLocal() { return matchAny(blockScopes(), s -> s.hasLocal(name())); }
    public boolean referencesMember() { return matchAny(faceHeritage(), f -> f.resolves(this)); }

    public Variable referencedLocal() {
        return nullOr(s -> s.localNamed(name()), findFirst(blockScopes(), bs -> bs.hasLocal(name()))); }

    public Variable referencedMember() {
        return nullOr(s -> s.localNamed(name()), findFirst(faceHeritage(), fs -> fs.resolves(this))); }

    public List<Typified> faceHeritage() {
        List<Typified> results = emptyList(Typified.class);
        results.add((Typified)facialScope());
        results.addAll(facialScope().simpleHeritage());
        return results; }

    public Emission variableNotes() { return emitSequence(notes().variableNotesOnlyDecor()); }
    public Emission argumentNotes() { return emitSequence(notes().argumentNotesOnlyDecor()); }

    public Emission emitNotedType() {
        if (hasTypeNote()) return notedType().emitCode(true);
        if (definesValue()) return emitItem(localVar());
        if (isDefined()) return null;
        return emitItem(localVar());
    }

    public Emission emitType(boolean wantsBases) { // used by members
        if (hasTypeNote()) return notedType().emitCode(wantsBases);
        if (definesValue()) emitItem(localVar());
        if (isDefined()) return null;
        return emitItem(localVar());
    }

    public Emission emitCast() { return emitCast(emitType(false), emitItem(name())); }
    public Emission emitType() { return emitType(true) ; }
    public Emission emitArgumentType() {
        return hasTypeNote() ? notedType().makeArgued(true).emitCode(false) : emitItem(RootType().fullName()) ; }

    @Override public Emission emitOperand() {
        if (this.hasValue()) {
            if (this.isStacked()) return this.hasTypeNote() ? emitSequence(emitSimply(), emitBinding()) : emitBoundValue();
            if (this.referencesStacked()) return emitBoundValue();
            if (operandValue().hasCascades()) return emitValue();
        }

        return emitSimply();
    }

    @Override public Emission emitItem() {
        if (notes().isProperty()) return emitProperty();
        return this.isTransient() ? emitTransientLocal() : emitVariable(); }

    public Emission emitBinding() { return emitStackedBind(name(), name()); }
    public Emission emitBoundValue() { return emitStackedBind(name(), emitValue()); }
    public Emission emitErasedArgument() { return emitArgument(fullName(), RootType().shortName(), true); }
    public Emission emitBlockArgument(int index, int level) { return emitBlockArgument(name(), emitArgumentType(), index, level); }
    public Emission emitArgument() { return emitNamedArgument(name(), emitArgumentType(), argumentNotes()); }
    public Emission emitArgument(boolean useFinal) { return emitArgument(fullName(), typeResolver().typeName(), useFinal); }
    public Emission emitProperty() {
        return emitProperty(name(), emitNotedType(), containerScope().name(), emitValue(), variableNotes(), comment()) ; }

    static final String NoComment = null;
    public Emission emitLocal() { return emitVariable(name(), emitNotedType(), emitValue(), NoValue, NoComment); }
    public Emission emitSimply() { return emitVariable(name(), emitNotedType(), emitValue(), NoValue, NoComment); }
    public Emission emitVariable() {  return emitVariable(name(), emitNotedType(), emitValue(), variableNotes(), comment()); }
    public Emission emitTransientLocal() { return emitTransient(name(), emitNotedType(), emitValueOrDefault(), valueNeedsCast()); }
    public Emission emitValueOrDefault() { return hasValue() ? emitValue() : emitItem(defaultValue()); }
    public Emission emitValue() { return nullOr(v -> isPrimitive() ? v.emitPrimitive() : v.emitOperand(), operandValue()); }

//    @Override public boolean needsLocalResolution() { return false; }
//    @Override public Scope resolvingScope() { return containerScope(); }

//    public void name(Tree node) {
//        comment(commentFrom(node));
//        name(tokenFrom(node).getText());
//    }

//    @Override public Class resolvedType() {
//        String typeName = type();
//        if (typeName.endsWith("[]")) {
//            return typeNamed(typeName);
//        }
//        Class type = Primitive.getPrimitiveType(type());
//        if (type != null) {
//            return type;
//        }
//        if (typeName.startsWith(RootJava)) {
//            return typeNamed(typeName);
//        }
//        Typified typeFace = Library.findFace(typeName);
//        return (typeFace == null ? RootClass() : typeFace.primitiveClass());
//    }

    static final String JDK_8 = "1.8";
    static final String JDK_11 = "11";
    static final BigDecimal MinimumVersion = new BigDecimal(JDK_8);
    static final BigDecimal LocalVarSupport = new BigDecimal(JDK_11);

    static final String JavaSpecVersion = systemValue("java.specification.version");
    static final BigDecimal JavaVersion = new BigDecimal(JavaSpecVersion);

    public static String javaVersion() { return JavaSpecVersion; }
    public static String javaCompatibility() { return (JavaVersion.compareTo(LocalVarSupport) < 0)? JDK_8 : JDK_11; }
    private boolean supportsLocalVars() { return JDK_11.equals(javaCompatibility()); }
    private String localVar() { return supportsLocalVars() ? LocalVariable : null; }
    static final String LocalVariable = "var";

} // Variable
