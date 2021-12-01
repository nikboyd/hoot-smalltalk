package Hoot.Runtime.Maps;

import java.io.*;
import java.util.*;

import Hoot.Runtime.Names.Name;
import Hoot.Runtime.Faces.Named;
import Hoot.Runtime.Names.TypeName;
import Hoot.Runtime.Behaviors.Mirror;
import Hoot.Runtime.Behaviors.Typified;
import static Hoot.Runtime.Functions.Utils.*;
import static Hoot.Runtime.Names.Operator.*;
import static Hoot.Runtime.Maps.ClassPath.*;
import static Hoot.Runtime.Names.Primitive.*;

/**
 * Maintains references to all classes and interfaces imported from external packages.
 * Packages are located relative to the system class path established by the Java environment.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 1999,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 * @see <a href="https://gitlab.com/hoot-smalltalk/hoot-smalltalk/tree/master/LICENSE.txt">LICENSE for more details</a>
 */
public class Library implements TypeName.Resolver {

    protected Library() { }
    public static final Library CurrentLib = new Library();
    public File locate(String folder) { return CurrentPath.locate(folder); }
    public boolean canLocate(Package aPackage) { return CurrentPath.canLocatePackage(aPackage); }
    public static Typified findFace(String faceName) { return CurrentLib.faceNamed(faceName); }

    // all uses, including tests
    public static void loadBasePaths(List<File> basePaths) {
        CurrentLib.configurePaths(basePaths);
        CurrentLib.loadStandardLibs();
        CurrentLib.loadNeededHootLibs();
    }

    void whileShowingOnlyDots(Runnable r) {
        try {
            clear(); CurrentPath.showOnlyDots(true); r.run();
        }
        finally {
            CurrentPath.showOnlyDots(false);
            printLine();
        }
    }

    void withoutLoadReports(Runnable r) {
        try {
            Package.reportLoads(false); r.run();
        }
        finally {
            Package.reportLoads(true);
        }
    }

    void loadStandardLibs() {
        whileShowingOnlyDots(() -> {
            mapStandardPaths();
            loadRootPackages();
            loadConfiguredPaths();
        });
    }

    void loadBasicLibs() {
        whileShowingOnlyDots(() -> {
            withoutLoadReports(() -> {
                mapStandardPaths();
                loadRootPackages();
            });
        });
    }

    public static final String[] HootLibs = { // libs in transitive dependence order
        "libs-hoot", "libs-smalltalk", "hoot-compiler", "hoot-runtime", "hoot-abstracts", };

    public static List<String> reverseHootLibs(int count) { return reverseList(listHootLibs(count)); }
    public static List<String> listHootLibs(int count) { return wrapLast(count, HootLibs); }

    public void loadAllHootLibs() { // all libs including compiler
        withoutLoadReports(() -> { clear(); CurrentPath.mapLibs(reverseHootLibs(5)); }); }

    public void loadHootSmalltalkLibs() { // only standard faces + compiler
        loadBasicLibs(); withoutLoadReports(() -> { CurrentPath.mapLibs(reverseHootLibs(4)); }); }

    public void loadHootStandardLibs() { // all libs without compiler
        withoutLoadReports(() -> {
            List<String> list = reverseHootLibs(5); list.remove(HootLibs[2]);
            clear(); CurrentPath.mapLibs(list);
        });
    }

    public void loadHootCompilerLibs() { // runtime libs with compiler
        withoutLoadReports(() -> { clear(); CurrentPath.mapLibs(reverseHootLibs(3)); }); }

    public void loadHootRuntimeLibs() { // only runtime libs
        withoutLoadReports(() -> { clear(); CurrentPath.mapLibs(reverseHootLibs(2)); }); }

    void loadNeededHootLibs() {
        // just what's needed for this library
        int limit = HootLibs.length + 1;
        for (int count = 1; count < limit; count++) {
            CurrentPath.mapLibWhenNot(listHootLibs(count));
        }
        printLine();
    }

    Map<String, Typified> faces = emptyMap(Typified.class);
    public int countFaces() { return distinctFaces().size(); }
    public boolean hasFace(String fullName) { return faces.containsKey(fullName); }
    public void removeFace(String faceName) { faces.remove(faceName); }
    public Typified faceFrom(Named reference) { return faceNamed(reference.fullName()); }
    public Set<String> distinctFaces() { return selectSet(faces.keySet(), f -> f.contains(Dot)); }
    public Typified faceNamed(String fullName) {
        if (hasNo(fullName)) return Mirror.emptyMirror();
        String faceName = Name.typeName(fullName);
        String packageName = Name.packageName(fullName);
        return packageName.isEmpty() ? faces.get(faceName) :
                packageNamed(packageName).faceNamed(faceName); }

    Map<String, Package> packages = emptyMap(Package.class);
    protected Map<String, Package> packages() { return this.packages; }
    protected Collection<Package> allPackages() { return packages().values(); }
    public int countPackages() { return packages().size(); }
    public Set<String> packageNames() { return packages().keySet(); }
    public void clear() { CurrentPath.clear(); faces.clear(); packages().clear(); whisper("cleared library"); }
    public Package packageNamed(String packageName) {
        if (packages().containsKey(packageName)) {
            return packages().get(packageName);
        }

        Package result = new Package(packageName);
        packages().put(packageName, result);
        return result;
    }

    File sourceBase;
    public String sourcePath() { return sourceBase.getAbsolutePath(); }
    public boolean whenSource(String libName) { return sourcePath().contains(libName); }

    File targetBase;
    public String targetPath() { return targetBase.getAbsolutePath(); }

    void configurePaths(List<File> basePaths) {
        sourceBase = basePaths.get(0);
        targetBase = basePaths.get(1);
    }

