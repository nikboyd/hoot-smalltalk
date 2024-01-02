package Hoot.Compiler.Scopes;

import java.util.*;

import Hoot.Runtime.Names.*;
import Hoot.Runtime.Faces.Named;
import Hoot.Runtime.Maps.Package;
import Hoot.Runtime.Maps.Library;
import Hoot.Runtime.Behaviors.*;
import Hoot.Runtime.Emissions.*;

import static Hoot.Runtime.Emissions.Emission.*;
import static Hoot.Runtime.Behaviors.HootRegistry.*;
import static Hoot.Runtime.Names.Name.Metaclass;
import static Hoot.Runtime.Names.Name.Metatype;
import static Hoot.Runtime.Names.Primitive.Dollar;
import static Hoot.Runtime.Names.Operator.Dot;
import static Hoot.Runtime.Names.Keyword.Nil;
import static Hoot.Runtime.Functions.Exceptional.*;
import static Hoot.Runtime.Functions.Utils.*;

/**
 * A class or type (interface) definition.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 1999,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class Face extends Scope implements Typified, TypeName.Resolver, ScopeSource {

    public Face(Scope container) { super(container); }
    protected String cachedDescription; // helps debugging

    @Override public int hashCode() { return isSigned() ? signature().hashCode() : super.hashCode(); }
    protected boolean equals(Face f) { return isSigned() && f.isSigned() && signature().equals(f.signature()); }
    @Override public boolean equals(Object face) {
        return hasAny(face) && getClass() == face.getClass() && falseOr(f -> this.equals(f), (Face) face); }

    @Override public boolean isEmpty() { return !isSigned(); }
    @Override public void clean() {
        super.clean();
        cleanMethods();

        buildMethodMap();
        if (this.isInterface()) {
            return;
        }

        if (this.hasMetaface()) {
            metaFace().clean();
        }
    }

    public static Face currentFace() { return from(Scope.current()); }
    public static Face from(Item item) { return nullOr(f -> (Face)f, item.facialScope()); }
    @Override public Face makeCurrent() { File.currentFile().currentScope(this); return this; }

    public static Typified named(TypeName type) { return named(type.fullName()); }
    public static Typified named(String faceName) { return Library.findFace(faceName); }
    public static Typified from(Class type) { return named(type.getCanonicalName()); }
    public Typified faceNamed(String name) { return File.from(this).faceNamed(name); }

    /**
     * Java package root.
     */
    protected static final String rootPackage = "java.";

    /**
     * Separates metaClass names from their associated class names.
     */
    protected static final String metaSeparator = ".";

    static final String ClassType = "class";
    static final String TypeType = "interface";
    public String type() { return this.isInterface() ? TypeType : ClassType; }

    /**
     * Returns the root base metaClass name.
     *
     * @return the root base metaClass name.
     */
    public static String baseMetaclassName() {
        return "smalltalk.behavior.Class";
    }

    /**
     * Returns a meta-name for the supplied (name).
     *
     * @param name the name of some Hoot entity.
     * @return a meta-name
     */
    public static String metaName(String name) {
        return name + Name.Metatype;
    }

    /**
     * Returns whether the supplied (faceName) has a metaFace.
     *
     * @param faceName the name of a Hoot face (class or type).
     * @return whether the indicated face has a metaFace
     */
    public static boolean metafaceExists(String faceName) {
        if (faceName.isEmpty()) {
            return false;
        }
        if (faceName.equals(Nil)) {
            return false;
        }
        if (faceName.startsWith(rootPackage)) {
            return false;
        }
        Typified aFace = Library.findFace(faceName);
        if (aFace == null) {
            return true; // assumed by default
        }
        Mirror mirror = aFace.typeMirror();
        if (mirror == null) {
            return true; // assumed by default
        }
        return mirror.hasMetaclass();
    }

    /**
     * Returns a metaClass name for the supplied (className).
     *
     * @param className the name of a Hoot class.
     * @return a metaClass name
     */
    public static String metaclassName(String className) {
        return (metafaceExists(className) ? metaName(className) : baseMetaclassName());
    }

    /**
     * Returns a metaType name for the supplied (typeName).
     *
     * @param typeName the name of a Hoot type.
     * @return a metaType name
     */
    public static String metatypeName(String typeName) {
        return (metafaceExists(typeName) ? metaName(typeName) : Empty);
    }


