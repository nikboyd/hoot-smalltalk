package Hoot.Compiler.Mojo;

import java.io.*;
import java.util.*;
import static Hoot.Compiler.HootMain.*;

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
public class HootMojo extends AbstractMojo {

    public static final String Generate = "generate";
    @Override public void execute() throws MojoExecutionException, MojoFailureException { runChoice(); }
    void runChoice() { if (wantsTest()) reportOptions(); else runCompile(); }
    boolean wantsTest() { return True.equals(getArg(Test)); }
    static final String True = "true";

    /**
     * Locates a Maven project that contains some Hoot code to be compiled.
     */
    @Parameter(defaultValue = "${project}") MavenProject project;
    File getProjectFolder() { return project.getBasedir(); }
    String getProjectFolderTail() { return getProjectFolder().getName(); }

    File findFolder(String folderPath) { return new File(getTargetFolder(), folderPath); }
    File getTargetFolder() { return new File(System.getProperty(BasePath), getArg(Folder)); }
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
    void addPackages() { commandArgs.add(shortened(Packages)); packages.forEach(p -> commandArgs.add(p)); }

    static final String[] NoArgs = { };
    String[] buildCommandArgs() {
        if (packages.isEmpty()) packages.add(WildCard);
        addArgs(); addPackages(); return commandArgs.toArray(NoArgs); }

    static final String Dashed = "--";
    static final String ShowHelp = Dashed + Help;
    static final String Testing = "testing Hoot compiler ...";
    void report(String... s) { if (s != null && s.length > 0) System.out.println(s[0]); else System.out.println(); }
    void reportOptions() { report(Testing); runCompile(); report(Empty); main(ShowHelp); }
    void runCompile() {
        addDefaultArgs();
        addTargetFolder();
        main(buildCommandArgs());

        // check for tests
        if (findFolder(SourceTest).exists()) {
            report();
            main(buildTestCompile());
        }
    }

    List<String> commandArgs = new ArrayList();
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

} // HootMojo
