package Hoot.Runtime.Maps;

import java.util.*;
import java.lang.reflect.*;
import java.util.stream.Collectors;

import Hoot.Runtime.Faces.Valued;
import Hoot.Runtime.Names.Keyword;
import Hoot.Runtime.Names.Selector;
import Hoot.Runtime.Blocks.Enclosure;
import static Hoot.Runtime.Functions.Utils.*;

/**
 * Caches method references for a Java class.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class MethodCache implements Valued {

    public MethodCache(Class aClass) { cacheClass = aClass; }
    public Class<?> cachedType() { return this.cacheClass; }
    private final Class cacheClass;

    private boolean loaded = false;
    private final HashMap<String, Method> contents = emptyMap(Method.class);
    public void loadAllMethods() {
        if (loaded) return;
        Method[] methods = cachedType().getMethods();
        for (Method method : methods) {
            Class[] types = method.getParameterTypes();
            String methodName = method.getName();
            if (qualifiesForLoading(methodName, types)) {
                contents.put(methodName, method);
            }
        }
        loaded = true;
    }

    public HashSet<String> loadMethods() {
        HashSet<String> result = emptySet(String.class);
        Method[] methods = cachedType().getDeclaredMethods();
        for (Method method : methods) {
            Class[] types = method.getParameterTypes();
            String methodName = Keyword.from(method.getName(), types.length);
            if (qualifiesForLoading(methodName, types)) {
                contents.put(methodName, method);
                result.add(methodName);
            }
        }
        return result;
    }

    public void flush() {
        contents.clear();
        loaded = false;
    }

    public Set<Selector> allSelectors() {
        loadAllMethods();
        return contents.keySet().stream()
                .map(name -> Selector.named(name))
                .collect(Collectors.toSet());
    }

    public Set<Selector> selectors() {
        return mapSet(loadMethods(), name -> Selector.named(name)); }

    public final Method methodNamed(String selector, int argumentCount) {

        // if found previously, return cached method
        Method aMethod = contents.get(selector);
        if (aMethod != null) return aMethod;

        // check special selectors for argument classes
        Class arguments[] = specialSelector(selector);

        // determine method argument classes
        if (arguments == null) {
            if (selector.endsWith(IfAbsent)) {
                arguments = classesWithBlock(argumentCount, 1);
            }
            else {
                arguments = objectClasses(argumentCount);
            }
        }

        // find the method and cache its reference
        String methodName = Keyword.with(selector).methodName();
        try {
            aMethod = cachedType().getMethod(methodName, arguments);
            contents.put(selector, aMethod);
            return aMethod;
        }
        catch(NoSuchMethodException e) {
            error(format(MissingMethod, cachedType().getName(), selector, argumentCount));
            return null;
        }
    }

    public final Constructor constructorFor(int argumentCount) {
        Class argumentsTypes[] = objectClasses(argumentCount);
        try {
            return cachedType().getConstructor(argumentsTypes);
        }
        catch( NoSuchMethodException e ) {
            error(format(MissingConstruct, cachedType().getName(), argumentCount));
            return null;
        }
    }

    protected boolean qualifiesForLoading(String methodName, Class[] types) {
        return qualifiesAsSpecial(methodName, types)
            || qualifiesAsAbsentia(methodName, types)
            || qualifiesAsNormal(methodName, types); }

    static final HashMap<String, Class[]> SpecialSelectors = emptyMap(Class[].class);
    protected static Class[] specialSelector(String methodName) { return SpecialSelectors.get(methodName); }
    protected boolean qualifiesAsSpecial(String methodName, Class[] types) {
        Class[] arguments = specialSelector(methodName);
        if (arguments == null || types.length != arguments.length) return false;
        for(int t = 0; t < types.length; t++) if (types[t] != arguments[t]) return false;
        return true;
    }

    static final String IfAbsent = ":ifAbsent:";
    protected boolean qualifiesAsAbsentia(String methodName, Class[] types) {
        if (!methodName.endsWith(IfAbsent)) return false;

        Class[] arguments = classesWithBlock(types.length, 1);
        for(int i = 0; i < types.length; i++) {
            if (types[i] != arguments[i]) return false;
        }

        return true;
    }

    protected boolean qualifiesAsNormal(String methodName, Class[] types) {
        return matchAll(wrap(types), type -> type.equals(Object.class)); }

    static final int CacheSize = 11;
    static Class[][] ObjectClasses = new Class[CacheSize][];
    protected static Class[] objectClasses(int argumentCount) {
        return (argumentCount < CacheSize) ? ObjectClasses[argumentCount] : objectClassArray(argumentCount); }

    static Class[] BlockClasses = { Enclosure.class, Enclosure.class, Enclosure.class };
    protected static Class[] classesWithBlock(int methodArgumentCount, int blockArgumentCount) {
        Class result[] = objectClassArray(methodArgumentCount);
        result[methodArgumentCount - 1] = BlockClasses[blockArgumentCount];
        return result;
    }

    protected static Class[] objectClassArray(int argumentCount) {
        Class result[] = new Class[argumentCount];
        for(int i = 0; i < argumentCount; i++) result[i] = Object.class;
        return result;
    }

    static {
        SpecialSelectors.put("do:", classesWithBlock(1, 1));
        SpecialSelectors.put("to:do:", classesWithBlock(2, 1));
        SpecialSelectors.put("from:to:do:", classesWithBlock(3, 1));
        SpecialSelectors.put("to:by:do:", classesWithBlock(3, 1));

        SpecialSelectors.put("detect:", classesWithBlock(1, 1));
        SpecialSelectors.put("select:", classesWithBlock(1, 1));
        SpecialSelectors.put("reject:", classesWithBlock(1, 1));
        SpecialSelectors.put("collect:", classesWithBlock(1, 1));
        SpecialSelectors.put("inject:into:", classesWithBlock(2, 2));

        SpecialSelectors.put("ensure:", classesWithBlock(1, 0));
        SpecialSelectors.put("ifCurtailed:", classesWithBlock(1, 0));

        for(int i = 0; i < CacheSize; i++) {
            ObjectClasses[i] = objectClassArray(i);
        }
    }

    private static final String MissingMethod = "%s does not understand %s with %d argument(s)";
    private static final String MissingConstruct = "%s does not construct with %d argument(s)";
}
