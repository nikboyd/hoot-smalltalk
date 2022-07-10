package Hoot.Runtime.Behaviors;

import java.util.*;
import java.lang.reflect.*;

import Hoot.Runtime.Faces.Named;
import Hoot.Runtime.Names.TypeName;
import Hoot.Runtime.Values.Variable;
import static Hoot.Runtime.Functions.Utils.*;
import static Hoot.Runtime.Behaviors.HootRegistry.*;
import static Hoot.Runtime.Names.Primitive.isStatic;
import Hoot.Runtime.Faces.Logging;
import Hoot.Runtime.Faces.Valued;

/**
 * An essential type description.
 * <h4>Typified Responsibilities:</h4>
 * <ul>
 * <li>knows its metaClass</li>
 * <li>knows its immediate superclass</li>
 * <li>knows its corresponding primitive Java class</li>
 * </ul>
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public interface Typified extends Valued.Metatype, Named, Logging {

    /**
     * Meta-type base type.
     */
    public static interface Metatype extends Typified { } // Metatype

    public Typified $class();
    public Typified superclass();

    default Class<?> outerClass() { return typeMirror().outerClass(); }
    default void reportRegistration() { whisper("registered " + primitiveClass().getCanonicalName()); }

    default Mirror typeMirror() { return Mirror.forType(this); }
    default String packageName() { return typeMirror().primitiveClass().getPackage().getName(); }
    default TypeName typeResolver() { return TypeName.fromOther(this); }

    default boolean isEmpty() { return typeMirror().isEmpty(); }
    default boolean hasMetaface() { return $class() != null; }
    default boolean isReflective() { return false; }

    default boolean isRoot() { return !isEmpty() && typeResolver().isRootType(); }
    default boolean isElementaryType() { return typeResolver().isElementaryType(); }
    default boolean isEraseableType() {
        return (isRoot()) || typeResolver().isEraseableType() ||
            this.inheritsFrom(Mirror.forClass(RootClass())); }

    default boolean overridenBy(Signed s)  { return typeMirror().overridenBy(s); }

    default Signed getSigned(Signed s) { return getSigned(s.matchSignature()); }
    default Signed getSigned(String s) { return typeMirror().getSigned(s); }
    default String matchSignatures(Signed s) { return typeMirror().matchSignatures(s); }

    default boolean resolves(Named reference) { return false; }
    default Class<?> resolveType(Named reference) { return null; }
    default Variable localNamed(String symbol) { return null; }
    default String resolveTypeName(Named reference) { return Empty; }
    public static List<String> names(List<Typified> types) { return map(types, type -> type.name().toString()); }

    default boolean inheritsFrom(Typified candidate) { return hasAny(candidate) && fullInheritance().contains(candidate); }
    default List<Typified> simpleHeritage() { return emptyList(Typified.class); }
    default List<Typified> typeHeritage() { return emptyList(Typified.class); }
    default List<Typified> fullInheritance() {
        ArrayList<Typified> results = emptyList(Typified.class);
        results.addAll(simpleHeritage());
        results.addAll(typeHeritage());
        return results; }

    default Map<String, Field> instanceFields() {
        HashMap<String, Field> results = emptyMap(Field.class);
        wrap(primitiveClass().getDeclaredFields()).forEach(f -> {
            if (!isStatic(f)) results.put(f.getName(), f);
        });
        return results; }

    default Map<String, Field> staticFields() {
        HashMap<String, Field> results = emptyMap(Field.class);
        wrap(primitiveClass().getDeclaredFields()).forEach(f -> {
            if (isStatic(f)) results.put(f.getName(), f);
        });
        return results; }

} // Typified
