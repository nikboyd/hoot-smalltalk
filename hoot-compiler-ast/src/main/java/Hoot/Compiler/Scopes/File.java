package Hoot.Compiler.Scopes;

import java.util.*;
import Hoot.Runtime.Faces.*;
import Hoot.Runtime.Names.*;
import Hoot.Runtime.Emissions.*;
import Hoot.Runtime.Behaviors.*;
import Hoot.Runtime.Maps.Package;
import static Hoot.Runtime.Functions.Utils.*;
import static Hoot.Runtime.Maps.Library.*;
import static Hoot.Runtime.Emissions.Emission.*;

import Hoot.Runtime.Maps.Library;
import Hoot.Compiler.Notes.Comment;
import Hoot.Compiler.Expressions.Import;
import static Hoot.Compiler.Expressions.Import.*;
import static Hoot.Runtime.Behaviors.HootRegistry.*;
import static Hoot.Runtime.Names.Keyword.Smalltalk;
import static Hoot.Runtime.Names.TypeName.EmptyType;
import static org.apache.commons.lang3.StringUtils.*;

/**
 * A class (or type) file, including the package name, imports, and a face definition.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2025 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class File extends Scope implements UnitFile, TypeName.Resolver, ScopeSource {

    public static final UnitFile.Factory StandardUnitFactory = (f, p) -> makeUnit(f, p);
    static { Package.UnitFactory = StandardUnitFactory; }

    // make a new file and its face current
    static UnitFile makeUnit(String faceName, String pkgName) {
        File file = new File(pkgName, faceName).makeCurrent();
//        file.faceScope().makeCurrent();
        return file; }

    public File() { super(null); }
    public File(String packageName, String faceName) {
        this(); facePackage = Package.named(packageName);
        fullName = TypeName.fromName(Name.formType(packageName, faceName)); }

    protected Scope currentScope = this; // manage scopes within each file here
    @Override protected void currentScope(Scope aScope) { this.currentScope = aScope; }
    @Override public Scope currentScope() { return this.currentScope; }

    // each file needs to track its focus: the current active scopes, both facial and member
    // facial scope varies between main and meta faces while parsing switched with protocol selector
    // each facial scope tracks its member focus scope which varies between methods and variables
    // the parse cursor focus wanders through the code of each file parsed

    public static File currentFile() { return from(Scope.currentFile()); }
    public static File from(Item item) { return nullOr(f -> (File)f, Scope.currentFile()); }
    @Override public File makeCurrent() { return (File)Scope.makeCurrentFile(this); }
    @Override public Scope popScope() { Scope.popFileScope(); return currentFile(); }

    protected Map<String, UnitFile> peerFaces = emptyMap(UnitFile.class);
    protected Map<String, UnitFile> peers() { return this.peerFaces; }
    @Override public void clean() { super.clean(); importAllFaces(); faceScope.clean(); }
    @Override public void parse() {  tokenCompiler().parseTokens(); facePackage.addFace(faceScope()); }
    @Override public boolean compile() { return tokenCompiler().compile(); }
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

        if (!SmalltalkMags.typeName().equals(packageName())) {
            importFace(Import.smalltalkMags(this));
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

    @Override @SuppressWarnings("unchecked")
    public void acceptComments(List comments) { cacheComments((List<Comment>)comments); }

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
    @Override public Scope activeFacia() { return activeFace(); }
    public Face activeFace() { return faceScope().mainScope()? faceScope(): faceScope().metaFace(); }

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

    TokenCompiler tokenCompiler;
    TokenCompiler tokenCompiler() {
        if (hasSome(tokenCompiler)) return tokenCompiler;
        tokenCompiler = new TokenCompiler(this); return tokenCompiler; }

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

    String fileType = languageType();
    @Override public String fileType() { return this.fileType; }

    public static String fileType(String type) {
        Library.languageType(defaultIfEmpty(type, SourceFileType));
        Logging.logger(File.class).info("processing sources " + languageType()); return type; }

    public String sourceFilename() { return initialName() + fileType(); }
    @Override public String targetFilename() { return initialName() + TargetFileType; }

    @Override public java.io.File sourceFile() {
        java.io.File packageFolder = facePackage.sourceFolder();
        if (packageFolder == null) return null;
        return new java.io.File(packageFolder, sourceFilename());
    }

    public java.io.File makeTargetFolder() { return facePackage().createTarget(); }
    public java.io.File targetFolder() { return facePackage.targetFolder(); }
    @Override public java.io.File targetFile() {
        java.io.File packageFolder = targetFolder();
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
    public Emission emitLibraryScope() { return emitLibraryScope(notice(), emitPackage(), emitImports()); }

    void reportWriting() { report("writing " + targetFilename()); }
    @Override public Emission emitScope() { return faceScope().emitScope(emitLibraryScope()); }
    @Override public void writeCode() {
        makeCurrent();
        clean();
        if (hasSome(makeTargetFolder())) { // any failure already reported
            reportWriting();
            tokenCompiler().fileParser().writeCode(emitScope());
        }
        popScope();
    }

} // File
