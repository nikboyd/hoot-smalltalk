package Hoot.Runtime.Faces;

import java.util.*;

/**
 * A compilation unit.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2019 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public interface UnitFile extends Named, LanguageParser.Factory {

    /**
     * A compilation unit factory.
     */
    public static interface Factory {

        public UnitFile createUnit(String faceName, String packageName);

    } // Factory

    void parse();
    boolean compile();

    Named makeCurrent();
    Named popScope();

    default void clean() {}
    default void writeCode() {}
    default void acceptComments(List comments) {}
    void peers(Map<String, UnitFile> peers);
    void addStandardImports();
    Named faceScope();

    java.io.File sourceFile();
    java.io.File targetFile();
    String targetFilename();

    @Override default FileParser createParser() { return null; }

} // UnitFile
