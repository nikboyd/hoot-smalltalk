package Hoot.Runtime.Emissions;

import java.util.*;
import java.util.function.Predicate;

import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.Tree;

import Hoot.Runtime.Faces.Named;
import Hoot.Runtime.Notes.Note;
import Hoot.Runtime.Notes.NoteList;
import Hoot.Runtime.Values.Operand;
import Hoot.Runtime.Behaviors.Scope;
import Hoot.Runtime.Names.TypeName;
import Hoot.Runtime.Names.TypeName.Resolver;
import static Hoot.Runtime.Functions.Utils.*;

/**
 * An item with its associated parse context and parent.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public abstract class Item implements EmissionSource, Resolver {

    protected Item() { super(); }
    protected Item(Item container) { this(); inside(container); }

    @SuppressWarnings("unchecked")
    protected <T extends Item> Class<T> itemType() { return (Class<T>)getClass(); }
    public final <T extends Item> T inside(Item container) { container(container); return asType(itemType()); }

    public void clean() { } // derived classes sometimes override this
    public void release() { // derived classes sometimes override this
        if (hasOne(container())) {
            // breaks cycles, if needed, to promote proper garbage collection
            container().release();
            this.container = null;
        }
    }

    protected Item container;
    public Item container() { return this.container; }
    public void container(Item container) { this.container = container; scope(container); }
    protected void contain(Item item) { if (hasOne(item)) item.container(this); }
    protected <T extends Item> void containAll(List<T> items) {
        if (hasAny(items)) items.forEach(item -> this.contain(item)); }

    protected Scope itemScope;
    public Scope scope() { return this.itemScope; }
    protected void scope(Scope s) { this.itemScope = s; }
    protected void scope(Item it) { if (hasOne(it)) scope(hasInstance(it) ? (Scope)it : it.containerScope()); }

    public Scope containerScope() { return findParent(Scope.class); }
    public static boolean hasInstance(Item item) { return Scope.class.isInstance(item); }

    protected NoteList notes = new NoteList();
    public NoteList notes() { return this.notes; }
    public void note(Note note) { this.notes.note(note); }
    public void noteAll(List<Note> notes) { notes().noteAll(notes); }

    @Override public TypeName resolveTypeNamed(Named reference) {
        return nullOr(s -> s.resolveTypeNamed(reference), containerScope()); }

    public boolean isVoidedContext() { return false; }
    public boolean hasVoidedContext() {
        Scope m = methodScope();
        Operand v = variableContainer();
        if (hasOne(m)) return m.isVoidedContext();
        if (hasOne(v)) return v.isVoidedContext();
        return false;
    }

    public boolean hasPrimitiveContext() {
        Scope m = methodScope();
        Operand v = variableContainer();
        if (hasOne(m)) return m.isPrimitive();
        if (hasOne(v)) return v.isPrimitive();
        return false;
    }

    protected Scope findScope(Predicate<Item> p) { return findItem(Scope.class, p); }
    public Operand variableContainer() { return findItem(Operand.class, s -> s.isVariable()); }

    public List<Scope> blockScopes() { return Scope.recentBlockScopes(); }
    public Scope blockScope() { return Scope.currentBlock(); }
    public Scope methodScope() { return findScope(s -> s.isMethod()); }
    public Scope facialScope() { return fileScope().facialScope(); }
    public Scope activeFacia() { return fileScope().activeFacia(); }
    public Scope fileScope() { return Scope.currentFile(); }

    @Override public int nestLevel() {
        int level = (hasNone(container())) ? 0 : container().nestLevel();
        whisper("level " + level); return level; }

    public <T extends Item> T parentItem(Class<T> itemType) { return itemType.cast(container()); }
    public <T extends Item> T asType(Class<T> itemType) { return itemType.cast(this); }
    public <R extends Item> R itemNotes() { return null; } // override this!
    public List<String> knownTypes() { return emptyList(String.class); } // override this

    public boolean isEmpty() { return false; }
    public boolean missingAny(Item... items) { for (Item item : items) if (hasNone(item)) return true; return false; }

    public boolean isStatic() { return notes().isStatic(); }
    public boolean isAbstract() { return notes().isAbstract(); }
    public boolean isStacked() { return notes().isStacked(); }
    public boolean isPrimitive() { return notes().isPrimitive(); }

    public boolean isFramed() { return false; }
    public boolean isVariable() { return false; }
    public boolean isBlock() { return false; }
    public boolean isMethod() { return false; }
    public boolean isFacial() { return false; }
    public boolean isFile() { return false; }

    protected boolean matchesExactly(Class<?> itemType) { return hasOne(itemType) && itemType.equals(getClass()); }
    protected boolean matches(Class<?> itemType) { return hasOne(itemType) && itemType.isAssignableFrom(getClass()); }

    /**
     * @return an item of a specific kind, within limits, or null
     * @param <T> an item type
     * @param itemType an item type
     * @param limitTypes limit types that truncate search
     */
    public <T extends Item> T findItem(Class<T> itemType, Class<?>... limitTypes) {
        if (hasNone(itemType)) return null;
        return this.matches(itemType) ? asType(itemType) : this.findParent(itemType, limitTypes); }

    protected <R extends Item> R findItem(Class<R> itemType, Predicate<Item> p) {
        Item item = this; while (hasOne(item) && !p.test(item)) item = item.container();
        return itemType.cast(item); }

    /**
     * @return whether a parent of the given kind exists
     * @param <T> a parent type
     * @param itemType a parent type
     * @param limitTypes limit types
     */
    public <T extends Item> boolean hasParent(Class<T> itemType, Class<?>... limitTypes) {
        return findParent(itemType, limitTypes) != null; }

    /**
     * @return a parent item of a specific kind, within limits, or null
     * @param <T> a parent item type
     * @param itemType a parent item type
     * @param limitTypes limit types that truncate search
     */
    public <T extends Item> T findParent(Class<T> itemType, Class<?>... limitTypes) {
        if (hasNone(itemType)) return null;
        if (hasNone(container())) return null;

        if (limitTypes.length > 0) {
            for (Class<?> testType : limitTypes) {
                if (container().matches(testType)) {
                    return null; // limit search scope
                }
            }
        }

        return container().findItem(itemType, limitTypes);
    }

    public <T extends Item> T findParentExactly(Class<T> itemType, Class<?>... limitTypes) {
        if (hasNone(itemType)) return null;
        if (hasNone(container())) return null;

        if (limitTypes.length > 0) {
            for (Class<?> testType : limitTypes) {
                if (container().matchesExactly(testType)) {
                    return null; // limit search scope
                }
            }
        }

        return container().findExactly(itemType, limitTypes);
    }

    public <T extends Item> T findExactly(Class<T> itemType, Class<?>... limitTypes) {
        return nullOr(it -> matchesExactly(it) ? asType(it) : findParentExactly(it, limitTypes), itemType); }

    protected int sourceLine = 0;
    public int sourceLine() { return sourceLine; }
    public void sourceLine(int lineNumber) { sourceLine = lineNumber + 1; }
    public <T extends Item> T withLine(int line) { sourceLine(line); return asType(itemType()); }

    public String description() { return getClass().getName(); }
    public String commentFrom(Tree node) { return nullOr(hidden -> hidden.getText(), hiddenToken((Tree) node)); }
    public TokenStream tokenStream() { return null; } // dervied classes override!!
    public Token tokenFrom(Tree node) { return (Token)node.getPayload(); }
    public Token hiddenToken(Tree node) { return nullOr(n -> hiddenToken(tokenFrom(n)), node); }
    public Token hiddenToken(Token token) {
        if (hasNone(token) || hasNone(tokenStream())) return null;
        Token candidate = tokenStream().get(token.getTokenIndex() - 1);
        return (candidate.getChannel() == Token.HIDDEN_CHANNEL ? candidate : null);
    }

    protected static final String ALL = "ALL";
    protected static final String Wild = "*";
    protected static final String Hash = "#";
    protected static final String Blank = " ";
    protected static final String Quote = "\"";
    protected static final String Trophe = "'";
    protected static final String Arrayed = "[]";
    protected static final String Etc = "...";

    protected static final Emission[] EmptyArgs = {};
    protected static final List<Emission> EmptyList = wrap(EmptyArgs);

} // Item
