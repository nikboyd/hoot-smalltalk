package Hoot.Runtime.Behaviors;

import java.util.*;

import Hoot.Runtime.Faces.Valued;
import Hoot.Runtime.Names.TypeName;
import static Hoot.Runtime.Names.Keyword.*;
import static Hoot.Runtime.Functions.Utils.*;
import static Hoot.Runtime.Names.TypeName.*;
import Hoot.Runtime.Faces.Logging;

/**
 * Hoot runtime class with registries.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 * @see <a href="https://gitlab.com/hoot-smalltalk/hoot-smalltalk/tree/master/LICENSE.txt">LICENSE for more details</a>
 */
public class HootRegistry implements Logging {

    protected static final HashMap<Class<?>, Object> ValueRegistry = new HashMap<>();
    public static void registerValue(Object instance) {
        nullOr(item -> ValueRegistry.put(item.getClass(), item), instance); }

    @SuppressWarnings("unchecked")
    public static <T> T getValue(Class<?> aClass) { return (T)nullOr(c -> ValueRegistry.get(c), aClass); }

    @SuppressWarnings("unchecked")
    public static <T> T getRegistered(Class<?> aClass) { return (T)nullOr(c -> TypeRegistry.get(c), aClass); }
    public static <T> T getRegistered(TypeName className) { return getRegistered(className.findClass()); }

    protected static final HashMap<Class<?>, Typified> TypeRegistry = new HashMap<>();
    public static Typified emptyType() { return Mirror.emptyMirror(); }
    public static Typified getType(Class<?> aClass) { return emptyOr(aClass); }
    public static boolean hasType(Class<?> aClass) { return !getType(aClass).isEmpty(); }
    public static Typified emptyOr(Class<?> aClass) { return (hasNone(aClass)) ? emptyType() : emptyOrType(aClass); }
    protected static Typified emptyOrType(Class<?> aClass) { return TypeRegistry.getOrDefault(aClass, emptyType()); }
    public static Typified registerType(Typified type) {
        if (hasNo(type)) return emptyType(); // vacuous --nik
        if (hasType(type.primitiveClass())) return getType(type.primitiveClass());

        Mirror m = Mirror.forClass(type.outerClass());
        TypeRegistry.put(type.primitiveClass(), type);
        type.reportRegistration();
        if (!m.isEmpty()) {
            TypeRegistry.put(type.outerClass(), m);
            m.reportRegistration();
        }
        return type; }

    public static Class<?> JavaRoot() { return JavaRoot; }
    public static Class<?> RootClass() { return itemOr(JavaRoot, RootType().findClass()); }

    public static TypeName ObjectType() { return TypeName.ObjectType(); }
    public static TypeName RootType() { return TypeName.RootType(); }
    public static TypeName ArrayType() { return TypeName.ArrayType(); }

    public static TypeName BooleanType() { return TypeName.fromCache(Hoot, Behaviors, Boolean); }
    public static TypeName BehaviorType() { return TypeName.fromCache(Hoot, Behaviors, Behavior); }
    public static TypeName ClassType() { return TypeName.fromCache(Hoot, Behaviors, ClassType); }
    public static TypeName MetaclassType() { return TypeName.fromCache(Hoot, Behaviors, MetaclassType); }

    public static TypeName NilType() { return TypeName.fromCache(Hoot, Behaviors, Nil); }
    public static TypeName TrueType() { return TypeName.fromCache(Hoot, Behaviors, True); }
    public static TypeName FalseType() { return TypeName.fromCache(Hoot, Behaviors, False); }
    public static TypeName StringType() { return TypeName.fromCache(Hoot, Collections, String); }
    public static TypeName SymbolType() { return TypeName.fromCache(Hoot, Collections, Symbol); }
    public static TypeName CharacterType() { return TypeName.fromCache(Hoot, Magnitudes, Character); }
    public static TypeName IntegerType() { return TypeName.fromCache(Hoot, Magnitudes, Integer); }
    public static TypeName FixedType() { return TypeName.fromCache(Hoot, Magnitudes, Fixed); }
    public static TypeName FloatType() { return TypeName.fromCache(Hoot, Magnitudes, Float); }
    public static TypeName DoubleType() { return TypeName.fromCache(Hoot, Magnitudes, Double); }

    public static Valued Nil() { return getValue(NilType().findClass()); }
    public static Valued True() { return getValue(TrueType().findClass()); }
    public static Valued False() { return getValue(FalseType().findClass()); }

    public static boolean isRunning() { return false; }
    public static boolean isReady() { return false; }

    static final TypeName[] LiteralTypes = {
        NilType(), TrueType(), FalseType(), StringType(), SymbolType(),
        CharacterType(), IntegerType(), FixedType(), FloatType(), DoubleType(),
        ArrayType(),
    };

    static final TypeName[] CollectionTypes = {
        TypeName.fromCache(Hoot, Collections, "Set"),
        TypeName.fromCache(Hoot, Collections, "Bag"),
        TypeName.fromCache(Hoot, Collections, "ByteArray"),
        TypeName.fromCache(Hoot, Collections, "Dictionary"),
        TypeName.fromCache(Hoot, Collections, "IdentityDictionary"),
        TypeName.fromCache(Hoot, Collections, "IdentitySet"),
        TypeName.fromCache(Hoot, Collections, "OrderedCollection"),
        TypeName.fromCache(Hoot, Collections, "SortedCollection"),
        TypeName.fromCache(Hoot, Collections, "Interval"),
    };

    static final TypeName[] MagnitudeTypes = {
        TypeName.fromCache(Hoot, Magnitudes, "Time"),
        TypeName.fromCache(Hoot, Magnitudes, "Fraction"),
        TypeName.fromCache(Hoot, Magnitudes, "Association"),
        TypeName.fromCache(Hoot, Magnitudes, "FastInteger"),
        TypeName.fromCache(Hoot, Magnitudes, "LargeInteger"),
        TypeName.fromCache(Hoot, Magnitudes, "LongInteger"),
    };

    static final TypeName[] ExceptionTypes = {
        TypeName.fromCache(Hoot, Exceptions, "Exception"),
        TypeName.fromCache(Hoot, Exceptions, "Error"),
        TypeName.fromCache(Hoot, Exceptions, "ControlError"),
        TypeName.fromCache(Hoot, Exceptions, "Warning"),
        TypeName.fromCache(Hoot, Exceptions, "ArithmeticError"),
        TypeName.fromCache(Hoot, Exceptions, "ZeroDivide"),
    };

    static final TypeName[] StreamTypes = {
        TypeName.fromCache(Hoot, Streams, "Transcript"),
        TypeName.fromCache(Hoot, Streams, "TextWriteStream"),
    };

    static {
        registerType(Mirror.forClass(JavaRoot));
    }

} // HootRegistry
