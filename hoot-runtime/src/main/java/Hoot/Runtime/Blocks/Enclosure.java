package Hoot.Runtime.Blocks;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import Hoot.Runtime.Faces.*;
import Hoot.Runtime.Functions.*;
import Hoot.Runtime.Values.Frame;
import Hoot.Runtime.Values.Cacheable;
import Hoot.Runtime.Values.CachedStack;
import Hoot.Runtime.Exceptions.ExceptionBase;
import Hoot.Runtime.Exceptions.ExceptionContext;
import Hoot.Runtime.Exceptions.HandledException;
import Hoot.Runtime.Exceptions.UnhandledException;
import static Hoot.Runtime.Functions.Utils.*;
import static Hoot.Runtime.Functions.Exceptional.*;
import static Hoot.Runtime.Functions.Exceptional.Result.*;
import static Hoot.Runtime.Names.Primitive.normalPriority;
import java.util.function.Predicate;

/**
 * A block enclosure.
 *
 * <h4>Enclosure Responsibilities:</h4>
 * <ul>
 * <li>knows a block</li>
 * <li>knows a block argument count</li>
 * <li>evaluates a block to produce a result</li>
 * </ul>
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 * @see <a href="https://gitlab.com/hoot-smalltalk/hoot-smalltalk/tree/master/LICENSE.txt">LICENSE for more details</a>
 */
public class Enclosure implements NiladicValuable, MonadicValuable, DyadicValuable, Cacheable<Enclosure> {

    static final Consumer<Frame> DoNothing = (Frame f) -> {};
    static final Enclosure DefaultContinuation = Enclosure.withBlock(DoNothing);
    public static Enclosure defaultContinuation() { return DefaultContinuation; }

    // convert closures to stack-based! --nik
    static final CachedStack<Enclosure> ClosureStack = new CachedStack<>();
    static { ClosureStack.defaultIfEmpty(defaultContinuation()); }

    @Override public CachedStack<Enclosure> itemStack() { return closureStack(); }
    protected static CachedStack<Enclosure> closureStack() { return ClosureStack; }

    protected static Stack<Enclosure> closures() { return closureStack().cachedStack(); }
    static public Enclosure currentClosure() { return closureStack().top(); }

    private int stackIndex = 0;
    @Override public int stackIndex() { return this.stackIndex; }
    @Override public void stackIndex(int value) { this.stackIndex = value; }

    protected Enclosure(Function<Frame,?> block) { this.block = block; }

    public static Enclosure withBlock(Supplier<?> block) {
        if (block == null) throw new IllegalArgumentException("block missing");
        return withBlock(f -> { return block.get(); }); }

    public static Enclosure withBlock(Consumer<Frame> block) {
        if (block == null) throw new IllegalArgumentException("block missing");
        return new Enclosure(f -> { block.accept(f); return null; }); }

    public static Enclosure withBlock(Function<Frame,?> block) {
        if (block == null) throw new IllegalArgumentException("block missing");
        return new Enclosure(block); }

    public static Enclosure withQuiet(Argued<Frame,?> block) {
        if (block == null) throw new IllegalArgumentException("block missing");
        return new Enclosure(f -> Exceptional.nullOrTryQuietly(block, f)); }

    public static Enclosure withQuieted(Runner block) {
        if (block == null) throw new IllegalArgumentException("block missing");
        return new Enclosure(f -> { Exceptional.runQuietly(block); return null; }); }

    @SuppressWarnings("unchecked")
    public <V> Function<Frame,V> block() { return (Function<Frame,V>)this.block; }
    private final Function<Frame,?> block;

    private Frame frame = new Frame();
    public Frame frame() { return this.frame; }
    public Enclosure withFrame(Frame frame) { this.frame = frame; return this; }
    @Override public IntegerValue argumentCount() { return IntegerValue.with(frame().countArguments()); }

    public Enclosure runQuiet() { Exceptional.runQuietly(() -> value()); return this; }
    public Enclosure runLoud()  { Exceptional.runLoudly(() -> value()); return this; }

