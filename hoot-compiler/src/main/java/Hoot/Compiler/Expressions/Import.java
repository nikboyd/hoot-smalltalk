package Hoot.Compiler.Expressions;

import java.util.*;
import Hoot.Runtime.Names.*;
import Hoot.Compiler.Scopes.*;
import Hoot.Runtime.Maps.Package;
import Hoot.Runtime.Emissions.Emission;
import static Hoot.Runtime.Names.Keyword.*;
import static Hoot.Runtime.Emissions.Emission.*;
import static Hoot.Runtime.Functions.Utils.*;

/**
 * An import statement.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class Import extends Message {

    public Import(File file, Global g, String s) { super(file); this.operands.add(g); this.selector.append(s); }
    public static Import from(File file, TypeName type) { return from(file, type.toGlobal(), Import); }
    public static Import from(File file, Global g, String s) { return new Import(file, g, s); }

    public static final Global RuntimeFaces = Global.with("Hoot", "Runtime", "Faces");
    public static Import runtimeFaces(File aFile) { return from(aFile, RuntimeFaces, ImportAll); }

    public static final Global RuntimeFuncs = Global.with("Hoot", "Runtime", "Functions");
    public static Import runtimeFuncs(File aFile) { return from(aFile, RuntimeFuncs, ImportAll); }

    public static final Global RuntimeValues = Global.with("Hoot", "Runtime", "Values");
    public static Import runtimeValues(File aFile) { return from(aFile, RuntimeValues, ImportAll); }

    public static final Global RuntimeBlocks = Global.with("Hoot", "Runtime", "Blocks");
    public static Import runtimeBlocks(File aFile) { return from(aFile, RuntimeBlocks, ImportAll); }

    public static final Global SmalltalkBlocks = Global.with("Smalltalk", "Blocks");
    public static Import smalltalkBlocks(File aFile) { return from(aFile, SmalltalkBlocks, ImportAll); }

    public static final Global SmalltalkCore = Global.with("Smalltalk", "Core");
    public static Import smalltalkCore(File aFile) { return from(aFile, SmalltalkCore, ImportAll); }

    public static final Global HootExceptions = Global.with("Hoot", "Exceptions");
    public static final Global HootBehaviors = Global.with("Hoot", "Behaviors");
    public static Import hootBehaviors(File aFile) { return from(aFile, HootBehaviors, ImportAll); }

    public static final Global HootMagnitudes = Global.with("Hoot", "Magnitudes");
    public static Import hootMagnitudes(File aFile) { return from(aFile, HootMagnitudes, ImportAll); }

    public static final Global HootCollections = Global.with("Hoot", "Collections");
    public static Import hootCollections(File aFile) { return from(aFile, HootCollections, ImportAll); }

    public Global globalName() { return (Global)this.operands.get(0); }
    public String importedName() { return TypeName.with(names()).typeName(); }
    public String importSelector() { return selector().contents(); }

    public boolean importsOne() { return Import.equals(importSelector()); }
    public boolean importsAll() { return ImportAll.equals(importSelector()); }
    public boolean importsStatics() { return ImportStatics.equals(importSelector()); }

    boolean useLowerCase = false;
    public Import withLowerCase(boolean lowerCase) {
        useLowerCase = lowerCase;
        return this;
    }

    public List<String> names() {
        if (importsAll()) return namesWith(Wild);
        if (importsStatics()) return namesWith(Wild + Wild);
        return namesWith(Empty); }

    public List<String> namesWith(String extra) {
        List<String> results = globalName().names();
        if (!extra.isEmpty()) results.add(extra);
        return this.useLowerCase ? map(results, r -> r.toLowerCase()) : results; }

    @Override public Emission emitItem() {
        return importsStatics() ? emit("ImportStatics").name(importedName()) : emit("Import").name(importedName()); }

    public void addToFaces(Map<String, TypeName> importedFaces) {
        Map<String, TypeName> types = emptyMap(TypeName.class);
        if (importsAll()) {
            types.putAll(Package.named(importedName()).faceTypes());
        }
        else if (importsOne()) {
            TypeName type = TypeName.fromName(importedName());
            types.put(type.shortName(), type);
        }

        types.values().forEach(type -> {
            importedFaces.put(type.shortName(), type);
            importedFaces.put(type.fullName(), type);
        });
    }

} // Import
