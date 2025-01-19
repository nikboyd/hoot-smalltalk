package Hoot.Runtime.Behaviors;

import java.util.*;
import Hoot.Runtime.Values.*;
import Hoot.Runtime.Emissions.*;
import Hoot.Runtime.Faces.Resulting;
import static Hoot.Runtime.Functions.Utils.*;

/**
 * A language scope, and management thereof.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2025 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public abstract class Scope extends NamedItem implements Resulting {
    
    static final Deque<Scope> FileScopes = new ArrayDeque<>();
    static void pushFileScope(Scope s) { FileScopes.push(s); }
    public static void popFileScope() { popSafely(FileScopes); }
    public static Scope currentFile() { return peekSafely(FileScopes); }
    public static Scope makeCurrentFile(Scope s) { return makeCurrent(s, FileScopes); }

    static final Deque<Scope> BlockScopes = new ArrayDeque<>();
    static void pushBlockScope(Scope s) { BlockScopes.push(s); }
    public static void popBlockScope() { popSafely(BlockScopes); }
    public static Scope currentBlock() { return peekSafely(BlockScopes); }
    public static Scope makeCurrentBlock(Scope s) { return makeCurrent(s, BlockScopes); }
    
    public static List<Scope> recentBlockScopes() {
        List<Scope> bs = new ArrayList<>(); // scan available block scopes
        BlockScopes.iterator().forEachRemaining((b) -> { 
            if (bs.isEmpty()) bs.add(0, b); else if (!bs.get(0).isMethod()) bs.add(0, b); });
        return reverseList(bs); }

    public static Scope findCurrentMethod() {
        List<Scope> bs = new ArrayList<>(); // scan available block scopes
        BlockScopes.iterator().forEachRemaining((b) -> { if (b.isMethod() && bs.isEmpty()) bs.add(b); });
        return bs.isEmpty()? null: bs.get(0); }

    static void popSafely(Deque<?> s) { if (!s.isEmpty()) s.pop(); }
    static <S extends Scope> S peekSafely(Deque<S> s) { return s.isEmpty()? null: (S)s.peek(); }
    static <S extends Scope>boolean currently(S scope, Deque<S> s) { return peekSafely(s) == scope; }
    static <S extends Scope> S makeCurrent(S scope, Deque<S> s) { if (!currently(scope, s)) s.push(scope); return scope; }

    // make scope stack for files, faces, methods = blocks
    private void pushScope(Scope scope) {
        if (scope.isFile()) pushFileScope(scope);
        if (scope.isBlock() || scope.isMethod()) pushBlockScope(scope);
//        reportScope(scope);
    }

    public Scope popScope() {
        return reportScope(currentScope());
    }

    public Scope(NamedItem parentItem) { super(parentItem); }
    @Override public String description() { return name(); }
    @Override public void clean() { super.clean(); locals().clean(); 
        whisper("cleaned " + description()); }

    protected void currentScope(Scope scope) { }
    public Scope currentScope() { return current(); }
    public Scope makeCurrent() { pushScope(this); return this; }
    public static Scope current() { return nullOr(f -> f.currentScope(), currentFile()); }
    private Scope reportScope(Scope scope) { if (hasAny(scope)) scope.reportScope(); return scope; }
    public Scope reportScope() {
//        report("scope now " + description());
        return this; }

//    public boolean resolves(Named reference) { return false; }
//    public Scope scopeResolving(Named reference) { return containerScope().scopeResolving(reference); }
//    public Class resolveType(Named reference) { return containerScope().resolveType(reference); }
//    public String resolveTypeName(Named reference) { return containerScope().resolveTypeName(reference); }

    protected Table locals = new Table(this);
    public Table locals() { return this.locals; }
    public void addLocal(Variable variable) { locals().addSymbol(variable); }
    public boolean hasLocals() { return !locals().isEmpty(); }
    public boolean hasLocal(String symbolName) { return locals().containsSymbol(symbolName); }
    public Variable localNamed(String symbol) { return locals().symbolNamed(symbol); }

    @Override public Emission emitItem() { return emitScope(); }
    @Override public Emission emitOptimized() { return emitScope(); }
    @Override public Emission emitPrimitive() { return emitScope(); }

    public Emission emitScope() { return null; } // derived classes override this!
    public List<Emission> emitLocalVariables() { return map(locals().definedSymbols(), v -> v.emitLocal()); }

} // Scope