    /**
     * @return a value resulting from the base block, after the termination block is also evaluated
     * @param terminationBlock always evaluated (finally)
     */
    public Valued ensure(Valuable terminationBlock) { unwindBlock(terminationBlock); return value(); }

    public Enclosure $catch(MonadicValuable terminationBlock) {
        runLoudly(() -> value(), caughtBy(terminationBlock)); return this; }

    private Handler<Throwable> caughtBy(MonadicValuable terminationBlock) {
        return (Throwable ex) -> { terminationBlock.value(ex); }; }

    public Valued defaultIfCurtailed(Valued defaultValue) {
        return defaultOrTrySurely(() -> value(), DebugHandler, defaultValue); }

    /**
     * @return a value resulting either from the base block, or the termination block if the base fails
     * @param terminationBlock evaluated only if an exception occurs (catch)
     */
    public Valued ifCurtailed(Valuable terminationBlock) {
        return this.on_do(ExceptionBase.type(), curtailed(terminationBlock)); }

    private MonadicValuable curtailed(Valuable terminationBlock) {
        return new MonadicValuable(){
            @Override public <V, R> R value(V value) { return terminationBlock.value(); }
            @Override public IntegerValue argumentCount() { return IntegerValue.with(1); }
        };
    }

    private Handler<Throwable> curtailment(Valuable terminationBlock) {
        return (Throwable ex) -> { terminationBlock.value(); }; }

    public <V> boolean testWithEach(Set<V> values) { return values.stream().anyMatch(each -> value(each)); }
    public <V> Set<?>  evaluateWithEach(Set<V> values) { return mapSet(values, each -> value(each)); }
//    public <V> List<?> evaluateWithEach(V ... values) { return evaluateWithEach(wrap(values)); }
    public <V> List<?> evaluateWithEach(List<V> values) { return map(values, each -> value(each)); }
    public <V,R> List<R> evaluateEach(List<V> values, Class<R> itemType) { return map(values, each -> value(each)); }
    public <V> List<String> collectStringsFrom(List<V> items) {
        return collectWith(emptyList(String.class), items, (list,each) -> { value_value(list, each); }); }

    @Override public <R> R value() { return $return(evaluated()); }
    @Override public <V, R> R value(V value) { return valueWith(value); }
    @Override public <A, B, R> R value_value(A a, B b) { return valueWith(a, b); }

    static final Object Placeholder = new Object();
    public Enclosure valueNames(String... valueNames) { return valueNames(wrap(valueNames)); }
    public Enclosure valueNames(List<String> valueNames) {
        valueNames.forEach(vn -> frame().bind(vn, Placeholder)); return this; }

    public <R> R valueWith(Object... values) {
        return nullOrTryLoudly(
                () -> { frame().withAll(values); return value(); },
                () -> { frame().purge(); }); }

    public Thread fork() { return forkAt(normalPriority()); }
    public Thread forkAt(IntegerValue priority) { return forkAt(priority.intValue()); }
    public Thread forkAt(int priority) {
        Thread result = new Thread(() -> value());
        result.setPriority(priority);
        result.start();
        return result; }

    private Enclosure unwindBlock = DefaultContinuation;
    public Enclosure unwindBlock() { return this.unwindBlock; }
    protected void activateUnwind() { if (hasAny(unwindBlock())) unwindBlock().value(); }
    protected Enclosure unwindBlock(Valuable unwindBlock) {
        this.unwindBlock = (Enclosure)unwindBlock; return unwindBlock(); }

    private void makeCurrent() { closureStack().push(this); }

    @SuppressWarnings("unchecked")
    private <R> R evaluated() {
        try {
            makeCurrent();
            return (R)block().apply(frame());
        }
        finally {
            unwindIfStacked();
        }
    }

    private void popIfStacked() { closureStack().popIfTop(this); }
    protected void unwindIfStacked() { if (onStack()) { activateUnwind(); popIfStacked(); } }
    protected void unwindTo(Frame target) {
        if (hasNo(target)) return;
        if (target.knowsMethod() && !frame().matches(target)) {
            whisper("skipping " + frame().describe());
            if (hasPrior()) {
                Enclosure parent = priorItem();
                unwindIfStacked();
                parent.unwindTo(target);
            }
            else {
                unwindIfStacked();
            }
        }
    }