//    static final String TypeFile = "java-type.ftl";
//    static final String ClassFile = "java-class.ftl";
//    public String codeFile() {
//        return isInterface() ? TypeFile : ClassFile;
//    }

    NamedItem faceSignature;
    public boolean isSigned() { return hasAny(faceSignature); }
    public NamedItem signature() {
        if (!isSigned()) { file().parse(); }
        return this.faceSignature; }

    public Face signature(NamedItem signature) {
        if (hasNo(signature)) return this;
        this.faceSignature = signature.inside(this);
        this.cachedDescription = description();

        // now, collect all the related notes here
        notes().noteAll(this.faceSignature.notes().notes());
        reportScope(); return this;
    }

    @Override public String description() { return "Face " + signedDescription(); }
    @Override public String baseName() { return emptyOr(s -> s.baseName(), signature()); }
    public String signedDescription() { return isSigned() ? signature().description() : fileScope().name(); }
    @Override public String name() { return isSigned() ? signature().name() : fileScope().name(); }
    @Override public String shortName() { return signature().shortName(); }
    @Override public String fullName() {
        return typePackage().qualify(this.isMetaface() ? metaName() : name()); }

    @Override public List<String> knownTypes() {
        return this.isMetaface() ?
                this.typeFace().signature().knownTypes() :
                this.signature().knownTypes(); }

    List<ProtocolScope> memberScopes = emptyList(ProtocolScope.class);
    public List<ProtocolScope> memberScopes() {
        return this.memberScopes;
    }

    public void addScope(ProtocolScope scope) {
        this.memberScopes.add(scope);
    }

    List<Method> methods = emptyList(Method.class);
    public int methodCount() { return methods.size(); }
    public List<Method> methods() { return copyList(methods); }
    protected void cleanMethods() { methods.forEach(m -> m.clean()); }

    HashMap<String, Method> methodMap = emptyMap(Method.class);
    private void buildMethodMap() {
        if (!methodMap.isEmpty()) return;

        methods().forEach(m -> {
            int count = m.argumentCount();
            String sig = m.matchSignature();
            methodMap.put(sig, m);
            if (count > 0) {
                sig = m.matchErasure();
                methodMap.put(sig, m);
            }
        });
    }

    public List<Method> cleanedMethods() {
        ArrayList<Method> results = emptyList(Method.class);
        methods.stream()
        .filter((m) -> (!m.isEmpty()))
        .forEachOrdered((m) -> {
            if (m.argumentCount() > 0) {
                results.add(methodMap.get(m.matchErasure()));
            }
            else {
                results.add(methodMap.get(m.matchSignature()));
            }
        });

        return results;
    }

    public static Face from(Typified source) {
        return null;
    }

    protected Face metaFace = null;
    @Override public Typified $class() { return this.metaFace; }
    @Override public boolean hasMetaface() { return this.metaFace != null; }
    public Face addMetaface() { if (!this.hasMetaface()) metaFace(new Face(this)); return this.metaFace; }
    public Face metaFace() { return this.metaFace; }
    public void metaFace(Face aFace) {
        this.metaFace = aFace;
        this.metaFace.signature(signature().metaSignature());
    }

    public Face selectFace(String selector) {
        // selector == 'class' or empty from parser
        if (selector.isEmpty()) return typeFace().makeCurrent();
        if (this.isMetaface()) return makeCurrent();
        // add metaFace (1st encounter)
        return addMetaface().makeCurrent();
    }

    public Typified baseFace() { return faceNamed(typeFace().baseName()); }
    @Override public Typified superclass() { return faceNamed(baseName()); }
    @Override public Class<?> primitiveClass() { return typeClass(); }

    public String metaFaceType() { return (this.isInterface() ? Metatype : Metaclass); }

    @Override public String comment() { return signature().comment(); }
    @Override public Scope facialScope() { return this; }

    /**
     * Adds a (method) to those defined by this face.
     *
     * @param method a Hoot method definition.
     */
    public void addMethod(Method method) {
        method.container(this);
        methods.add(method);
    }

    /**
     * Adds a new method to those defined by this face.
     */
    public void addMethod() {
        addMethod(new Method(this));
    }

    public boolean isPackaged() {
        return (typePackage() instanceof Package);
    }

    /**
     * Returns whether this face is derived from a Java class.
     *
     * @return whether this face is derived from a Java class.
     */
    public boolean hasElementaryBase() {
        return !baseMirror().hasMetaclass();
    }

    public boolean isInnard() { return (container() instanceof Block); }

    @Override public boolean isAbstract() { return this.isInterface() || super.isAbstract(); }
    @Override public boolean isInterface() { return signature().isInterface(); }
    @Override public boolean isFacial() { return true; }
    @Override public boolean isReflective() { return false; }
    @Override public boolean resolves(Named reference) { return hasLocal(reference.name().toString()); }

    public boolean isMetaface() { return containerScope().isFacial(); }
    public boolean isMetaclassBase() { return this.name().equals(Metaclass + "Base"); }

    public String className() { return this.isMetaface() ? typeFace().name() + " class" : name(); }
    private String metaName() { return typeFace().name() + Dollar + metaFaceType(); }
    public String defaultName() { return this.isMetaface() ? typeFace().name() : name(); }

    @Override public int nestLevel() { return scope().nestLevel() + 1; }
    @Override public boolean hasHeritage() { return (hasAny(baseName()) && !Nil.equals(baseName())); }
    @Override public boolean hasNoHeritage() { return (hasNo(baseName()) || Nil.equals(baseName())); }

    @Override public List<Typified> simpleHeritage() { return signature().simpleHeritage(); }
    @Override public List<Typified> typeHeritage() { return signature().typeHeritage(); }

    public String basePackageName() { return Name.packageName(fullBaseName()); }
    public String fullBaseName() { return emptyOr(f -> f.fullName(), baseFace()); }

    public Class typeClass() { return TypeName.findPrimitiveClass(typeFace().fullName()); }
    public Class baseClass() { return TypeName.findPrimitiveClass(fullBaseName()); }
    public Mirror baseMirror() { return Mirror.forClass(baseClass()); }

    @Override public String packageName() { return typePackage().fullName(); }
    public Package typePackage() { return File.from(this).facePackage(); }
    public Face typeFace() { return (this.isMetaface() ? container().asType(Face.class) : this); }
    public String typeName() { return (this.isMetaface() ? metaName(typeFace().name()) : name()); }
    @Override public Mirror typeMirror() { return Mirror.forClass(typeClass()); }

    public String metaInstance() {
        return (metaFace == null
                ? "new Metaclass( " + typeFace().name() + ".$class() );"
                : "new " + metaFace.name() + "( " + name() + ".class );");
    }

    /**
     * Returns a revised identifier derived from the supplied (identifier).
     *
     * @param identifier identifies a named entity.
     * @return a revised identifier derived from the supplied (identifier).
     */
    @Override
    public String qualify(String identifier) {
        if (identifier.startsWith("this") || identifier.startsWith("super")) {
            return typeName() + Dot + identifier;
        } else {
            return typeName() + ".this." + identifier;
        }
    }

    private final Map<String, Typified> heritageMap = emptyMap(Typified.class);
    private void buildHeritageMap() {
        if (!heritageMap.isEmpty()) return;
        fullInheritance().stream().map((type) -> {
            heritageMap.put(type.fullName(), type);
            return type;
        }).forEachOrdered((type) -> {
            heritageMap.put(type.shortName(), type);
        });
    }

    @Override
    public String matchSignatures(Signed m) {
        buildMethodMap();
        String fullSig = m.matchSignature();
        if (methodMap.containsKey(fullSig)) {
            return fullSig;
        }

        String erasedSig = m.matchErasure();
        if (methodMap.containsKey(erasedSig)) {
            return erasedSig;
        }

        String shortSig = m.methodName();
        if (m.argumentCount() > 0) {
            for (String sig : methodMap.keySet()) {
                if (sig.startsWith(shortSig)) {
                    return sig;
                }
            }
        }

        return Empty;
    }

    public Signed resolveMethod(Method m) {
        String s = matchSignatures(m);
        if (!s.isEmpty()) {
            return methodMap.get(s);
        }

        List<Typified> heritage = fullInheritance();
        for (Typified aFace : heritage) {
            s = aFace.matchSignatures(m);
            if (!s.isEmpty()) {
                return aFace.getSigned(s);
            }
        }

        return null;
    }

    @Override public Signed getSigned(Signed s) {
        Signed signed = getSigned(s.matchSignature());
        if (hasAny(signed)) return signed;
        Signed erased = getSigned(s.matchErasure());
        return erased;
    }

    @Override public Signed getSigned(String s) {
        buildMethodMap();
        return methodMap.get(s); }

    @Override public boolean overridenBy(Signed s) { return this.overridenBy((Method)s); }
    public boolean overridenBy(Method m) {
        Face methodFace = m.face();
        if (methodFace.inheritsFrom(this)) {
            Signed result = resolveMethod(m);
            if (result == null) return false;
            return m.overrides(result);
        }
        return false;
    }


    public List<Emission> emitMethods() { return map(methods(), m -> m.emitScope()); }
    @Override public List<Emission> emitLocalVariables() {
        return map(locals().definedSymbols(), v -> emitStatement(v.emitItem())); }

    @Override public Emission emitScope() {
        return emitLibraryType(NoValue, emitSignature(), emitLocalVariables(), emitMetaFace(), emitMethods()); }

    public Emission emitScope(Emission libs) {
        return emitLibraryType(libs, emitSignature(), emitLocalVariables(), emitMetaFace(), emitMethods()); }

    public Emission emitSignature() {
        return this.isMetaface() ? typeFace().signature().emitMetaItem() : signature().emitItem(); }

    public Emission emitMetaFace() {
        if (this.isMetaface()) return null;
        if (this.hasMetaface()) {
            if (this.isInterface()) {
                return emitMetatype(signature().emitMetaItem(), metaFace().emitMethods());
            }
            else {
                return emitMetaclass(signature().name(), signature().baseName(),
                    signature().emitMetaItem(), metaFace().emitLocalVariables(), metaFace().emitMethods());
            }
        }
        return null;
    }

