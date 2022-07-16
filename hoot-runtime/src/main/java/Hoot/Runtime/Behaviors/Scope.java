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
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public abstract class Scope extends NamedItem implements Resulting {

    public Scope(NamedItem parentItem) { super(parentItem); }
    @Override public String description() { return name(); }
    @Override public void clean() { super.clean(); locals().clean(); whisper("cleaned " + description()); }

    private static final Stack<Scope> Scopes = new Stack<>();
    private static void popSafely() { if (!Scopes.empty()) Scopes.pop(); }
    public static Scope currentFile() { return Scopes.empty() ? null : Scopes.peek(); }
    private void pushScope(Scope scope) {
        if (scope.isFile()) {
            Scopes.push(scope);
        }
        else {
            currentFile().currentScope(scope);
        }
    }

    public Scope popScope() {
        if (this.isFile()) {
            popSafely();
        }
        else {
            currentFile().currentScope(nullOr(s -> s.containerScope(), current()));
        }
        return reportScope(currentScope());
    }

    protected void currentScope(Scope scope) { }
    public Scope currentScope() { return current(); }
    public Scope makeCurrent() { pushScope(this); return this; }
    public static Scope current() { return nullOr(f -> f.currentScope(), currentFile()); }
    private Scope reportScope(Scope scope) { if (hasAny(scope)) scope.reportScope(); return scope; }
    public Scope reportScope() { report("scope now " + description()); return this; }

//    public boolean resolves(Named reference) { return false; }
//    public Scope scopeResolving(Named reference) { return containerScope().scopeResolving(reference); }
//    public Class resolveType(Named reference) { return containerScope().resolveType(reference); }
//    public String resolveTypeName(Named reference) { return containerScope().resolveTypeName(reference); }

    protected Table locals = new Table(this);
    public Table locals() { return this.locals; }
    public boolean hasLocals() { return !locals().isEmpty(); }
    public boolean hasLocal(String symbolName) { return locals().containsSymbol(symbolName); }
    public void addLocal(Variable variable) { locals().addSymbol(variable); }
    public Variable localNamed(String symbol) { return locals().symbolNamed(symbol); }

    @Override public Emission emitItem() { return emitScope(); }
    @Override public Emission emitOptimized() { return emitScope(); }
    @Override public Emission emitPrimitive() { return emitScope(); }

    public Emission emitScope() { return null; } // derived classes override this!
    public List<Emission> emitLocalVariables() { return map(locals().definedSymbols(), v -> v.emitLocal()); }

} // Scope
