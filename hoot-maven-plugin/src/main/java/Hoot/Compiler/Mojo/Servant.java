package Hoot.Compiler.Mojo;

import java.io.*;
import java.util.Properties;
import Hoot.Runtime.Faces.Logging;
import org.eclipse.aether.resolution.ArtifactResult;
import static Hoot.Runtime.Functions.Exceptional.*;

/**
 * Starts and stops the web service as needed.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class Servant implements Logging {

    private Process remoteProcess = null;
    private InputStream processStream() { return remoteProcess.getInputStream(); }
    private BufferedReader processReader() { return new BufferedReader(new InputStreamReader(processStream())); }

    private void pipeShellOutput() {
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
    private String locateCompiler() {
        ArtifactResult result = Discovery.lookup(CompilerSpec + version());
        return result.getArtifact().getFile().getAbsolutePath();
    }

    static final String Shell = "/bin/sh";
    static final String ServiceCommand = "java -jar %s";
    public void runCompiler(String... args) {
        runLoudly(() -> {
            String launchCommand = format(ServiceCommand, locateCompiler());
            for (String s : args) { launchCommand += " " + s; }
            ProcessBuilder pb = new ProcessBuilder(Shell, "-c", launchCommand);
            remoteProcess = pb.start();
            new Thread(() -> pipeShellOutput()).start();
            remoteProcess.waitFor();
        });
    }

} // Servant
