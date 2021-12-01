package Hoot.Runtime.Faces;

import static Hoot.Runtime.Functions.Utils.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * A value.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2019 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 * @see <a href="https://gitlab.com/hoot-smalltalk/hoot-smalltalk/tree/master/LICENSE.txt">LICENSE for more details</a>
 */
public interface Valued extends Logging {

    /**
     * A value meta-type. A marker type for all meta-types.
     */
    public static interface Metatype {

        default Class<?> primitiveClass() { return getClass(); }
        default boolean inheritsFrom(Metatype candidate) {
            return hasAny(candidate) && candidate.primitiveClass().isAssignableFrom(primitiveClass()); }
        default boolean equals(Metatype candidate) {
            return hasAny(candidate) && primitiveClass().equals(candidate.primitiveClass()); }

    } // Metatype

    @SuppressWarnings("unchecked") default <R> Class<R> resultType(Class valueType) { return (Class<R>)valueType; }
    default <R> R value() { Class<R> resultType = resultType(valueType()); return resultType.cast(this); }

    default Class<?> valueType() { return getClass(); }
    default <R> R as(Class<R> aType) {
        if (hasNone(aType)) return null;
        if (aType.isAssignableFrom(getClass())) return value();
        return ValueProxy.with(this).as(aType); }

    /**
     * A value proxy.
     */
    public static class ValueProxy implements InvocationHandler {

        protected Valued value;
        protected ValueProxy(Valued v) { value = v; }
        public static ValueProxy with(Valued v) { return new ValueProxy(v); }

        public <R> R as(Class<R> aType) {
            java.lang.Class[] faces = { aType };
            return aType.cast(Proxy.newProxyInstance(getClass().getClassLoader(), faces, this));
        }

        @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            try {
                Method m = value.getClass().getMethod(method.getName(), method.getParameterTypes());
                if (m != null) return method.invoke(value, args);
            }
            catch (NoSuchMethodException ex) { }
            throw new UnsupportedOperationException(
                method.getName() + " unknown for " + value.getClass().getCanonicalName());
        }

    } // Proxy

} // Valued
