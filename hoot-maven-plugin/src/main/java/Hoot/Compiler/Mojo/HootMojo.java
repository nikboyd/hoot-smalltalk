package Hoot.Compiler.Mojo;

import java.io.*;
import java.util.*;
import Hoot.Runtime.Faces.Logging;
import static Hoot.Runtime.Functions.Utils.*;

import org.apache.maven.plugin.*;
import org.apache.maven.plugins.annotations.*;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;

/**
 * A mojo for the Hoot compiler.
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2020 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
@Mojo(name = HootMojo.Generate, defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class HootMojo extends AbstractMojo implements Logging {

    public static final String Generate = "generate";
    @Override public void execute() throws MojoExecutionException, MojoFailureException { runChoice(); }
    void runChoice() { if (wantsTest()) reportOptions(); else runCompile(); }
    boolean wantsTest() { return True.equals(getArg(Test)); }
    public static final String Test = "test";
    static final String True = "true";

    /**
     * Locates a Maven project that contains some Hoot code to be compiled.
     */
    @Parameter(defaultValue = "${project}") MavenProject project;
    File getProjectFolder() { return project.getBasedir(); }
    String getProjectFolderTail() { return getProjectFolder().getName(); }

    File findFolder(String folderPath) { return new File(getTargetFolder(), folderPath); }

    public static final String Folder = "folder";
    public static final String BasePath = "user.dir";
    File getTargetFolder() { return new File(System.getProperty(BasePath), getArg(Folder)); }

    public static final String Source = "source";
    public static final String TargetPath = "target/generated-sources/hoot-maven-plugin";
    void addTargetFolder() { if (!hasArg(Source)) project.addCompileSourceRoot(findFolder(TargetPath).getPath()); }

    /**
     * Contains (optional) arguments sent to the Hoot compiler main() entry point.
     */
    @Parameter(name = "main-args") Map<String,String> mainArgs = new HashMap();
    void addArgs() { mainArgs.forEach((k,v) -> addOption(k,v)); }

    static final String TargetReport = " located folder = ../%s";
    void addDefaultArgs() {
        if (!hasArg(Folder)) {
            mainArgs.put(Folder, getProjectFolderTail());
            report(String.format(TargetReport, getArg(Folder)));
        }
    }

    boolean hasArg(String argName) { return mainArgs.containsKey(argName); }
    String getArg(String argName) { return mainArgs.getOrDefault(argName, getDefault(argName)); }
    String getDefault(String argName) { return OptionalBools.getOrDefault(argName, ""); }

    /**
     * Contains a list of (optional) package names sent to the Hoot compiler.
     */
    @Parameter() List<String> packages = new ArrayList();
    public static final String Packages = "packages";
    void addPackages() { commandArgs.add(shortened(Packages)); packages.forEach(p -> commandArgs.add(p)); }
    public static String shortened(String optionName) { return Dash + shortOption(optionName); }
    static String shortOption(String optionName) { return optionName.substring(0, 1); }

    String[] buildCommandArgs() {
        addArgs(); addPackages();
        return commandArgs(); }

    static final String Dash = "-";
    static final String Dashed = "--";
    public static final String Help = "help";
    static final String ShowHelp = Dashed + Help;
    static final String Testing = "testing Hoot compiler ...";
    void reportOptions() { report(Testing); runCompile(); report(Empty); runMain(ShowHelp); }

    void runCompile() {
        addDefaultArgs();
        addTargetFolder();
        runMain(buildCommandArgs());

        // check for tests
        if (findFolder(SourceTest).exists()) {
            report("");
            runMain(buildTestCompile());
        }
    }

    static final Servant mainServant = new Servant();
    void runMain(String... s) {
        report("calling compiler with: " + wrap(s).toString());
        mainServant.runCompiler(s); } //  HootMain.main(s); }

    static final String[] NoArgs = { };
    List<String> commandArgs = new ArrayList();
    String[] commandArgs() { return commandArgs.toArray(NoArgs); }

    public static final String TestOnly = "only-test";
    void addOption(String k, String v) {
        if (OptionalBools.containsKey(k)) {
            if (True.equals(v)) { // map 'test' to 'only-test'
                String key = Test.equals(k) ? TestOnly : k;
                commandArgs.add(Dashed + key);
            }
        }
        else {
            commandArgs.add(Dashed + k);
            commandArgs.add(v);
        }
    }

    public static final String TestSource = "test-source";
    public static final String SourceTest = "src/test/hoot";
    public static final String TargetTest = "target/generated-test-sources/hoot-maven-plugin";
    String[] buildTestCompile() {
        String testSource = findFolder(SourceTest).getPath();
        String testTarget = findFolder(TargetTest).getPath();
        project.addTestCompileSourceRoot(testTarget);

        packages.clear();
        commandArgs.clear();
        mainArgs.remove(Source);
        mainArgs.put(Folder, testTarget);
        mainArgs.put(TestSource, testSource);
        return buildCommandArgs();
    }

    public static final String[] Optionals = { Help, Test };
    public static final Map<String, String> OptionalBools = new HashMap();
    static {
        OptionalBools.put(Help, "false");
        OptionalBools.put(Test, "false");
    }

} // HootMojo
