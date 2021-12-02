package Hoot.Runtime.Faces;

import java.util.*;

/**
 * A compilation unit.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2019 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public interface UnitFile extends Named {

    /**
     * A compilation unit factory.
     */
    public static interface Factory {

        public UnitFile createUnit(String faceName, String packageName);

    } // Factory

    public void parse();
    public boolean compile();
    public void peers(Map<String, UnitFile> peers);
    public void addStandardImports();
    public Named faceScope();

} // UnitFile
