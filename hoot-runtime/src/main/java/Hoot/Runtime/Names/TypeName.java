package Hoot.Runtime.Names;

import java.util.*;
import java.lang.reflect.Type;
import static java.lang.Character.*;

import Hoot.Runtime.Notes.Decor;
import Hoot.Runtime.Faces.Named;
import Hoot.Runtime.Maps.Library;
import Hoot.Runtime.Faces.Logging;
import Hoot.Runtime.Behaviors.Scope;
import Hoot.Runtime.Behaviors.Typified;
import Hoot.Runtime.Notes.DetailedType;

import static Hoot.Runtime.Functions.Exceptional.*;
import static Hoot.Runtime.Functions.Utils.*;
import static Hoot.Runtime.Faces.Logging.*;
import static Hoot.Runtime.Names.Operator.*;

import static Hoot.Runtime.Names.Keyword.Array;
import static Hoot.Runtime.Names.Keyword.Arrayed;
import static Hoot.Runtime.Names.Keyword.Behaviors;
import static Hoot.Runtime.Names.Keyword.Collections;
import static Hoot.Runtime.Names.Keyword.TypeSuffix;
import static Hoot.Runtime.Names.Keyword.Object;
import static Hoot.Runtime.Names.Keyword.Hoot;
import static Hoot.Runtime.Names.Name.removeTail;
import static Hoot.Runtime.Names.Primitive.Blank;
import static Hoot.Runtime.Notes.Decor.Generic;

