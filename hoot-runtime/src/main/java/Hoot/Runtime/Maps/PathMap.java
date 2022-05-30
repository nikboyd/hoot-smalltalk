package Hoot.Runtime.Maps;

import java.io.*;
import java.util.*;

import static Hoot.Runtime.Maps.Package.*;
import static Hoot.Runtime.Names.Operator.Dot;
import static Hoot.Runtime.Functions.Utils.*;
import Hoot.Runtime.Faces.Logging;

/**
 * Maps the classes located in an element of a class path.
 * Locates all package folders relative to a base path from the class path.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 1999,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class PathMap implements Logging {

    public PathMap(String folderPath) { basePath = folderPath; }
    protected String basePath; // the absolute base path mapped by this instance
    static String normalPath(String p) { return Package.normalPath(p); }
    String normalBase() { return normalPath(basePath); }

    public File locate(String folder) { File f = new File(normalBase(), normalPath(folder)); return (f.exists() ? f : null); }
    File locateRelative(String folderPath) { // all paths are relative to the basePath
        if (folderPath.isEmpty()) return new File(normalBase());
        return normalPath(folderPath).startsWith(normalBase()) ? 
            new File(normalPath(folderPath)) : new File(normalBase(), normalPath(folderPath)) ; }

    // maps a package folder name to a set of class names.
    protected Map<String, List<String>> map = new HashMap<>();
    public List<String> classesInPackage(Package p) { return classesInFolder(p.pathname()); }
    public List<String> classesInFolder(String n) {
        if (map.isEmpty()) load(); return map.getOrDefault(n, emptyList(String.class)); }

    // maps a face name to its folder path
    protected Map<String, String> faceMap = emptyWordMap();
    String locateFace(String faceName) { return faceMap.get(faceName); }
    public boolean hasFace(String faceName) { return faceMap.containsKey(faceName); }
    String formatPath(String folder, String faceName) { return folder + Package.separator() + faceName; }

    public List<String> listFaces() { return collectList(list -> faceMap.forEach((k,v) -> list.add(formatPath(v,k)))); }
    public Set<Package> getPackages() { return collectSet(set -> faceMap.forEach((k,v) -> set.add(packageNamed(v)))); }

    Package packageNamed(String name) { return Package.named(Package.nameFrom(name)); }
    private String packageForFace(String faceName) { return Package.nameFrom(locateFace(faceName)); }
    public String packageContaining(String faceName) { return hasFace(faceName) ? packageForFace(faceName) :  Empty ; }

    public static final String ClassFileType = ".class";
    static String classNameWithoutType(String n) { return n.substring(0, n.length() - ClassFileType.length()); }
    public static boolean namesClass(String n) { return hasSome(n) && n.endsWith(ClassFileType); }
    public static String className(String n) { return (!namesClass(n)) ? n : classNameWithoutType(n); }

    // skip this class that causes problems
    static final String SkippedClass = "MulticastDnsAdvertiser";
    protected void addMapped(String folderName, String fileName) {
        String faceName = className(fileName);
        if (SkippedClass.equals(faceName)) return;

        List<String> faceNames = map.getOrDefault(folderName, emptyList(String.class));
        packageNamed(folderName).loadFace(faceName);
        faceMap.put(faceName, folderName);
        faceNames.add(faceName);
    }

    public void load() { load(Empty); reportAdded(); }
    protected void load(String folderName) {
        File directory = locateRelative(folderName);
        String[] list = directory.list();

        // note: depth first recursive descent of folders
        int count = list.length;
        for (int n = 0; n < count; n++) {
            String fileName = list[n];
            File file = new File(directory, fileName);
            if (file.isDirectory()) { // load subdirectory
                load(folderName.isEmpty() ? fileName : file.getAbsolutePath());
            }
            else if (namesClass(fileName)) {
                addMapped(folderName, classNameWithoutType(fileName));
            }
        }
    }

    public void println() { System.out.println(); }
    static final String AddReport = "loaded %d faces into %s";
    protected void reportAdded() {
        map.forEach((folderName, faceNames) -> {
            if (ClassPath.matchHoots(folderName))
                report(format(AddReport, faceNames.size(), folderName));
        });
    }

} // PathMap
