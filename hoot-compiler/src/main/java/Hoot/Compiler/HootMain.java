package Hoot.Compiler;

import java.io.*;
import java.util.*;
import org.apache.commons.cli.*;
import org.apache.commons.cli.Option.*;
import org.apache.commons.lang3.ArrayUtils;

import Hoot.Runtime.Faces.*;
import Hoot.Runtime.Maps.Package;
import Hoot.Runtime.Values.Variable;

import static Hoot.Compiler.Scopes.File.*;
import static Hoot.Runtime.Maps.Package.*;
import static Hoot.Runtime.Names.Primitive.*;
import static Hoot.Runtime.Functions.Utils.*;
import static Hoot.Runtime.Functions.Exceptional.*;
import static Hoot.Runtime.Maps.Library.SourceFileType;
import static Hoot.Runtime.Maps.Library.loadBasePaths;

/**
 * Compiles Hoot code to Java classes and types.
 * Takes command line arguments and thereby instructs the compiler.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2025 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class HootMain implements Logging {

    public static final String Source = "source";
    public static final String TestSource = "test-source";
    public static final String SourceTest = "src/test/hoot";
    public static final String SourcePath = "src/main/hoot";

    public static final String Target = "target";
    public static final String TargetTest = "target/generated-test-sources/hoot-maven-plugin";
    public static final String TargetPath = "target/generated-sources/hoot-maven-plugin";

    static final String VersionReport = "found JDK %s, generated code will be compatible with JDK %s";
    static { // check and report java version
        String message = String.format(VersionReport, Variable.javaVersion(), Variable.javaCompatibility());
        printLine(); printLine(message);
    }

    public static void main(String... args) { mainly().parseCommand(args).runCommand(); }
    public static HootMain mainly() { return new HootMain(); }

    final CommandLineParser parser = new DefaultParser();
    CommandLine parseArgs(String[] args) { return nullOrTryLoudly(() -> parser.parse(availableOptions(), args)); }

    CommandLine command;
    HootMain parseCommand(String[] args) { command = parseArgs(args); return this; }
    HootMain runCommand() { if (helpWanted()) printHelp(); else compilePackages(); return this; }

    boolean helpWanted() { return falseOr((c) -> c.hasOption(Help), command); }
    boolean testWanted() { return falseOr((c) -> c.hasOption(TestOnly), command); }

    boolean hasMainSource() { return falseOr((c) -> c.hasOption(Source), command); }
    boolean hasTestSource() { return falseOr((c) -> c.hasOption(TestSource), command); }
    boolean needsDefaultSource() { return !hasMainSource() && !hasTestSource(); }

    static String normalPath(String p) { return Package.normalPath(p); }
    String defaultSourcePath() { return defaultSourceFolder().getPath(); }
    File defaultSourceFolder() { return new File(normalTargetFolder(), normalSource()); }
    String normalSource() { return normalPath(sourceTail()); }
    String normalTargetFolder() { return removeTail(normalPath(targetTail()), targetFolderPath()); }
    String shorten(String path, String tail) { return path.substring(0, path.length() - tail.length()); }
    String removeTail(String tail, String path) { return path.endsWith(tail)? shorten(path, tail): path; }

    String targetTail() { return hasTestSource() ? TargetTest : TargetPath; }
    String sourceTail() { return hasTestSource() ? SourceTest : SourcePath; }
    String selectedSource() { // figure intended source from supplied args
        return hasTestSource() ? testSourcePath() :
               hasMainSource() ? sourcePath() : defaultSourcePath(); }

    String targetFolderPath() { return targetFolder.getPath(); }
    String targetPath() { return emptyOr((c) -> c.getOptionValue(Folder, targetTail()), command); }
    String sourcePath() { return emptyOr((c) -> c.getOptionValue(Source, sourceTail()), command); }
    String sourceType() { return emptyOr((c) -> c.getOptionValue(Language, SourceFileType), command); }

    String testTargetPath() { return emptyOr((c) -> c.getOptionValue(Folder, TargetTest), command); }
    String testSourcePath() { return emptyOr((c) -> c.getOptionValue(TestSource, SourceTest), command); }

    static final String WorkPath = "working";
    public static final String BasePath = "user.dir";
    String basePath() { return emptyOr((c) -> c.getOptionValue(Base, systemValue(BasePath)), command); }

    final List<String> packageList = emptyList(String.class);
    List<String> packages() { return this.packageList; }
    boolean noPackages() { return packages().isEmpty(); }
    boolean somePackages() { return !noPackages(); }

    List<String> packageList() { return wrap(command.getOptionValues(Packages)); }
    void cachePackages() {
        packages().addAll(selectList(packageList(), p -> !p.isEmpty()));
        if (noPackages()) {
            packages().addAll(collectPackagesUnder(sourceFolder));
        }
    }

    List<String> collectPackagesUnder(File hootFolder) {
        return collectList(list -> {
            File[] subFolders = hootFolder.listFiles(FolderFilter);
            wrap(subFolders).forEach(f -> {
                File packageFolder = new File(hootFolder, f.getName());
                if (packageFolder.isDirectory()) {
                    String packageName = Package.nameFrom(sourceFolder, packageFolder);
                    list.addAll(collectPackagesUnder(packageFolder));
                    list.add(packageName);
                }
            });
        });
    }

    static final String[] Skipped = { "resources", "java" };
    static final List<String> SkippedFolders = wrap(Skipped);
    static final FileFilter FolderFilter = (f) -> f.isDirectory() && !SkippedFolders.contains(f.getName());

    final List<String> libsList = emptyList(String.class);
    List<String> libs() { return this.libsList; }
    void cacheLibs() { libs().addAll(libsList()); }
    List<String> libsList() { return wrap(command.getOptionValues(Libs)); }

    public static final String Compiler = "hoot-compiler";
    void printHelp() { helpPrinter().printHelp(Compiler, availableOptions()); }
    HelpFormatter helpPrinter() {
        HelpFormatter f = new HelpFormatter();
        f.setWidth(120);
        return f;
    }

    Options availableOptions() { return collectWith(new Options(), listOptions(), (opts,opt) -> opts.addOption(opt)); }
    List<Option> listOptions() { return wrap(
        baseOption(), sourceOption(), targetOption(), packageOption(),
        testSourceOption(), testOption(), helpOption(), languageOption()
        ); }

    String basePath;
    File baseFolder;
    File sourceFolder;
    File targetFolder;

    void prepareFolders() {
        // validate the paths
        basePath = basePath();
        baseFolder = locate(WorkPath, basePath);
        if (baseFolder == null) return;

        targetFolder = locate(Target, targetPath(), targetTail());
        sourceFolder = locate(Source, selectedSource(), sourceTail());
    }

    static final String Adding = "adding";
    static final String RelativePrefix = "../";
    static final String ClassesPath = "target/classes";
    final List<File> basicPaths = emptyList(File.class);
    void addBasicPath(String relativePath) { basicPaths.add(locate(Adding, relativePath, ClassesPath)); }
    void addQualifiedPath(String relativePath, String qualifier) {
        if (basePath.endsWith(qualifier)) basicPaths.add(locate(Adding, RelativePrefix + relativePath, ClassesPath)); }

    void addPath(String relativePath, String... qualifiers) {
        if (hasAny(qualifiers)) addQualifiedPath(relativePath, qualifiers[0]); else addBasicPath(relativePath); }

    void loadPaths() {
        File[] paths = { sourceFolder, targetFolder, };
        basicPaths.addAll(wrap(paths));
        loadBasePaths(wrap(paths));
        UnitFactory = StandardUnitFactory;
        Hoot.Compiler.Scopes.File.fileType(sourceType());
    }

    static final String Comparison = "comparing: '%s' and '%s'";
    File locate(String name, String... paths) { return reportFolder(name, locateCode(name, paths)); }
    File locateCode(String folderName, String... folderPaths) {
        File folder = new File(normalPath(folderPaths[0]));
        if (WorkPath.equals(folderName) || folder.isAbsolute()) return folder;
        if (folderPaths.length == 1) return new File(baseFolder, normalPath(folderPaths[0]));
        return locateRelative(folderName, normalPath(folderPaths[0]), normalPath(folderPaths[1])) ;
    }

    // paths have already been normalized
    File locateRelative(String folderName, String relativePath, String soughtPath) {
        File folder = new File(relativePath);
        if (relativePath.endsWith(soughtPath)) return folder;
        File possible = new File(folder, soughtPath);
        if (Target.equals(folderName)) return possible;
        return possible.exists() ? possible : folder;
    }

    static final String Pad = "  ";
    File reportFolder(String folderName, File folder) {
        if (folderName.length() < Missing.length())
            folderName = Pad + folderName; // adjust name

        if (folder.exists()) { // found folder
            reportLoudly(folderName, folder); return folder; }
        else if (Target.equals(folderName.trim())) {
            folder.mkdirs(); // create a missing target
            reportLoudly(Created, folder); return folder; }
        else { // report missing folder
            reportLoudly(Missing, folder); return null; }
    }

    static final String Created = " CREATED";
    static final String Missing = " WITHOUT";
    static final String FolderFound = "%s folder = %s";
    void reportLoudly(String folderName, File folder) {
        runLoudly(() -> report(format(FolderFound, folderName, folder.getCanonicalPath()))); }


    void compilePackages() { prepareCompile(); compileOrTest(); }
    void prepareCompile() { prepareFolders(); cacheLibs(); cachePackages(); loadPaths(); }
    void compileAllPackages() { packages().forEach(p -> compilePackage(Package.named(p))); }
    void compileOrTest() {
        if (testWanted()) reportPackages(); 
        else compileAllPackages();
    }

    static final String PackageReport = "packages: %s";
    void reportPackages() { report(format(PackageReport, joinWith(Blank, packages()))); testPackages(); }
    void testPackages() { packages().forEach((p) -> { reportPackage(Package.named(p)); }); }

    static final String PackageFound = "source folder located: %s";
    static final String PackageMissing = "no " + PackageFound;
    void reportPackage(Package p) { File folder = p.sourceFolder();
        String reportFormat = folder.exists() ? PackageFound : PackageMissing;
        runLoudly(() -> report(format(reportFormat, folder.getCanonicalPath()))); }

    static final String HootFileType = ".hoot";
    static final FileFilter HootFilter = (f) -> isHoot(f);
    static boolean isHoot(File f) { return f.isFile() && f.getPath().endsWith(HootFileType); }

    static final String Translation = "translating %s";
    void compilePackage(Package p) {
        if (!p.sourceFaces().isEmpty()) {
            report(Empty);
            report(format(Translation, p.name()));
            runLoudly(() -> { // any throwable is seriously jacked! --nik
                Map<String, UnitFile> fileMap = p.parseSources();
                fileMap.values().forEach(f -> f.compile());
            });
        }
    }

    static final String Dash = "-";
    static final String Optional = Dash + Dash;
    public static final String Help = "help";
    Option helpOption() { return buildOption(Help, "optional: displays this help").build(); }

    public static final String TestOnly = "only-test";
    Option testOption() { return buildOption(TestOnly, "optional: only tests compile arguments").build(); }

    public static final String Test = "test"; // note mojo alias for only-test
    public static final String[] Optionals = { Help, Test };
    public static final Map<String, String> OptionalBools = emptyMap();
    static {
        OptionalBools.put(Help, "false");
        OptionalBools.put(Test, "false");
    }

    static final String Base = "base";
    Option baseOption() { return baseOptBuilder().optionalArg(true).hasArg().build(); }
    Builder baseOptBuilder() { return buildOption(Base, Base+Path, "optional: base folder path, assumes 'user.dir' value"); }

    Option sourceOption() { return sourceOptBuilder().optionalArg(true).hasArg().build(); }
    Builder sourceOptBuilder() { return buildOption(Source, Source+Path, "optional: Hoot sources folder path"); }

    Option testSourceOption() { return testSourceBuilder().optionalArg(true).hasArg().build(); }
    Builder testSourceBuilder() { return buildOption(TestSource, Source+Path, "optional: Hoot test sources folder path"); }

    static final String Path = "Path";
    public static final String Folder = "folder";
    Option targetOption() { return targetOptBuilder().optionalArg(false).hasArg().build(); }
    Builder targetOptBuilder() { return buildOption(Folder, Target+Path, "required: Java target folder path"); }

    static final char BLANK = ' ';
    static final String Libs = "libs";
    Option libsOption() { return libsOptBuilder().optionalArg(true).valueSeparator(BLANK).hasArgs().build(); }
    Builder libsOptBuilder() { return buildOption(Libs, Libs+Path, "library JARs"); }

    static final String LangName = "languageName";
    static final String Language = "language";
    Option languageOption() { return languageBuilder().optionalArg(true).hasArg().build(); }
    Builder languageBuilder() { return buildOption(Language, LangName, "optional: language name, .st or .hoot = default"); }

    static final String PackNames = "packageNames";
    public static final String Packages = "packages";
    Option packageOption() { return packageOptBuilder().optionalArg(true).valueSeparator(BLANK).hasArgs().build(); }
    Builder packageOptBuilder() { return buildOption(Packages, PackNames, "optional: packages to compile, none = all"); }

    public static String shortened(String optionName) { return Dash + shortOption(optionName); }
    static String shortOption(String optionName) { return optionName.substring(0, 1); }
    Builder buildOption(String optionName, String text) { return buildOption(optionName, Empty, text); }

    static final String Optioned = "Option";
    Builder buildOption(String optionName, String argName, String text) {
        return Option.builder(shortOption(optionName))
                .longOpt(optionName).required(false).desc(text)
                .argName(argName.isEmpty() ? optionName+Optioned : argName); }

    public static String[] buildCommand(String path, String... options) {
        String[] args = { shortened(Folder), path, }; return ArrayUtils.addAll(args, options); }

} // HootMain