/**
 * Identifies a type, including (possibly) its package.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class TypeName implements Named {

    private TypeName(List<String> heads, List<String> tails) { headNames.addAll(heads); tailNames.addAll(tails); }
    private TypeName(TypeName source) {
        this(source.headNames, source.tailNames); decor.makeNote(source.decor.notes()); makeArrayed(source.isArrayed()); }

    private static TypeName from(Typified type) { return fromCache(type.fullName()); }
    private static TypeName from(Named item) { return fromName(item.fullName()); }
    public static TypeName from(Class<?> aClass) {
        if (aClass == null) return EmptyType;
        return aClass.getName().contains(Dollar) ?
            TypeName.fromName(aClass.getName()) :
            TypeName.fromName(aClass.getCanonicalName()); }

    public static TypeName fromOther(Object item) {
        if (item == null) return EmptyType;
        if (item instanceof TypeName) return new TypeName((TypeName)item);
        if (item instanceof Typified) return TypeName.from((Typified)item);
        if (item instanceof Named) return TypeName.from((Named)item);
        return TypeName.from(item.getClass()); }

    public static TypeName withTails(List<String> names) { return new TypeName(emptyList(String.class), copyList(names)); }
    public static List<TypeName> listFrom(List<String> names) { return map(names, n -> fromName(n)); }
    public static TypeName fromType(Type aType) { return DetailedType.from(aType).toTypeName(); }
    public static TypeName fromTail(String typeName) { return withTails(wrap(typeName.split(WildDot))); }
    public static TypeName fromName(String typeName) { return with(typeName); }
    public static TypeName with(String... names) { return with(wrap(names)); }

    static final String TypeTail = " type";
    static final String ClassTail = " class";
    public static TypeName with(List<String> names) {
        if (names.isEmpty()) return EmptyType;
        if (1 == names.size()) {
            String fullName = names.get(0);
            if (fullName.contains(Dot)) {
                names = wrap(fullName.split(WildDot));
            }
            else if (names.get(0).contains(Blank)) {
                if (fullName.endsWith(TypeTail)) {
                    fullName = fullName.substring(0, fullName.length() - TypeTail.length());
                }
                if (fullName.endsWith(ClassTail)) {
                    fullName = fullName.substring(0, fullName.length() - ClassTail.length());
                }
                names = wrap(fullName.split(Blank));
            }
        }

        ArrayList<String> heads = copyList(names);
        String tail = heads.remove(heads.size() - 1);

        if (tail.equals(Wild + Wild)) {
            tail = heads.remove(heads.size() - 1);
            String[] tails = { tail, Wild };
            return new TypeName(heads, wrap(tails));
        }

        boolean arrayed = tail.endsWith(Arrayed);
        if (arrayed) tail = removeTail(tail, Arrayed);
        String[] tails = tail.split(Dollar);

        return new TypeName(heads, wrap(tails)).makeArrayed(arrayed); }

    // look for known typical Smalltalk variable naming conventions
    private static final String[] Prefixes = { "other", "another", "an", "a", };
    public static TypeName inferFrom(String variableName) { return inferFrom(variableName, null); }
    public static TypeName inferFrom(String variableName, Scope s) {
        if (isEmpty(variableName)) return RootType();
        if (hasSome(s) && s.isFacial()) {
            if (Typified.from(s).matchesTail(variableName))
                return TypeName.fromName(s.name());
        }
        for (String prefix : Prefixes) {
            if (variableName.startsWith(prefix)) {
                String result = variableName.substring(prefix.length());
                if (isCapitalized(result)) return TypeName.fromName(result);
            }
        }
        return RootType();
    }


    public Global toGlobal() { return Global.withList(allNames()).makeArrayed(arrayedType); }
    public List<String> allNames() {
        ArrayList<String> results = emptyList(String.class);
        results.addAll(headNames);
        results.addAll(tailNames);
        return results; }

    private final Decor decor = new Decor();
    public TypeName withNotes(List<String> notes) { decor.makeNote(notes); return this; }
    public TypeName withNotes(String... notes) { return this.withNotes(wrap(notes)); }
    public boolean isNotedPrimitive() { return !this.isUnknown() && decor.hasPrimitive(); }
    public boolean isNotedGeneric() { return !this.isUnknown() && decor.hasGeneric(); }
    public TypeName noteGeneric() { return this.withNotes(Generic); }

    private final List<String> headNames = emptyList(String.class);
    protected int headCount() { return headNames.size(); }
    public boolean isUnpackaged() { return headNames.isEmpty(); }
    protected String headMost() { return (this.isUnpackaged()) ? Empty : headNames.get(0); }
    public boolean fromElementaryPackage() { return Primitive.fromElementaryPackage(headMost() + Dot); }

    private final List<String> tailNames = emptyList(String.class);
    protected int tailCount() { return tailNames.size(); }
    protected String tailMost() { return tailNames.isEmpty() ? Empty : tailNames.get(tailCount() - 1); }
    private static boolean isCapitalized(String name) { return !isEmpty(name) && isUpperCase(name.charAt(0)); }

    protected boolean arrayedType = false;
    public boolean isArrayed() { return arrayedType; }
    public TypeName makeArrayed(boolean value) { this.arrayedType = value; return this; }

    public boolean isAny() { return Any.equals(tailMost()); }
    public boolean isWild() { return Wild.equals(tailMost()); }
    public boolean isUnknown() { return tailMost().isEmpty(); }
    public boolean isNested() { return !this.isUnknown() && tailCount() > 1; }
    public boolean isGeneric() { return isGenericType() || isNotedGeneric(); }
    public boolean isGenericType() { return tailMost().endsWith(TypeSuffix); }

    static final String SigsReport = "types %s %s %s %s";
    protected boolean reportMatch(TypeName type, boolean matched) {
        String similarity = (matched ? "~=" : "!=");
        String realName = Name.asMetaMember(tailName());
        String gen = (isGeneric() || type.isGeneric() ? "generically" : "");
        whisper(format(SigsReport, realName, similarity, type.shortName(), gen));
        return matched;
    }

    public boolean matches(TypeName match) {
        boolean result = false;
        if (isArrayed() && !match.isArrayed()) {
            return reportMatch(match, false);
        }

        if (!isArrayed() && match.isArrayed()) {
            return reportMatch(match, false);
        }

        String realName = Name.asMetaMember(tailName());
        if (isGeneric() && match.isGeneric()) {
            return reportMatch(match, true);
        }

        if (isUnpackaged()) {
            result = match.tailName().startsWith(realName);
            if (result) { reportMatch(match, true); }
            result = realName.equals(match.tailName());
            return reportMatch(match, result);
        }

        if (match.isUnpackaged()) {
            result = realName.startsWith(match.tailName());
            if (result) { reportMatch(match, true); }
            result = realName.equals(match.tailName());
            return reportMatch(match, result);
        }

        realName = Name.asMetaMember(typeName());
        result = match.tailName().startsWith(realName);
        if (result) { reportMatch(match, true); }
        result = realName.equals(match.typeName());
        return reportMatch(match, result);
    }

    protected boolean matches(Named item) { return matches(item.fullName()); }
    public boolean matches(String match) {
        return !isEmpty(match) && match.equals(match.contains(Dot) ? fullName() : shortName()); }

    @Override public int hashCode() { return fullName().hashCode(); }
    @Override public boolean equals(Object candidate) {
        if (hasNone(candidate)) return false;
        if (candidate instanceof TypeName) return matches((TypeName)candidate);
        if (candidate instanceof String) return matches((String)candidate);
        if (candidate instanceof Named) return matches((Named)candidate);
        return false;
    }

    public static Class<?> findPrimitiveClass(String name) { return nullOrTryQuietly(n -> Class.forName(n), name); }
    public Class<?> findPrimitiveClass() { return findPrimitiveClass(fullName()); }
    public Class<?> findClass() { return nullOr(f -> f.primitiveClass(), findType()); }
    public Typified findType() {
        if (this.isAny() || this.isWild() || this.isUnknown()) return null;
        if (this.isGeneric()) return ObjectType().findType();
        return Library.findFace(typeName()); }

    @Override public String name() { return fullName(); }
    @Override public String shortName() { return tailName(); }
    @Override public String fullName() {
        if (this.isUnknown()) return RootType().fullName();
        String packageName = packageName();
        return packageName.isEmpty() ? tailName() :
                Name.formType(packageName, tailName()); }

    public String hootPackage() { return packageName().replace(Dot, Blank); }
    public String hootName() {
        String fullName = fullName();
        if (!Name.isMetaNamed(fullName)) return fullName.replace(Dot, Blank);
        String tail = Name.isMetaclassNamed(fullName)? " class" : " type";
        return Name.withoutMeta(fullName).replace(Dot, Blank) + tail; }

    public String javaName() {
        String fullName = fullName();
        return fullName.replace(Blank, Dot);
    }

    public String convertedTail() { return Primitive.convertType(packagedTail()); }
    public String packagedTail() {
        String packageName = packageName();
        return packageName.isEmpty() ? markedUpTail() :
                Name.formType(packageName, markedUpTail()); }


    private String tailMark() { return isWild() ? Dot : Dollar; }
    public String markedUpTail() { return joinWith(tailMark(), tailNames); }

    private String trailer() { return isArrayed() ? Arrayed : Empty; }
    private String tailName() { return markedUpTail() + trailer(); }

    public String typeName() {
        if (isUnknown()) return RootType().shortName();

        String fullName = fullName();
        if (Primitive.convertsType(fullName)) {
            return Primitive.convertType(fullName);
        }

        if (!isNested()) {
            if (isAny()) return Quest;
            if (isPrimitiveType()) return tailName().toLowerCase();
        }

        return fullName;
    }

    public String packageName() {
        String packageName = Name.formType(headNames);
        return this.isUnpackaged() ? Empty : fromElementaryPackage() ? packageName.toLowerCase() : packageName; }

    public boolean isObjectType() { return this.matches(ObjectType()); }
    public boolean isRootType() { return this.matches(RootType()); }
    public boolean isPrimitiveType() {
        if (this.isUnknown()) return false;
        if (isUnpackaged()) return Primitive.isPrimitiveType(tailMost());
        if (isElementaryType() || !fromElementaryPackage()) return false;
        return Primitive.isPrimitiveType(tailMost()); }

    public boolean isElementaryType() { return Primitive.isElementaryType(fullName()); }
    public boolean isEraseableType() { return Primitive.isEraseableType(tailMost()); }

    public static TypeName fromCache(String... names) {
        if (hasNo(names)) return EmptyType;
        TypeName test = TypeName.with(names);
        String typeName = test.typeName();
        if (!TypeCache.containsKey(typeName) && !test.isUnpackaged()) {
            TypeCache.put(typeName, test);
        }
        return TypeCache.get(typeName);
    }

    private static final String Quest = "?";
    private static final String Any = "Any";
    private static final String Wild = "*";
    private static final String Dollar = "$";

    public static final Class<?> JavaRoot = java.lang.Object.class;
    public static final TypeName ObjectType = TypeName.from(JavaRoot);
    public static final TypeName VoidType = TypeName.from(void.class);
    public static final TypeName EmptyType = new TypeName(emptyList(String.class), emptyList(String.class));
    static final HashMap<String, TypeName> TypeCache = emptyMap(TypeName.class);
    static {
        TypeCache.put(Empty, EmptyType);
        TypeCache.put(VoidType.typeName(), VoidType);
        TypeCache.put(ObjectType.typeName(), ObjectType);
    }

    public static TypeName ObjectType() { return TypeName.fromCache(JavaRoot.getCanonicalName()); }
    public static TypeName RootType() { return TypeName.fromCache(Hoot, Behaviors, Object); }
    public static TypeName ArrayType() { return TypeName.fromCache(Hoot, Collections, Array); }

    /**
     * Resolves a type from its references.
     */
    public static interface Resolver extends Logging {

        public TypeName resolveTypeNamed(Named reference);

    } // Resolver

} // TypeName
