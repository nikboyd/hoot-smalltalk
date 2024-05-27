package Hoot.Compiler.Scopes;

import java.util.*;
import Hoot.Runtime.Emissions.*;
import Hoot.Runtime.Faces.Resulting;
import static Hoot.Runtime.Functions.Utils.*;
import Hoot.Compiler.Expressions.*;

/**
 * Contains the statements for a block.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class BlockContent extends Item implements ScopeSource, Resulting {

    Block block;
    @Override public Block block() { return this.block; }
    @Override public Method method() { return block().method(); }

    public BlockContent() { this(Block.currentBlock()); }
    public BlockContent(Block b) { super(b); this.block = b; }
    public static BlockContent from(Item item) { return nullOr(b -> (BlockContent)b, item); }
    @Override public void clean() { super.clean(); statements().forEach(s -> s.clean()); }

    public static BlockContent emptyBlock() { return new BlockContent(); }
    public static BlockContent with(List<Statement> statements, Expression exit, int periodCount) {
        BlockContent result = new BlockContent();
        result.periodCount = periodCount;
        result.statements.addAll(select(statements, s -> hasOne(s)));
        result.addResult(exit); // can have exit == null
        return result.acquireStatements();
    }

    int periodCount = 0;
    List<Statement> statements = emptyList(Statement.class);
    public int countPeriods() { return this.periodCount; }
    public int countStatements() { return this.statements.size(); }
    public List<Statement> statements() { return this.statements; }
    @Override public boolean isEmpty() { return this.statements.isEmpty(); }
    private Statement firstStatement() { return this.isEmpty() ? null : statements().get(0); }
    private Statement finalStatement() { return this.isEmpty() ? null : statements().get(countStatements() - 1); }

    public void add(Statement s) { if (s != null) this.statements.add(s); }
    public void add(Construct c) {
        if (c != null) {
            this.statements.add(0, Statement.with(c).inside(this));
            this.periodCount++;
        }
    }

    public boolean isNested() { return block().isNested(); }
    @Override public int nestLevel() { return block().nestLevel(); }
    @Override public boolean containsExit() { return !isEmpty() && matchAny(statements(), s -> s.exitsMethod()); }

    private BlockContent acquireStatements() {
        this.statements().forEach(s -> s.inside(this));

        // ensure final statement is a result when needed
        if (countStatements() > countPeriods()) {
            if (!block().hasPrimitiveContext() &&
                !block().hasVoidedContext()) {
                finalStatement().makeResult();
            }
        }
        return this;
    }

    boolean hasResult = false;
    private boolean isAbstracted() { return face().isInterface() || notes().isAbstract(); }
    private boolean noContent(Expression x) { return hasNone(x) && statements().isEmpty(); }
    public void addResult(Expression x) {
        if (!this.isEmpty()) {
            if (finalStatement().throwsException()) {
                // drop unreachable result
                return; // done here
            }
        }

        Block b = block();

        // for methods ...
        if (hasOne(b) && b.isMethod()) {
            Method m = Method.currentMethod();
            if (x == null && m.isPrimitive()) {
                // primitive methods are coded exactly as wanted
                return; // done here
            }

            if (noContent(x) && isAbstracted()) {
                // abstract methods are empty of content
                return; // done here
            }

            if (m.isConstructor() || m.returnsVoid()) {
                // correct the coding error, make x result a normal statement
                Statement result = nullOr(r -> Statement.with(r), x);
                add(result);
                return; // done here
            }

            // normal methods always return a result, which may be yourself
            Statement result = Statement.with(hasOne(x)? x: UnarySequence.yourself());
            add(result.makeResult());
            this.hasResult = true;
            return; // done here
        }

        // otherwise, ensure the block returns its result
        if (x != null) {
            Statement result = Statement.with(x);
            add(result.makeResult());
            this.hasResult = true;
        }
    }

    /**
     * @return whether this block needs to be evaluated within a frame
     */
    public boolean needsFrame() {
        if (isEmpty()) return false;
        int exits = countAny(statements(), s -> s.exitsMethod());
        if (Block.MethodLevel == nestLevel() && finalStatement().isExit()) exits--;

        // are there any exits in nested blocks? is this method primitive?
        boolean framed = (exits > 0) && !method().isPrimitive();
        return framed;
    }


    public Emission emitFinalValue() { return finalStatement().emitValue(); }
    @Override public Emission emitItem() { return emitLines(emitStatements()); }

    public Emission emitConstruct() {
        if (this.isEmpty()) return null;
        return firstStatement().isConstruct() ? firstStatement().emitItem() : null;
    }

    public List<Emission> emitStatements() {
        return mapList(statements(), (s) -> !s.isConstruct(), (s) -> s.emitItem()); }

} // BlockContent