    public <R> R exitMethod(Frame methodFrame, R result) {
        whisper("seeking " + methodFrame.describe());
        unwindTo(methodFrame);
        return result;
    }

    // knows which kinds of exceptions it handles, where kind = exception type primitive class
    private final Set<Class<?>> handledExceptions = new HashSet<>();
    private void clearExceptions() { handledExceptions().clear(); }
    private Set<Class<?>> handledExceptions() { return this.handledExceptions; }
    public Set<Class<?>> coveredExceptions() { return copySet(handledExceptions()); }
    private boolean handlesAny() { return !handledExceptions().isEmpty(); }
    private boolean handlesNone() { return handledExceptions().isEmpty(); }
    private boolean handledMatch(Predicate<Class<?>> p) { return matchAny(handledExceptions(), p); }
    private boolean under(Class<?> type) { return handledMatch((ht) -> type.isAssignableFrom(ht)); }
    private boolean covers(Class<?> type) { return handledMatch((ht) -> ht.isAssignableFrom(type)); }
    private Class<?> covered(Class<?> type) { return findFirst(handledExceptions(), (ht) -> type.isAssignableFrom(ht)); }

    // only adopt distinct exception types which have no heritage relationship with others
    // if there is a relationship, adopt only the most general type over those collected
    public Enclosure asHandlerOf(HandledException.Metatype ... exceptionTypes) {
        List<HandledException.Metatype> types = wrap(exceptionTypes);
        if (types.isEmpty()) return this;
        types.forEach((type) -> {
            if (hasAny(type)) {
                Class<?> typeClass = type.outerClass();
                if (handlesNone()) {
                    handledExceptions().add(typeClass);
                    enableAsHandler();
                }
                else {
                    while (under(typeClass)) { // clean out any covered types
                        handledExceptions().remove(covered(typeClass));
                    }
                    handledExceptions().add(typeClass);
                    enableAsHandler();
                }
            }
        });
        return this; }

    public boolean handles(HandledException ex) { return handles(ex.valueType()); }
    public boolean handles(Class type) { return hasAny(type) && covers(type) && isActive(); }

    private boolean activeAsHandler = false;
    public boolean isActive() { return this.activeAsHandler; }
    public void disableAsHandler() { this.activeAsHandler = false; }
    public void enableAsHandler() { this.activeAsHandler = true; }

    protected void adoptAsHandlerFor(HandledException ex) { ex.currentHandler(this); disableAsHandler(); }
    protected void releaseHandlerFor(HandledException ex) { ex.currentHandler(null); enableAsHandler(); }
    protected <R> R fireHandler(HandledException exception) {
        adoptAsHandlerFor(exception); frame().bind(0, exception);
        R value = nullOrTryLoudly(() -> evaluated(), () -> releaseHandlerFor(exception));
        return $return(value); }

    public Valued handleSignaled(HandledException exception) {
        if (handles(exception)) return fireHandler(exception);
        UnhandledException.type().raise(exception); return this; }

    public ExceptionContext resume(Valued value) { return context().resume(value); }
    public ExceptionContext retry(NiladicValuable aBlock) { return context().retry(aBlock); }
    public ExceptionContext retry() { return context().retry(); }

    public <R> R $return(R v) {
//        unwindIfNeeded();
        return (R)v;
    }

    @SuppressWarnings("unchecked")
    @Override public Valued on_do(Valued.Metatype exceptionType, MonadicValuable handler) {
        Enclosure alias = (Enclosure)handler;
        alias.asHandlerOf((HandledException.Metatype)exceptionType);
        return nullOrTryQuietly(
            () -> ExceptionContext.during_handle(this, handler),
            () -> alias.clearExceptions()); }

    private ExceptionContext context;
    public ExceptionContext context() { return this.context; }
    public Enclosure context(ExceptionContext ctx) { this.context = ctx; return this; }
    public void passFrom(HandledException ex) { if (hasAny(context())) context().passFrom(ex); }

} // Enclosure
