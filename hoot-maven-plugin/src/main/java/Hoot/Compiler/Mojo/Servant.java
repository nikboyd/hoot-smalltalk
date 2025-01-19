package Hoot.Compiler.Mojo;

import java.io.*;
import java.util.Properties;
import org.apache.commons.lang3.SystemUtils;
import org.eclipse.aether.artifact.Artifact;
import static Hoot.Runtime.Functions.Exceptional.*;
import Hoot.Runtime.Faces.Logging;

/**
 * Compiles Hoot code in a separate process.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2025 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class Servant implements Logging {

    private Process remoteProcess = null;
    public void runCompiler(String... args) {
        runLoudly(() -> {
            remoteProcess = buildProcess(args).start();
            new Thread(() -> pipeShellOutput()).start();
            remoteProcess.waitFor();
        });
    }

    InputStream processStream() { return remoteProcess.getInputStream(); }
    BufferedReader processReader() { return new BufferedReader(new InputStreamReader(processStream())); }
    void pipeShellOutput() {
        String line; BufferedReader reader = processReader();
        try { while((line = reader.readLine()) != null) report(line); }
        catch (IOException x) { error(x); }
    }

    Properties p = new Properties();
    static final String Version = "version"; // loaded value name
    static final String MojoVersion = "/version.properties";
    String version() { runLoudly(() -> loadVersion(p)); return p.getProperty(Version); }
    void loadVersion(Properties p) throws IOException { try (InputStream in = versionStream()) { p.load(in); } }
    InputStream versionStream() throws IOException { return getClass().getResourceAsStream(MojoVersion); }

    static final String CompilerSpec = "hoot-smalltalk:hoot-compiler-boot:";
    Artifact locateArtifact() { return Discovery.lookup(CompilerSpec + version()).getArtifact(); }
    String locateCompiler() { return locateArtifact().getFile().getAbsolutePath(); }

    static final String Quote = "\"";
    static final String WinShell = "cmd.exe";
    static final String WinCommand = "'%%JAVA_HOME%%\\bin\\java' -jar %s";
    String buildWinCommand(String... args) {
        String result = format(WinCommand, locateCompiler()).replace("'", Quote);
        for (String s : args) { result += " " + s; }
        report("running "+result);
        return result; }

    static final String Shell = "/bin/sh";
    static final String JavaCommand = "$JAVA_HOME/bin/java -jar %s";
    String buildJavaCommand(String... args) {
        String result = format(JavaCommand, locateCompiler());
        for (String s : args) { result += " " + s; }
        report("running "+result);
        return result; }

    ProcessBuilder buildProcess(String... args) {
        return SystemUtils.IS_OS_WINDOWS ?
            new ProcessBuilder(WinShell, "/c", buildWinCommand(args)) :
            new ProcessBuilder(Shell, "-c", buildJavaCommand(args)); }

} // Servant
