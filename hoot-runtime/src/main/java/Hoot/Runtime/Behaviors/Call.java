package Hoot.Runtime.Behaviors;

import java.util.*;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import Hoot.Runtime.Names.Keyword;
import Hoot.Runtime.Names.Selector;
import static Hoot.Runtime.Functions.Utils.*;
import org.apache.commons.lang3.reflect.MethodUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;

/**
 * Describes a method call.
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2024 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public interface Call {

    default Object receiver() { return null; }
    default String methodName() { return getClass().getCanonicalName(); }
    default String selectedName() { return Keyword.with(methodName()).methodName(); }
    default Class<?> selectedClass() { return Selector.named(methodName()).toClass(); }

    static final Class VoidClass = void.class;
    default Class<?> receiverType() { 
        return hasNo(receiver())? VoidClass: 
            Class.class.isInstance(receiver())? (Class)receiver(): 
                receiver().getClass(); }

    static final Object[] NoArgs = {};
    default Object[] argValues() { return unwrap(argumentValues(), NoArgs); }
    default List<Object> argumentValues() { return new ArrayList<>(); }

    static final Class[] NoTypes = {};
    default Class[] argTypes() { return unwrap(argumentTypes(), NoTypes); }
    default List<Class<?>> argumentTypes() {
        return map(argumentValues(), arg -> arg.getClass()); }

    default Method findMethod() {
        return MethodUtils.getMatchingAccessibleMethod(
            receiverType(), selectedName(), argTypes()); }
    
    default Constructor findConstructor() {
        return ConstructorUtils.getAccessibleConstructor(
            constructorType(), argTypes()); }

    default Class<?> constructorType() {
        if (hasOne(methodName())) return selectedClass();
        reportMissingReceiver();
        return null;
    }

    default Object callMethod() {
        try {
            if (VoidClass == receiverType()) {
                Constructor c = findConstructor(); if (hasNo(c)) return null;
                return c.newInstance(argValues());
            }
            else {
                Method m = findMethod(); if (hasNo(m)) return null;
                return m.invoke(receiver(), argValues());
            }
        }
        catch (Exception ex) {
            return null;
        }
    }

    default void reportMissingReceiver() {
        throw new IllegalArgumentException("expected a Class or TypeDescription"); }

} // Call
