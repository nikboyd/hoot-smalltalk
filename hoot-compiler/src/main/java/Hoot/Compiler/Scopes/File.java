package Hoot.Compiler.Scopes;

import java.util.*;
import org.antlr.v4.runtime.*;

import Hoot.Runtime.Faces.*;
import Hoot.Runtime.Names.*;
import Hoot.Runtime.Emissions.*;
import Hoot.Runtime.Behaviors.*;
import Hoot.Runtime.Maps.Package;
import static Hoot.Runtime.Functions.Utils.*;
import static Hoot.Runtime.Maps.Library.*;
import static Hoot.Runtime.Emissions.Emission.*;
import static Hoot.Runtime.Maps.Package.*;

import Hoot.Compiler.Notes.Comment;
import Hoot.Compiler.Expressions.Import;
import static Hoot.Compiler.Expressions.Import.*;
import static Hoot.Runtime.Behaviors.HootRegistry.*;
import static Hoot.Runtime.Names.Keyword.Smalltalk;
import static Hoot.Runtime.Names.TypeName.EmptyType;

/**
 * A class (or type) file, including the package name, imports, and a face definition.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class File extends Scope implements UnitFile, TypeName.Resolver, ScopeSource {

    public File() { super(null); }
    public File(String packageName, String faceName) {
        this(); facePackage = Package.named(packageName);
        fullName = TypeName.fromName(Name.formType(packageName, faceName)); }

    public static final UnitFile.Factory
    StandardUnitFactory = (faceName, packageName) -> new File(packageName, faceName);
    static { UnitFactory = StandardUnitFactory; }

    protected Scope currentScope = this; // manage scopes with each file here
    @Override protected void currentScope(Scope aScope) { this.currentScope = aScope; }
    @Override public Scope currentScope() { return this.currentScope; }

    public static File currentFile() { return from(Scope.currentFile()); }
    public static File from(Item item) { return nullOr(f -> (File)f, item.fileScope()); }
    @Override public Scope makeCurrent() { return super.makeCurrent(); }

    protected Map<String, UnitFile> peerFaces = emptyMap(UnitFile.class);
    protected Map<String, UnitFile> peers() { return this.peerFaces; }
    @Override public void clean() { super.clean(); importAllFaces(); faceScope.clean(); }
    @Override public void parse() { tokenCompiler.parseTokens(); facePackage.addFace(faceScope()); }
    @Override public boolean compile() { return tokenCompiler.compile(); }
    @Override public void peers(Map<String, UnitFile> peers) {
        if (hasKeys(peers)) {
            peers().putAll(peers);
            peers.values().forEach(f -> {
                peers().put(f.fullName(), f);
            });
        }
    }

    @Override public void addStandardImports() {
        addRuntimeLibraryImports();
        addSmalltalkLibraryImports();
        if (!packageName().startsWith(Smalltalk)) {
            addHootLibraryImports();
        }
    }

    public void addRuntimeLibraryImports() {
        importFace(Import.runtimeFuncs(this));
        importFace(Import.runtimeFaces(this));
        importFace(Import.runtimeValues(this));
        importFace(Import.runtimeBlocks(this));
    }

    public void addSmalltalkLibraryImports() {
        if (!SmalltalkCore.typeName().equals(packageName())) {
            importFace(Import.smalltalkCore(this));
        }

        if (!SmalltalkBlocks.typeName().equals(packageName())) {
            importFace(Import.smalltalkBlocks(this));
        }
    }

    public void addHootLibraryImports() {
        if (!HootBehaviors.typeName().equals(packageName())) {
            importFace(Import.hootBehaviors(this));
            importFace(Import.from(this, NilType()));
            importFace(Import.from(this, RootType()));
            importFace(Import.from(this, TrueType()));
            importFace(Import.from(this, FalseType()));
            importFace(Import.from(this, BooleanType()));
        }

        if (!HootMagnitudes.typeName().equals(packageName())) {
            importFace(Import.hootMagnitudes(this));
            importFace(Import.from(this, IntegerType()));
            importFace(Import.from(this, CharacterType()));
            importFace(Import.from(this, FloatType()));
            importFace(Import.from(this, DoubleType()));
        }

        if (!HootCollections.typeName().equals(packageName())) {
            importFace(Import.hootCollections(this));
            importFace(Import.from(this, StringType()));
        }
    }

    protected List<Comment> cachedComments = emptyList(Comment.class);
    public void cacheComments(List<Comment> comments) { cachedComments.addAll(comments); }
    public Comment getComment(int index) {
        return (index >= 0 && index < cachedComments.size()) ? cachedComments.get(index) : null; }

    protected List<Import> faceImports = emptyList(Import.class);
    public void importFace(Import faceImport) { faceImports.add(faceImport); }
    public List<Import> faceImports() { return faceImports; }

    public TypeName importedTypeNamed(Named reference) {
        return (importedFaces().isEmpty()) ? null : importedFaces().get(reference.name().toString()); }

    protected Map<String, TypeName> importedFaces = emptyMap(TypeName.class);
    public Map<String, TypeName> importedFaces() { importAllFaces(); return this.importedFaces; }
    private void importAllFaces() {
        if (faceImports().isEmpty() || !importedFaces.isEmpty()) return;
        faceImports().forEach((faceImport) -> { faceImport.addToFaces(importedFaces); });
    }

    TypeName fullName = EmptyType;
    public void fullName(String fullName) { this.fullName = TypeName.fromName(fullName); }
    public String initialName() { return this.fullName.shortName(); }
    @Override public String name() { return initialName(); }
    @Override public String fullName() { return facePackage().qualify(initialName()); }
    @Override public String description() { return "File " + initialName(); }

    Face faceScope = new Face(this);
    @Override public Face faceScope() { return faceScope; }
    @Override public Scope facialScope() { return this.faceScope(); }
    public String faceName() { return faceScope.name(); }

    public Typified faceNamed(String faceName) {
        String baseFace = Name.withoutMeta(faceName);
        String searchName = Name.asMetaMember(faceName);
        boolean metaNamed = Name.isMetaNamed(faceName);

        if (importedFaces().containsKey(searchName)) {
            Typified result = importedFaces().get(searchName).findType();
            reportFace(result, searchName, "imports");
            return result;
        }

        if (peers().containsKey(baseFace)) {
            Typified result = facePackage().faceNamed(baseFace);
            if (Face.class.isInstance(result)) {
                Face found = (Face)result;
                if (!found.isSigned()) found.file().parse();
            }
            if (metaNamed) result = nullOr(r -> r.$class(), result);
            reportFace(result, searchName, "peers");
            return result;
        }

        Typified result = findFace(searchName);
        boolean known = reportWhetherKnown("library", result, searchName);
        if (!known) reportFace(result, searchName, "library");
        return result;
    }

    static final String FaceReport = "found %s from %s with '%s'";
    void reportFace(Typified face, String name, String source) {
        if (hasSome(face)) whisper(format(FaceReport, face.fullName(), source, name));
        else if ("library".equals(source)) {
            whisper(format(FaceReport, "null", source, name));
        }
    }

    TokenCompiler tokenCompiler = new TokenCompiler(this);
    @Override public TokenStream tokenStream() { return tokenCompiler.tokenStream(); }
    @Override public boolean isFile() { return true; }
    @Override public int nestLevel() { return 0; }
    public String notice() { return this.faceScope.notes().notice(); }

    protected Package facePackage = null;
    public Package facePackage() { return facePackage; }
    public String packageName() { return facePackage.name(); }
    public String packagePathname() { return facePackage.pathname(); }
    public void namePackage(String packageName) { facePackage = Package.named(packageName); }

    public boolean needsMagnitudes() { return facePackage.definesBehaviors(); }
    public boolean needsCollections() { return facePackage.definesBehaviors() || facePackage.definesMagnitudes(); }

    public String sourceFilename() { return initialName() + SourceFileType; }
    public String targetFilename() { return initialName() + TargetFileType; }

    public java.io.File sourceFile() {
        java.io.File packageFolder = facePackage.sourceFolder();
        if (packageFolder == null) return null;
        return new java.io.File(packageFolder, sourceFilename());
    }

    public java.io.File targetFile() {
        java.io.File packageFolder = facePackage.targetFolder();
        if (packageFolder == null) return null;
        return new java.io.File(packageFolder, targetFilename());
    }

//    @Override public boolean resolves(Named reference) { return faceLibrary.resolves(reference); }
//    @Override public Scope scopeResolving(Named reference) { return resolves(reference) ? this : null; }
//    @Override public Class resolveType(Named reference) { return faceLibrary.resolveType(reference); }

//    @Override public String resolveTypeName(Named reference) {
//        String name = reference.name().toString();
//        if (!importedFaces().isEmpty()) {
//            if (importedFaces().containsKey(name)) {
//                return importedFaces().get(name).fullName();
//            }
//        }
//
//        Typified face = facePackage.faceNamed(name);
//        if (face != null) return face.fullName();
//
//        return faceLibrary.resolveTypeName(reference);
//    }

    @Override public TypeName resolveTypeNamed(Named reference) {
        String name = reference.name().toString();
        TypeName result = importedTypeNamed(reference);
        if (result != null) return result;

        Typified face = facePackage.faceNamed(name);
        if (face != null) return TypeName.fromOther(face);

        return CurrentLib.resolveTypeNamed(reference);
    }

    public Emission emitPackage() { return emitStatement(emit("Package").name(packageName())); }
    public List<Emission> emitImports() { return map(faceImports, face -> face.emitItem()); }
    @Override public Emission emitScope() { return faceScope().emitScope(emitLibraryScope()); }
    public Emission emitLibraryScope() { return emitLibraryScope(notice(), emitPackage(), emitImports()); }

} // File
