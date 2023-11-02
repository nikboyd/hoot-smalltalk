package Test.Compiler.Mojo;

import java.io.*;
import java.util.*;
import org.junit.*;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.testing.*;
import org.codehaus.plexus.configuration.*;

import Hoot.Runtime.Faces.Logging;
import Hoot.Compiler.Mojo.HootMojo;
import static Hoot.Runtime.Functions.Exceptional.*;
import hoot_smalltalk.hoot_maven_plugin.HelpMojo;

/**
 * A hoot-maven-plugin test fixture.
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2020 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
//@Ignore
public class MojoTest implements Logging {

    // file paths
    static final String TestPOM = "src/test/resources/test-pom.xml";

    // configured files
    static final File PomFile = new File(TestPOM);

    @Test public void testHelp() throws Exception { new HelpMojo().execute(); }
    @Test public void testMojo() throws Exception { loadMojo().execute(); }
    HootMojo loadMojo() { return nullOrTryLoudly(() -> (HootMojo)lookupMojo()); }

    static final String Lib = "hoot-smalltalk";
    static final String Jar = "hoot-maven-plugin";
    Mojo lookupMojo() throws Exception { return rule.lookupMojo(Lib, Jar, version(), HootMojo.Generate, config()); }
    PlexusConfiguration config() throws Exception { return rule.extractPluginConfiguration(Jar, PomFile); }

    Properties p = new Properties();
    static final String Version = "version"; // loaded value name
    static final String MojoVersion = "target/classes/version.properties";
    static final File VersionFile = new File(MojoVersion);
    String version() { runLoudly(() -> loadVersion(p)); return p.getProperty(Version); }
    void loadVersion(Properties p) throws IOException { try (InputStream in = versionStream()) { p.load(in); } }
    InputStream versionStream() throws IOException { return VersionFile.toURI().toURL().openStream(); }

    @Rule public MojoRule rule = new MojoRule()
    {
        @Override protected void before() throws Throwable {}
        @Override protected void after() {}
    };

} // MojoTest
