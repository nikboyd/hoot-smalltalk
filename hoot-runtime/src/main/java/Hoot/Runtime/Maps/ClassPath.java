package Hoot.Runtime.Maps;

import java.io.*;
import java.util.*;
import static java.util.Collections.*;
import org.eclipse.aether.resolution.ArtifactResult;

import Hoot.Runtime.Names.Name;
import Hoot.Runtime.Faces.Logging;
import static Hoot.Runtime.Functions.Utils.*;
import static Hoot.Runtime.Maps.Discovery.*;
import static Hoot.Runtime.Maps.Library.*;
import static Hoot.Runtime.Names.Operator.Dot;
import static Hoot.Runtime.Names.Primitive.*;

/**
 * Provides a directory of the classes located by the Java class path.
 * Locates packages by their directory names and provides a list of the classes contained in a package.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 1999,2025 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class ClassPath implements Logging {

    protected ClassPath() { }
    public static final ClassPath CurrentPath = new ClassPath();
    static String normalPath(String p) { return Package.normalPath(p); }

    static final String WorkPath = "user.dir";
    public static String workPath() { return systemValue(WorkPath); }

    static File CachedBase = new File(workPath());
    public static File cachedBase() { return CachedBase; }

    public static final String Workspace = "workspace";
    public static final String HootSmalltalk = "hoot-smalltalk";
    static { discoverBase(Workspace, HootSmalltalk); }

    static final String BaseReport = "using default base folder: %s";
    static void reportDefaultBase(File baseCache) {
        cachedDiscovery().report(String.format(BaseReport, baseCache.getAbsolutePath())); }

    static boolean found(File f) { return cachedDiscovery().found(f); }
    static File find(String name, File f) { return hasSome(f) && !found(name, f) ? find(name, f.getParentFile()) : f; }
    static boolean found(String baseName, File f) { return f.getAbsolutePath().endsWith(baseName); }
    static boolean foundAny(File f, String... baseNames) { return matchAny(wrap(baseNames), (n) -> found(n, f)); }
    public static File discoverBase(String... baseNames) {
        File workFolder = new File(workPath());
        File baseFolder = find(baseNames[0], workFolder);

        int index = 0;
        while (!found(baseFolder) && (++index) < baseNames.length)
        { baseFolder = find(baseNames[index], workFolder); }

        File baseCache = baseFolder;
        setSafely(() -> baseCache, (f) -> {
            if (!CachedBase.getAbsolutePath().equals(f.getAbsolutePath()) && !foundAny(f, baseNames)) {
                reportDefaultBase(f); // found a different default
            }
            CachedBase = f;
        });

        return cachedBase();
    }

    List<PathMap> contents = emptyList(PathMap.class);
    public void clear() { contents.clear(); }
    public List<PathMap> contents() { return this.contents; }
    private List<PathMap> reversedPath() { List<PathMap> results = copyList(contents); reverse(results); return results; }

    public List<String> listFaces() { return collectList(fs -> collectFaces(fs)); }
    void collectFaces(Collection<String> fs) { contents().forEach(pathMap -> fs.addAll(pathMap.listFaces())); }


    public void mapLibs(String... libNames) { mapLibs(wrap(libNames)); }
    public void mapLibs(List<String> libNames) { libNames.forEach(libName -> mapLibrary(libName)); }

    public void mapLibWhen(String codePath, String libName) { if (whenSource(codePath)) mapLibrary(libName); }
    public void mapLibWhenNot(String... codePaths) { mapLibWhenNot(wrap(codePaths)); }
    public void mapLibWhenNot(List<String> codePaths) { // when lib not the source
        if (!matchAny(codePaths, s -> whenSource(s))) mapLibrary(codePaths.get(0));
    }

    public boolean whenSource(String libName) { return CurrentLib.sourcePath().contains(libName); }
    public boolean whenTarget(String libName) { return CurrentLib.targetPath().contains(libName); }

    static final String Colon = ":";
    static final String LATEST = Colon + "LATEST";
    static String artifactNamed(String libName) { // default to Hoot if no group provided
        return libName.contains(Colon) ? libName : HootSmalltalk + Colon + libName + LATEST; }

    static final String TargetClasses = "target/classes";
    public void mapLibrary(String libName) {
        String artifactName = artifactNamed(libName);
        ArtifactResult result = Discovery.lookup(artifactName);
        if (hasSome(result)) { // try maven resolution first
            if (Package.ReportLoads) reportMapping(result, artifactName);
            File file = result.getArtifact().getFile();
            if (file.exists()) {
                PathMap m = mapPath(file);
                if (Package.ReportLoads) printLine();
                m.getPackages().forEach(p -> p.loadFaces());
                return; // done with found library
            }
        }

        // try local library class resolution
        File libFolder = new File(cachedBase(), libName);
        if (libFolder.exists()) {
            File classFolder = new File(libFolder, normalPath(TargetClasses));
            if (classFolder.exists()) {
                if (Package.ReportLoads) reportMapping(libName, classFolder);
                PathMap m = mapPath(classFolder);
                if (Package.ReportLoads) printLine();
                m.getPackages().forEach(p -> p.loadFaces());
            }
        }
    }

    public PathMap mapPath(File folder) { boolean appended = true; return addMapped(folder, appended); }
    public PathMap addMapped(File folder, boolean appended) {
        if (hasNo(folder) || !folder.exists()) {
            if (hasSome(folder)) report(folder.getName() + " not loaded");
            return null; // bail out, can't load from non-existent folder
        }

        PathMap map = buildMap(folder);
        if (appended) contents.add(map);
                 else contents.add(0, map);

        map.load();
        reportMapped(); // while mapping
        if (Package.ReportLoads) reportCount(map.listFaces().size());
        return map;
    }

    private PathMap buildMap(File folder) {
        // NOTE: folder may actually locate
        // a ZIP or JAR file that contains a class library!
        return ZipMap.supports(folder.getAbsolutePath()) ?
                new ZipMap(folder.getAbsolutePath()) :
                new PathMap(folder.getAbsolutePath()) ; }

    boolean classPathMapping = false;
    public boolean showDots() { return this.classPathMapping; }
    public boolean showOnlyDots(boolean value) { this.classPathMapping = value; return value; }

    private boolean locatesFace(String faceName, String packageName) {
        return matchAny(classesInPackage(packageName), faceNames ->
            hasSome(faceNames) && faceNames.contains(faceName)); }

    private boolean anyPackageHasFaceNamed(String faceName) {
        return matchAny(reversedPath(), pathMap ->
            hasSome(pathMap.packageContaining(faceName)));
    }

    public boolean canLocateFaceNamed(String fullName) {
        String faceName = Name.typeName(fullName);
        String packageName = Name.packageName(fullName);
        return locatesFace(faceName, packageName) || anyPackageHasFaceNamed(faceName); }

    public boolean canLocatePackage(Package aPackage) { return classesExistInFolder(aPackage.pathname()); }
    private boolean classesExistInFolder(String packagePath) {
        return matchAny(reversedPath(), pathMap ->
            hasSome(pathMap.classesInFolder(packagePath))); }

    public Set<String> classesInPackage(Package aPackage) { return classesInPackage(aPackage.pathname()); }
    private Set<String> classesInPackage(String packageName) {
        return collectSet(results ->
            reversedPath().forEach(pathMap ->
                results.addAll(pathMap.classesInFolder(packageName)))); }

    public java.io.File locate(String folder) {
        for (PathMap map : reversedPath()) {
            File result = map.locate(folder);
            if (hasSome(result)) return result;
        }
        return null;
    }

    static final String StandardPath = "java.class.path";
    String[] standardPaths() { return systemValue(StandardPath).split(Separator); }
    public static String buildPath(String... basePaths) { return joinWith(Separator, wrap(basePaths)); }
    public void mapStandardPaths() {
        reportMapping("CLASSPATH", null); wrap(standardPaths()).forEach(p -> mapPath(new File(p))); }

    static final String ArtReport = "mapping %s = %s ";
    private void reportMapping(ArtifactResult r, String name) {
        printLine(); print(format(ArtReport, name, r.getArtifact().getVersion())); }

    static final String CountReport = " mapped %d faces";
    private void reportCount(int count) { if (!showDots()) print(format(CountReport, count)); }

    static final String MappingReport = "mapping %s ";
    public void reportMapping(String name, File folder) { printLine();
        print(hasNo(folder) ? format(MappingReport, name) : format(ArtReport, name, folder.getAbsolutePath())); }

    public void reportMapped() { if (showDots()) print(Dot); }
    public void println(int count) { while(count-- > 0) printLine(); }
    public void println() { System.out.println(); }

    public static final String PathSeparator = "path.separator";
    public static final String Separator = systemValue(PathSeparator);

    static final String[] Hoots = { "Hoot", "Smalltalk", };
    static final List<String> HootBases = wrap(Hoots);
    public static boolean matchHoots(String faceName) { return matchAny(HootBases, (h) -> faceName.startsWith(h)); }

} // ClassPath
