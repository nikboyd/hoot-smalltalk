package Hoot.Compiler.Mojo;


import java.io.*;
import java.util.*;
import org.eclipse.aether.*;
import org.eclipse.aether.artifact.*;
import org.eclipse.aether.repository.*;
import org.eclipse.aether.resolution.*;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.apache.maven.repository.internal.*;

import Hoot.Runtime.Faces.Logging;
import static Hoot.Runtime.Functions.Utils.*;
import static Hoot.Runtime.Functions.Exceptional.*;

/**
 * Discovers locally available library JARs cached by Maven.
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2020 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class Discovery implements Logging {

    List<RemoteRepository> remotes = emptyList(RemoteRepository.class);
    List<RemoteRepository> remotes() { return this.remotes; }
    Artifact buildArtifact(String partName) { return new DefaultArtifact(partName); }
    ArtifactRequest buildRequest(Artifact art) { return new ArtifactRequest(art, remotes(), null); }
    ArtifactRequest requestArtifact(String partName) { return buildRequest(buildArtifact(partName)); }


    static final String UserHome = "user.home";
    public static String userHome() { return System.getProperty(UserHome); }

    static final String MavenRepo = "maven.repo.local";
    static final String LocalFolder = ".m2/repository";
    public static File localMavenFolder() {
        String localFolder = System.getProperty(MavenRepo, Empty);
        return localFolder.isEmpty() ? new File(userHome(), LocalFolder) :  new File(localFolder); }

    static RepositorySystem Repository = buildRepository();
    static RepositorySystem repository() { return Repository; }

    static RepositorySystem buildRepository() {
        DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
        locator.addService(TransporterFactory.class, FileTransporterFactory.class);
        locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
        return locator.getService(RepositorySystem.class); }

    DefaultRepositorySystemSession session = buildSession();
    DefaultRepositorySystemSession session() { return this.session; }
    static DefaultRepositorySystemSession buildSession() { return MavenRepositorySystemUtils.newSession(); }

    static final Discovery CachedDiscovery = new Discovery();
    public static Discovery cachedDiscovery() { return CachedDiscovery; }

    public static final String HootSmalltalk = "hoot-smalltalk";
    static { cachedDiscovery().cacheLocalRepository(); }

    void cacheLocalRepository() {
        File localFolder = localMavenFolder();
        reportWhetherFound(LocalFolder, localFolder);
        LocalRepository result = new LocalRepository(localFolder);
        session().setLocalRepositoryManager(repository().newLocalRepositoryManager(session(), result));
    }

    public static ArtifactResult lookup(String partName) {
        return cachedDiscovery().lookupArtifact(cachedDiscovery().requestArtifact(partName)); }

    ArtifactResult lookupArtifact(ArtifactRequest r) {
        return nullOrTryQuietly(() -> { return repository().resolveArtifact(session(), r); }); }

    static final String FoundLocally = "found '%s' locally: %s";
    static final String NotFound = "not " + FoundLocally;
    public boolean found(File localFolder) { return hasSome(localFolder) && localFolder.exists(); }
    void reportWhetherFound(String folderName, File localFolder) {
        if (found(localFolder)) // expected
            whisper(format(FoundLocally, folderName, localFolder.getAbsolutePath()));
        else // not found, unexpected!
            warn(format(NotFound, folderName, "???"));
    }

} // Discovery