    public List<File> configuredPaths() { return wrap(sourceBase, targetBase); }
    public void loadConfiguredPaths() {
        List<File> list = configuredPaths(); Collections.reverse(list);
        list.forEach(path -> CurrentPath.mapPath(path));
    }

    // classes whose names are shared by both Hoot and Java
    public void removeShadowedClasses() { wrap(ShadowedClasses).forEach(c -> removeFace(c)); }
    private static final String[] ShadowedClasses = {
        "Object", "Boolean", "Character", "String",
        "Number", "Double",  "Float",  "Integer",
        "Class",  "Exception", "Error",  "Array",
    };

    public static final String[] RootPackages = { "java.lang", "java.lang.reflect", };
    public void loadRootPackages() { wrap(RootPackages).forEach(p -> packageNamed(p).loadFaces()); }
    public void mapStandardPaths() { CurrentPath.mapStandardPaths(); }

    public void loadEmptyPackages() { selectSet(allPackages(), p -> p.needsLoad()).forEach(p -> p.loadFaces()); }
    public int countPackagedFaces() { return reduce(map(allPackages(), p -> p.countFaces()), Integer::sum, 0); }
    Set<Package> includedPackages() { return selectSet(allPackages(), p -> !excludesPackage(p.name())); }

    public void addFace(Typified face) {
        String fullName = face.fullName();
        String typeName = Name.typeName(fullName);
        String packageName = Name.packageName(fullName);
        if (excludesPackage(packageName)) return;

        faces.put(fullName, face);
        faces.put(typeName, face);
        if (!packageName.isEmpty() && typeName.startsWith(packageName)) {
            typeName = typeName.substring(packageName.length() + 1);
            faces.put(typeName, face);
        }

        if (Name.isMetaNamed(fullName)) {
            faces.put(fullName.replace(Dollar, Dot), face);
            faces.put(typeName.replace(Dollar, Dot), face);
        }

        whisper("added " + fullName + " to Library");
    }

    static final String[] ExcludedLibs = {
        "org.apache", "org.slf4j", "org.abego", "org.stringtemplate",
        "st4hidden", "org.hamcrest", "org.eclipse", //"", "",
        "com.google", "org.junit", "org.codehaus", "org.antlr", "junit",
    };
    static final List<String> Exclusions = wrap(ExcludedLibs);
    boolean excludesPackage(String packageName) { return matchAny(Exclusions, ex -> packageName.startsWith(ex)); }


    public Class resolveType(Named reference) { return nullOr(f -> f.primitiveClass(), faceFrom(reference)); }
    @Override public TypeName resolveTypeNamed(Named reference) { return TypeName.fromOther(faceFrom(reference)); }

//    public boolean resolves(Named reference) {
//        if (reference.name().toString().isEmpty()) return false;
//        if (reference.name().equals(Primitive)) return true;
////        if (reference.isElementary()) return true;
////        if (reference.isGlobal()) return true;
//
//        String faceName = Name.typeName(reference.name().toString());
//        if (faceName.isEmpty()) return false;
//
//        if (Name.isMetaNamed(faceName)) {
//            faceName = Name.asMetaMember(faceName);
//        }
//
//        String packageName = Name.packageName(reference.fullName());
//        if (!packageName.isEmpty()) {
//            Typified face = faceFrom(reference);
//            if (face != null) return true;
//        }
//
//        boolean result = faces.containsKey(faceName);
//        if (result) {
////            System.out.println("Library resolved " + symbol);
//        }
//        else {
////            System.out.println("Library can't resolve " + faceName + " from " + reference.name());
//        }
//        return result;
//    }

//    public String resolveTypeName(Named reference) {
//        Class faceClass = resolveType(reference);
//        if (faceClass != null) {
//            if (Mirror.forClass(faceClass).hasMetaclass()) {
//                return MetaclassType().fullName();
//            } else {
//                return faceClass.getCanonicalName();
//            }
//        }
//
//        String symbol = Name.typeName(reference.name().toString());
//        if (faces.containsKey(symbol)) {
//            return MetaclassType().fullName();
//        }
//        return RootType().fullName();
//    }

    public static final String SourceFileType = ".hoot";
    public static FilenameFilter SourceFileFilter =
        ((File dir, String name) -> name.endsWith(SourceFileType));

    public static final String TargetFileType = ".java";
    public static FilenameFilter TargetFileFilter =
        ((File dir, String name) -> name.endsWith(TargetFileType));

    static final String DiscoveryReport = "%s found %d faces";
    public void reportFacesWhen(String libName) { if (whenSource(libName)) reportFaces(); }
    public void reportFaces() { reportFacesDiscovered("discovery", reportedFaces()); }

    List<String> reportedFaces() { return sortList(copyList(collectedFaces())); }
    Set<String> collectedFaces() {
        return collectSet(fs -> includedPackages().forEach(p -> fs.addAll(p.qualifiedFaceNames()))); }

    void reportFacesDiscovered(String reportName, List<String> results) {
        report(Empty);
        report(format(DiscoveryReport, reportName, results.size()));
        if (!results.isEmpty()) {
            results.forEach(faceName -> report(faceName));
        }
    }

    static final String PackReport = "%s has %d faces";
    static final String CountReport = "packs: %d, faces: %d, packedFaces: %d";
    public void reportPackagedFaces() {
        int faceCount = countFaces();
        int packCount = countPackages();
        int pkgdCount = countPackagedFaces();
        report(format(CountReport, packCount, faceCount, pkgdCount));

        includedPackages().forEach(p -> {
            report(format(PackReport, p.name(), p.countFaces()));
        });
    }

} // Library
