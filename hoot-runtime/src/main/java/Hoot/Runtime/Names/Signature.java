package Hoot.Runtime.Names;

import java.util.*;
import java.lang.reflect.Type;
import java.lang.reflect.Method;

import Hoot.Runtime.Behaviors.*;
import static Hoot.Runtime.Functions.Utils.*;
import static Hoot.Runtime.Names.Keyword.Object;
import static Hoot.Runtime.Names.Keyword.Colon;

/**
 * A method signature.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 * @see <a href="https://gitlab.com/hoot-smalltalk/hoot-smalltalk/tree/master/LICENSE.txt">LICENSE for more details</a>
 */
public class Signature implements Signed {

    public Signature(Method m) { this.m = m; }
    public static Signature from(Method method) { return nullOr(m -> new Signature(m), method); }

    protected Method m;
    public Method method() { return m; }
    protected String selector() { return method().getName(); }
    protected Class<?> methodClass() { return method().getDeclaringClass(); }
    protected Mirror typeMirror() { return Mirror.forClass(methodClass()); }
    protected Mirror resultMirror() { return Mirror.forClass(method().getReturnType()); }
    protected List<Typified> argumentMirrors() {
        return map(wrap(method().getParameterTypes()), type -> Mirror.forClass(type)); }

    public boolean overrides(Signature s) { return matchesSign(s) && inherits(s); }
    @Override public boolean overrides(Signed s) {
        return matchesKind(s) ? overrides((Signature)s) : Signed.super.overrides(s); }

    @Override public boolean isStatic() { return Primitive.isStatic(method()); }
    public boolean matchesKind(Signed s) { return getClass().isInstance(s); }
    public boolean inherits(Signature s) { return !matchesClass(s) && s.methodClass().isAssignableFrom(methodClass()); }
    public boolean matchesClass(Signature s) { return methodClass().equals(s.methodClass()); }
    public boolean matchesSign(Signature s) { return selector().equals(s.selector()) && matchArguments(s); }
    public boolean matchArguments(Signature s) {
        if (argumentCount() != s.argumentCount()) return false;
        if (0 == argumentCount()) return true;

        List<TypeName> argTypeNames = argumentTypeNames();
        List<TypeName> sargTypeNames = s.argumentTypeNames();
        for (int index = 0; index < argTypeNames.size(); index++) {
            if (!argTypeNames.get(index).matches(sargTypeNames.get(index))) return false;
        }
        return true;
    }

    @Override public String methodName() { return Keyword.with(selector()).methodName(); }
    @Override public Typified faceType() { return typeMirror(); }
    @Override public String faceName() { return faceType().fullName(); }

    @Override public int argumentCount() { return m.getParameterCount(); }
    @Override public List<Typified> argumentTypes() { return argumentMirrors(); }
    @Override public List<TypeName> argumentTypeNames() {
        final int[] index = { 0 };
        Class<?>[] argTypes = method().getParameterTypes();
        Type[] genTypes = method().getGenericParameterTypes();
        return map(wrap(genTypes), type -> {
            String argTypeName = argTypes[index[0]].getSimpleName();
            String genTypeName = genTypes[index[0]].getTypeName();
            index[0]++;

            boolean isGeneric = !genTypeName.endsWith(argTypeName);
            whisper(argTypeName + " ?= " + genTypeName);
            TypeName result = TypeName.fromType(type);
            return isGeneric ? result.noteGeneric() : result; }); }

    @Override public String fullSignature() { return shortSignature() + formatTerm(argumentSignatures()); }
    @Override public String erasedSignature() { return shortSignature() + formatTerm(argumentErasures()); }
    @Override public String shortSignature() { return resultMirror().fullName() + Colon + methodName(); }
    @Override public String matchSignature() { return methodName() + formatTerm(argumentSignatures()); }
    @Override public String matchErasure() { return methodName() + formatTerm(argumentErasures()); }

    private List<String> argumentErasures() { return map(argumentTypes(), type -> Object); }
    private List<String> argumentSignatures() { return map(argumentTypeNames(), type -> type.shortName()); }

    public static String formatTerm(List<String> types) { return String.format(Term, formatList(types)); }
    public static String formatList(List<String> types) { return joinWith(Comma, types); }

    static final String Comma = ",";
    static final String Term = "(%s)";
    static final String TermEnds = "[\\(\\)]";
    public static List<String> parse(String sig) {
        String[] parts = { Empty, sig };
        if (sig.contains(Colon)) {
            parts = sig.split(Colon);
        }

        String[] names = parts[1].split(TermEnds);
        ArrayList<String> results = emptyList(String.class);
        results.add(parts[0]);
        results.add(names[0]);
        if (names.length == 1) {
            return results;
        }

        String[] args = names[1].split(Comma);
        results.addAll(wrap(args));
        return results;
    }

    public static Class[] argumentTypes(List<String> sigNames) {
        Class[] empty = { };
        if (sigNames.size() < 3) return empty;
        ArrayList<Class> results = emptyList(Class.class);
        for (int index = 2; index < sigNames.size(); index++) {
            Class argType = Selector.from(sigNames.get(index).trim()).toClass();
            if (hasAny(argType)) results.add(argType);
        }
        return unwrap(results, empty);
    }

} // Signature