//    public Emission emitMetaInstance() {
//        if (this.isInterface()) {
//            if (!this.isMetaface()) {
//                return emit("TypeMembers");
//            }
//
//            return null;
//        }
//
//        if (this.isMetaface()) {
//            return emit("MetaMembers").name(name());
//        }
//
//        if (this.hasMetaface()) {
//            TypeName rootType = RootType();
//            boolean overrides = !rootType.equals(this) && inheritsFrom(named(rootType));
//            return emit("FaceMembers")
//                    .type(type()).name(name())
//                    .with("metaName", metaFace().name())
//                    .with("member", this.isClassType() ? "metaclass" : "$class")
//                    .with("override", overrides ? "override" : null);
//        }
//
//        return null;
//    }


    public void reportBuildingWrapper(Method m) {
        report(format(BuildingWrapper, className(), m.name()));
    }

    public void reportAlreadyWrapped(Method m) {
        report(format(AlreadyWrapped, className(), m.name()));
    }

    public void reportExtraneousWrapper(Method m) {
        report(format(ExtraneousWrapper, className(), m.name()));
    }

    static final String BuildingWrapper = "Building wrapper method for %s >> %s";
    static final String AlreadyWrapped = "Warning! %s >> %s was declared wrapped, but already has wrapper";
    static final String ExtraneousWrapper = "Warning! %s >> %s was declared wrapped, but needs no wrapper";
}
