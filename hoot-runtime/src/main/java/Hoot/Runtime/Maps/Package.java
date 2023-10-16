package Hoot.Runtime.Maps;

import java.io.*;
import java.util.*;

import Hoot.Runtime.Names.Name;
import Hoot.Runtime.Faces.Named;
import Hoot.Runtime.Names.TypeName;
import Hoot.Runtime.Behaviors.Mirror;
import Hoot.Runtime.Behaviors.Typified;
import Hoot.Runtime.Emissions.Item;
import Hoot.Runtime.Faces.UnitFile;
import Hoot.Runtime.Faces.Logging;
import static Hoot.Runtime.Functions.Utils.*;
import static Hoot.Runtime.Functions.Exceptional.*;
import static Hoot.Runtime.Maps.Library.*;
import static Hoot.Runtime.Maps.ClassPath.*;
import static Hoot.Runtime.Names.Keyword.*;
import static Hoot.Runtime.Names.Operator.Dot;
import static Hoot.Runtime.Names.Primitive.Blank;
import static Hoot.Runtime.Names.Primitive.Dollar;
import static Hoot.Runtime.Behaviors.HootRegistry.*;
import static Hoot.Runtime.Exceptions.ExceptionBase.*;
import static Hoot.Runtime.Names.Name.removeTail;
import org.apache.commons.lang3.SystemUtils;
import org.codehaus.plexus.util.StringUtils;

