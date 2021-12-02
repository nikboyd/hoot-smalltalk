package Hoot.Runtime.Behaviors;

import java.util.*;
import java.lang.reflect.*;

import Hoot.Runtime.Names.Name;
import Hoot.Runtime.Faces.Named;
import Hoot.Runtime.Emissions.Item;
import Hoot.Runtime.Names.Selector;
import Hoot.Runtime.Names.Signature;
import static Hoot.Runtime.Functions.Utils.*;
import static Hoot.Runtime.Functions.Exceptional.*;
import static Hoot.Runtime.Names.Name.MetaMember;

/**
 * Provides reflective utilities for dealing with primitive Java classes.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 1999,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class Mirror extends Item implements Typified {

    public Mirror() { this(null); }
    public Mirror(Class<?> reflectedClass) { super(null); aClass = reflectedClass; }

    @Override public int hashCode() { return isEmpty() ? super.hashCode() : aClass.hashCode(); }
    protected boolean equals(Mirror m) { return (isEmpty() && m.isEmpty()) || (aClass.equals(m.aClass)); }
    @Override public boolean equals(Object mirror) {
        return hasAny(mirror) && getClass() == mirror.getClass() && falseOr(m -> this.equals(m), (Mirror) mirror); }

    /**
     * @return a Mirror for reflecting upon a type
     * @param aType a type upon which to reflect
     */
    public static Mirror forType(Typified aType) {
        return (aType == null) ? emptyMirror() : forClass(aType.primitiveClass()); }

    /**
     * @return a Mirror for reflecting upon aClass
     * @param aClass a class upon which to reflect
     */
    public static Mirror forClass(Class<?> aClass) {
        if (aClass == null) return emptyMirror();
        if (!Registry.containsKey(aClass)) {
            Registry.put(aClass, new Mirror(aClass));
        }
        return Registry.get(aClass);
    }


    // an empty mirror (for null)
    public static final Mirror EmptyMirror = new Mirror();
    public static Mirror emptyMirror() { return EmptyMirror; }
    @Override public Typified superclass() { return superior(); }
    public Mirror superior() { return isEmpty() ? emptyMirror() : superMirror(); }
    private Mirror superMirror() { return Mirror.forClass(reflectedSuperclass()); }

    public static Class[] unwrapTypes(List<Typified> types) {
        return unwrap(map(types, type -> type.primitiveClass()), EmptySample); }

    public static List<Typified> argumentTypes(Method m) {
        return map(wrap(m.getParameterTypes()), type -> Mirror.forClass(type)); }

    protected static Class[] EmptySample = { };
    public static Class[] argumentTypes(List<String> classNames) {
        return unwrap(map(classNames, n -> Selector.from(n).toClass()), EmptySample); }

    protected Class<?> aClass; // class upon which to reflect
    @Override public void release() { aClass = null; }
    @Override public boolean isEmpty() { return hasNone(aClass); }
    @Override public boolean isReflective() { return true; }
    @Override public Class<?> primitiveClass() { return aClass; }
    public boolean isTypical() { return Typified.class.isInstance(aClass); }
    public Typified reflectedType() { return Typified.class.cast(aClass); }

    public Class<?> reflectedClass() { return primitiveClass(); }
    private Class<?> reflectedSuperclass() { return reflectedClass().getSuperclass(); }
    @Override public Class<?> outerClass() { return isEmpty() ? null : aClass.getEnclosingClass(); }

    @Override public String name() { return isEmpty() ? Empty : primitiveClass().getName(); }
    @Override public String shortName() { return isEmpty() ? Empty : Name.typeName(name()); }
    @Override public String description() { return isEmpty() ? Empty : primitiveClass().getCanonicalName(); }

    public boolean hasMetaclass() { return (fieldType(MetaMember) != null); }
    public Field metaclassField() { return fieldNamed(MetaMember); }
    public Class<?> metaclassType() { return fieldType(MetaMember); }
    @Override public Typified $class() { return hasMetaclass() ? Mirror.forClass(metaclassType()) : null; }
    public Typified metaclassInstance() { return nullOrTryLoudly(() -> (Typified)metaclassField().get(null)); }

    public Field fieldNamed(String fieldName) { return findField(fieldName); }
    public Class<?> fieldType(String fieldName) {
        return nullOr(f -> f.getType(), fieldNamed(fieldName)); }

    @Override public Signed getSigned(Signed s) { return nullOr(m -> Signature.from(m), methodSigned(s)); }
    public Method methodSigned(Signed s) {
        return findFirst(select(wrap(aClass.getDeclaredMethods()),
            m -> m.getName().equals(s.methodName())),
            m -> Signature.from(m).matchesArgumentTypes(s)); }

    public Method methodSigned(String signature) {
        List<String> parts = Signature.parse(signature);
        return methodNamed(parts.get(1), Signature.argumentTypes(parts)); }

    public Method methodNamed(String methodName, Class[] arguments) { return findMethod(methodName, arguments); }
    public Class<?> methodType(String methodName, Class[] arguments) {
        return nullOr(m -> m.getReturnType(), methodNamed(methodName, arguments)); }

    @Override public Class<?> resolveType(Named reference) { return fieldType(reference.name().toString()); }
    @Override public String resolveTypeName(Named reference) {
        return emptyOr(type -> type.getCanonicalName(), resolveType(reference)); }

    @Override public List<Typified> simpleHeritage() {
        ArrayList<Typified> results = emptyList(Typified.class);
        if (!isEmpty()) {
            Typified superior = superior();
            if (!superior.isEmpty()) {
                results.add(superior);
                results.addAll(superior.simpleHeritage());
            }
        }
        return results;
    }

    @Override public List<Typified> typeHeritage() {
        Set<Typified> results = emptySet(Typified.class);
        wrap(primitiveClass().getInterfaces()).forEach(type -> {
            Typified mirror = Mirror.forClass(type);
            results.add(mirror);
            results.addAll(mirror.typeHeritage());
        });
        if (!superior().isEmpty()) {
            simpleHeritage().forEach(superType -> {
                wrap(superType.primitiveClass().getInterfaces()).forEach(impType -> {
                    Typified aType = Mirror.forClass(impType);
                    results.add(aType);
                    results.addAll(aType.typeHeritage());
                });
            });
        }
        return copyList(results);
    }

    @Override public boolean resolves(Named reference) { return fieldNamed(reference.shortName()) != null; }
    @Override public boolean overridenBy(Signed s) {
        Typified mFace = s.faceType();
        if (mFace.inheritsFrom(this)) {
            Method result = methodSigned(s);
            if (hasAny(result)) return true;
//            if (result == null) return false;
//            return s.overrides(Signature.from(result));
        }
        return false;
    }

    @Override public Signed getSigned(String s) { return Signature.from(methodSigned(s)); }
    @Override public String matchSignatures(Signed s) {
        String fullSig = s.fullSignature();
        if (getSigned(fullSig) != null) return fullSig;

        String erasedSig = s.erasedSignature();
        if (getSigned(erasedSig) != null) return erasedSig;

        String shortSig = s.shortSignature();
        List<String> parts = Signature.parse(shortSig);
        if (s.argumentCount() > 0) {
            for (Method m : aClass.getMethods()) {
                Signature sig = Signature.from(m);
                if (sig.shortSignature().equals(parts.get(1))) {
                    return sig.shortSignature();
                }
            }
        }

        return Empty;
    }

    // registered class mirrors
    protected static Map<Class, Mirror> Registry = new HashMap<>();
    public static void releaseAll() {
        Registry.entrySet().forEach(entry -> entry.getValue().release());
        Registry.clear();
    }


    private Field findField(String fieldName) {
        try {
            if (isEmpty()) return null;
            return aClass.getDeclaredField(fieldName);
        } catch (NoSuchFieldException ex) {
            return superior().fieldNamed(fieldName);
        }
    }

    @SuppressWarnings("UseSpecificCatch")
    public Method findMethod(String methodName, Class[] arguments) {
        try {
            if (isEmpty()) return null;
            return aClass.getDeclaredMethod(methodName, arguments);
        } catch (Throwable ex) {
            return superior().methodNamed(methodName, arguments);
        }
    }

    public Method findMethod(String methodName, List<String> typeNames) {
        return null; // ??
    }

} // Mirror
