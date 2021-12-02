package Hoot.Runtime.Maps;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import static Hoot.Runtime.Functions.Utils.*;

/**
 * Maps the classes located in an element of a class path.
 * Locates all package directories relative to an archive from the class path.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 1999,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class ZipMap extends PathMap {

    public ZipMap(String pathname) { super(pathname); }

    /**
     * @return whether the supplied pathname identifies a support file type.
     * @param pathname the pathname of a potential archive file.
     */
    public static boolean supports(String pathname) {
        return matchAny(FileTypes, type -> pathname.endsWith(type)); }

    public static final String[] SupportedFileTypes = { ".zip", ".ZIP", ".jar", ".JAR" };
    public static final List<String> FileTypes = wrap(SupportedFileTypes);

    /**
     * @return null - file operations are not supported in archives.
     * @param folderName the name of a Java/Hoot package directory.
     */
    @Override public File locate(String folderName) { return (null); }

    @Override public void load() {
        try (ZipFile zipFile = new ZipFile(basePath)) {
            Enumeration e = zipFile.entries();
            while (e.hasMoreElements()) {
                load((ZipEntry) e.nextElement());
            }
        }
        catch (IOException ex) {
            error(ex.getMessage(), ex);
        }
        finally {
            reportAdded();
        }
    }

    protected void load(ZipEntry entry) {
        String fullName = entry.getName();
        File f = new File(fullName);
        String folderName = f.getParent();
        String fileName = f.getName();
        if (namesClass(fileName)) {
            addMapped(folderName, classNameWithoutType(fileName));
        }
    }

} // ZipMap
