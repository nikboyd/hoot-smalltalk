package Hoot.Runtime.Behaviors;

import java.util.*;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandle;
import static java.lang.invoke.MethodHandles.lookup;

import Hoot.Runtime.Values.Value;
import Hoot.Runtime.Faces.Logging;
import Hoot.Runtime.Faces.Selector;
import Hoot.Runtime.Blocks.MultiValuable;
import static Hoot.Runtime.Functions.Utils.*;
import static Hoot.Runtime.Functions.Exceptional.*;

/**
 * A method (or constructor) invoker.
 *
 * <h4>Invoke Responsibilities:</h4>
 * <ul>
 * <li>knows a method selector</li>
 * <li>knows a method result type</li>
 * <li>knows a method receiver and its argument values</li>
 * <li>calls a method to produce a result</li>
 * </ul>
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class Invoke implements Logging {

    /**
     * @return a new Invoker
     * @param values a method receiver and arguments.
     */
    public static Invoke withValues(Object... values) {
        List<Value> results = map(wrap(values), each -> Value.with(each));
        if (!results.isEmpty()) results.get(0).makeSelfish(); // mark receiver
        return with(unwrap(results, NoValues));
    }

    /**
     * @return a new Invoker
     * @param values a method receiver and argument values
     */
    public static Invoke with(Value<?> ... values) {
        Invoke result = new Invoke();
        result.values = values;
        return result;
    }

    /**
     * @return this Invoker configured
     * @param resultType a result type
     */
    public Invoke result(Class<?> resultType) { this.resultType = resultType; return this; }

    /**
     * @return this Invoker configured
     * @param selector a selector
     */
    public Invoke with(Selector selector) { this.selector = selector; return this; }

    @SuppressWarnings("unchecked")
    public <R> R callWith(Object... arguments) {
        return nullOrTryLoudly(() -> {
//            report(arguments.getClass().getCanonicalName());
            return (R) methodHandle().invokeWithArguments(arguments); }); }

    /**
     * @return the result of a method call, or null
     * @param <R> a result type
     */
    public <R> R call() { return callWith(unwrap(methodArguments(), NoObjects)); }

    /**
     * @return the result of a method call, or null
     * @param <R> a result type
     * @param selector a method selector
     */
    public <R> R call(Selector selector) { return with(selector).call(); }

    /**
     * @return the result of a method call, or null
     * @param <R> a result type
     * @param selector a method selector
     * @param arguments method arguments
     */
    public <R> R call_with(Selector selector, Object ... arguments) {
        return with(selector).callWith(arguments); }

    /**
     * @return the result of a method call, or null
     * @param <R> a result type
     * @param resultType a result type
     */
    public <R> R call(Class<?> resultType) {
        return with(selector).result(resultType).call(); }

    private Object receiver() { return (count() == 0 ? null : values().get(0).value()); }
    private Class<?> receiverType() {
        if (count() == 0) return void.class;
        return (this.values[0].isSelfish() ?
                this.values[0].value().getClass() : void.class); }

    private MethodType constructorType() { return MethodType.methodType(VoidClass, argumentTypes()); }
    private Class<?> constructorClass() {
        if (selector() != null) return selectedClass();
        if (receiver() == null) reportMissingReceiver();
        if (receiver() instanceof Class) return (Class)receiver();
        if (receiver() instanceof Typified) return typifiedReceiver().primitiveClass();

        return null;
    }

    private List<Class<?>> argumentTypes() { return map(argumentValues(), each -> each.value().getClass()); }
    private List<Value<?>> argumentValues() {
        if (count() == 0) return new ArrayList<>();
        List<Value<?>> results = values();
        return (results.get(0).isSelfish() ? results.subList(1, count()) : results); }


    private List<Object> methodArguments() {
        if (count() == 0) return emptyList(Object.class);
        List<Value<?>> results = values();

        if (results.get(0).isSelfish() &&
            results.get(0).valueType().getName().equals(Class.class.getName())) {
            results = results.subList(1, count());
        }

        return map(results, each -> each.value());
    }

    static final String HandleReport = "lookup: %s! %s#%s%s";
    private void reportHandle(Class aClass, MethodType mType) {
        report(format(HandleReport,
            mType.returnType().getSimpleName(),
            aClass.getSimpleName(), selector().name(),
            map(wrap(mType.parameterArray()), (p) -> p.getSimpleName()))); }

    private MethodHandle methodHandle() throws Throwable {
        Class<?> receiverType = receiverType();
        if (receiverType == VoidClass) {
//            reportHandle(constructorClass(), constructorType());
            return lookup().findConstructor(constructorClass(), constructorType());
        }

        if (Class.class.isAssignableFrom(receiverType)) {
//            reportHandle(classifiedReceiver(), methodType());
            return lookup().findStatic(classifiedReceiver(), selector().name(), methodType());
        }

//        reportHandle(receiverType, methodType());
        return lookup().findVirtual(receiverType, selector().name(), methodType());
    }

    private Value<?>[] values = { };
    private List<Value<?>> values() { return wrap(this.values); }
    private int count() { return this.values.length; }

    private Selector selector;
    private Selector selector() { return this.selector; }
    private Class<?> selectedClass() { return selector().toClass(); }

    private Class<?> classifiedReceiver() { return (Class<?>)receiver(); }
    private Typified typifiedReceiver() { return (Typified)receiver(); }

    private Class<?> resultType = Object.class;
    private Class<?> resultType() { return this.resultType; }
    private MethodType methodType() { return MethodType.methodType(resultType(), argumentTypes()); }

    private void reportMissingReceiver() {
        throw new IllegalArgumentException("expected a Class or TypeDescription"); }

    public static final Value[] NoValues = { };
    public static final Object[] NoObjects = { };

    public static final Class VoidClass = void.class;
    public static final String[] AllEmpty = { Empty, Empty };
    static final Selector MainSelector = Hoot.Runtime.Names.Selector.named("main");
    public static void mainOn(Class aClass) {
        Invoke.withValues(aClass, AllEmpty).result(VoidClass).with(MainSelector).call(); }

} // Invoker