/**
 * A package of class definitions.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 1999,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class Package implements Named, Logging {

    public Package() { this(Empty); }
    public Package(String packageName) { super(); name = packageName; }
    public Package(String packageName, String folderPath) { this(packageName); baseFolder = folderPath; }

    String name;
    @Override public String name() { return name; }
    public void name(String packageName) { name = packageName; }

    public String pathname() { return pathFrom(name); }
    public String parentName() { return Name.packageName(name); }
    public static Package named(String packageName) { return CurrentLib.packageNamed(nameFrom(packageName)); }

    public static final String[] LiteralPackages = { "Hoot.Behaviors", "Hoot.Magnitudes", "Hoot.Collections" };
    public static final List<String> LiteralPackageNames = wrap(LiteralPackages);
    public boolean definesLiterals() { return LiteralPackageNames.contains(name()); }
    public boolean definesMagnitudes() { return LiteralPackages[1].equals(name()); }
    public boolean definesCollections() { return LiteralPackages[2].equals(name()); }
    public boolean definesBehaviors() { return (LiteralPackages[0].equals(name()) || definesSmalltalk()); }
    public boolean definesSmalltalk() { return Smalltalk.equals(name()); }
    public boolean definesRoot() { return RootPackages[0].equals(name()); }

    public static UnitFile.Factory UnitFactory;
    private UnitFile createUnit(String fullName) { return UnitFactory.createUnit(fullName, name()); }
    private Map<String, UnitFile> createPeers() {
        HashMap<String, UnitFile> results = emptyMap(UnitFile.class);
        sourceFaces().forEach(f -> results.put(f, createUnit(f)));
        results.values().forEach(f -> f.peers(results));
        return results;
    }

    public HashMap<String, UnitFile> parseSources() throws Exception {
        HashMap<String, UnitFile> results = emptyMap(UnitFile.class);
        if (UnitFactory == null) return results;

        java.io.File sourceFolder = sourceFolder();
        if (!sourceFolder.exists()) {
            error("failed to locate sources for " + name());
            return results;
        }

        results.putAll(createPeers());
        results.values().forEach(f -> f.addStandardImports());
        results.values().forEach(f -> addFace((Typified)((Item)f).facialScope()));
        results.values().forEach(f -> f.parse());

        return results;
    }

    public List<String> sourceFaces() { return listFaces(sourceFolder(), languageType(), sourceFileFilter()); }
    public List<String> targetFaces() { return listFaces(targetFolder(), TargetFileType, TargetFileFilter); }
    public List<String> listFaces(java.io.File folder, String type, FilenameFilter filter) {
        return map(wrap(folder.list(filter)), f -> removeTail(f, type)); }

    public File sourceFolder() { return new File(CurrentLib.sourcePath(), pathname()); }
    public File targetFolder() { return new File(CurrentLib.targetPath(), pathname()); }

    public File createTarget() {
        File targetFolder = targetFolder();

        if (!targetFolder.exists() && !targetFolder.mkdirs()) {
            error("can't create " + targetFolder.getAbsolutePath());
            return null;
        }

        return targetFolder;
    }

    String baseFolder = Empty;
    public File directory() {
        return (baseFolder.isEmpty()) ? CurrentPath.locate(pathname()) : new File(baseFolder + pathname()); }

    public Set<String> packagedClassNames() {
        return collectSet(results -> {
            results.addAll(CurrentPath.classesInPackage(this));
            if (baseFolder.isEmpty() && definesRoot()) {
                results.add(JavaRoot().getSimpleName());
                results.addAll(wrap(RootExceptions));
            }
        }); }

    public boolean needsLoad() { return packagedClassNames().size() > faceNames().size(); }
    public void loadFaces(Set<String> faceNames) { faceNames.forEach(faceName -> loadFace(faceName)); }

    public void loadFaces() {
        int count = faceNames().size();
        loadFaces(packagedClassNames());

        int sizeNow = faceNames().size();
        if (ReportLoads) reportLoad(name(), sizeNow);
    }

    public static boolean ReportLoads = false;
    public static void reportLoads(boolean value) { ReportLoads = value; }

    static final String CountReport = "%s loaded %d faces";
    void reportLoad(String name, int count) {
        if (ReportLoads) report(format(CountReport, name, count));
        else whisper(format(CountReport, name, count)); }

    public void loadFace(String shortName) {
        runQuietly(() -> {
            Class c = TypeName.findPrimitiveClass(qualify(shortName));
            Mirror m = Mirror.forClass(c);
            addFace(m.isTypical() ? m.reflectedType() : m);
        });
    }

    Map<String, Typified> faces = emptyMap(Typified.class);
    public int countFaces() { return faces.size(); }
    public Set<String> faceNames() { return faces.keySet(); }
    public Map<String, Typified> knownFaces() { return new HashMap<>(faces); }
    public Typified faceNamed(String faceName) { return faces.get(Name.typeName(faceName)); }
    public Set<String> qualifiedFaceNames() { return mapSet(faceNames(), f -> qualify(f)); }

    public void addFace(Typified face) {
        String packageName = face.packageName();
        String typeName = Name.typeName(face.fullName());
        if (typeName.startsWith(packageName)) {
            typeName = typeName.substring(packageName.length() + 1);
        }

        if (!CurrentLib.hasFace(typeName)) { // ??
            registerFace(face, typeName);
        }
        else { // ??
            registerFace(face, typeName);
        }
    }

    protected void registerFace(Typified face, String typeName) {
        faces.put(typeName, face);
        CurrentLib.addFace(face);
        registerMetaface(face);
        if (Name.isMetaNamed(face.fullName())) {
            packageMetaface(face);
        }
    }

    protected void registerMetaface(Typified face) {
        if (face.hasMetaface()) {
            Typified metaFace = face.$class();
            CurrentLib.addFace(metaFace);
            packageMetaface(metaFace);
        }
    }

    protected void packageMetaface(Typified metaFace) {
        if (metaFace.packageName().equals(name())) {
            addMetaface(metaFace);
        }
        else {
            Package.named(metaFace.packageName()).addMetaface(metaFace);
        }
    }

    protected void addMetaface(Typified metaFace) {
        String metaName = Name.typeName(metaFace.fullName());
        String packageName = Name.packageName(metaFace.fullName());
        if (metaName.startsWith(packageName)) {
            metaName = metaName.substring(packageName.length() + 1);
        }
        faces.put(metaName.replace(Dollar, Dot), metaFace);
        faces.put(metaName.replace(Dot, Dollar), metaFace);
    }

    public Map<String, TypeName> faceTypes() {
        HashMap<String, TypeName> results = emptyMap(TypeName.class);
        faceNames().forEach((faceName) -> {
            results.put(faceName, TypeName.fromName(qualify(faceName)));
        });
        return results;
    }

    public static final String WildCard = ".*";
    public static boolean namesAllFaces(String importName) { return importName.endsWith(WildCard); }
    public static String nameWithout(String tail, String name) { return removeTail(name, tail); }
    public static String nameFrom(File baseFolder, File packageFolder) {
        return nameFrom(packageFolder.getPath().substring(baseFolder.getPath().length())); }

    public static final String Slash = "/"; // cover Unix paths
    public static final String BackSlash = "\\"; // cover Windows too! --nik
    static String slashToDot(String packagePath) {
        return packagePath.replace(BackSlash, Blank).trim().replace(Slash, Blank).trim().replace(Blank, Dot); }
    public static String nameFrom(String packageName) { return slashToDot(nameWithout(WildCard, packageName)); }
    public static String pathFrom(String packageName) { return packageName.replace(Dot, separator()); }
    public static String separator() { return SystemUtils.IS_OS_WINDOWS ? BackSlash : Slash; }
    public static String normalPath(String folderPath) {
        return SystemUtils.IS_OS_WINDOWS ? folderPath.replace(Slash, BackSlash) : folderPath; }

    public void reportReflectively() {
        report("");
        report(fullName());
        faceNames().forEach(n -> {
            Typified type = faceNamed(n);
            if (hasSome(type)) {
                report(format(Clss, type.name()));
                reportHeritage(type.simpleHeritage(), Xtds);
                reportHeritage(type.typeHeritage(), Imps);
            }
        });
    }

    static final String Comma = ",";
    static final String Clss = "  %s";
    static final String Xtds = "       extends %s";
    static final String Imps = "    implements %s";
    static final String Tabs = "               %s";
    protected void reportHeritage(List<Typified> list, String report) {
        if (!list.isEmpty()) {
            String text = format(report, Typified.names(list).toString());
            int count = StringUtils.countMatches(text, Comma);
            if (count < 4) { report(text); return; } // exit early

            String[] parts = text.split(Comma); // take 1st three
            report(joinWith(Comma, wrap(parts[0], parts[1], parts[2])));

            text = parts[3]; // take last many
            for (int index = 4; index < parts.length; index++ ) text+=parts[index];
            report(format(Tabs, text));
        }
    }

} // Package
