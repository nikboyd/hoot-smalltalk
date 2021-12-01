package Hoot.Runtime.Exceptions;

import java.util.*;
import Hoot.Runtime.Blocks.*;
import Hoot.Runtime.Faces.Valued;
import Hoot.Runtime.Values.Cacheable;
import Hoot.Runtime.Values.CachedStack;
import static Hoot.Runtime.Functions.Utils.*;

/**
 * Provides a context for evaluating blocks and handling exceptions.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 * @see <a href="https://gitlab.com/hoot-smalltalk/hoot-smalltalk/tree/master/LICENSE.txt">LICENSE for more details</a>
 */
public class ExceptionContext implements Valued, Cacheable<ExceptionContext> {

    // an empty marker context for retry
    protected static final ExceptionContext RetryToken = new ExceptionContext();
    protected static final ExceptionContext Unhandled = new ExceptionContext();
    protected boolean isUnhandled() { return Unhandled == this; }

    private static final CachedStack<ExceptionContext> ContextStack = new CachedStack<>();
    static { ContextStack.defaultIfEmpty(Unhandled); }

    protected static CachedStack<ExceptionContext> contextStack() { return ContextStack; }
    @Override public CachedStack<ExceptionContext> itemStack() { return contextStack(); }

    protected static Stack<ExceptionContext> contexts() { return contextStack().cachedStack(); }
    public static ExceptionContext environment() { return contextStack().top(); }
    protected static ExceptionContext pop() { return contextStack().pop(); }
    protected void push() { contextStack().push(this); }

    protected int stackIndex = 0;
    @Override public int stackIndex() { return this.stackIndex; }
    @Override public void stackIndex(int value) { this.stackIndex = value; }

    public static Enclosure findHandler(HandledException.Metatype type) { return findHandler(type.primitiveClass()); }
    public static Enclosure findHandler(Class type) { return environment().findHandlerFor(type); }

    public static Enclosure findPriorHandler(HandledException.Metatype type) { return findPriorHandler(type.primitiveClass()); }
    public static Enclosure findPriorHandler(Class type) { return environment().priorContext().findHandlerFor(type); }

    public boolean hasPriorHandler(HandledException ex) {
        Enclosure pass = priorContext().findHandlerFor(ex.valueType()); return hasAny(pass); }

    public void passFrom(HandledException ex) {
        ExceptionContext prior = priorContext();
        Enclosure pass = prior.findHandlerFor(ex.valueType());
        if (hasAny(pass)) pass.handleSignaled(ex); }

    protected ExceptionContext withIndex(int index) { this.stackIndex = index; return this; }
    public ExceptionContext priorContext() { return isBottom()? Unhandled : contexts().get(stackIndex - 1); }
    public Enclosure findHandler(HandledException ex) { return findHandlerFor(ex.valueType()); }
    public Enclosure findHandlerFor(Class type) {
        if (isUnhandled()) return null; // check empty handlers?
        if (handles(type)) return handlerFor(type);
        return priorContext().findHandlerFor(type);
    }

    protected MonadicValuable continuation;
    public MonadicValuable continuation() { return this.continuation; }
    public ExceptionContext continuation(MonadicValuable block) { this.continuation = block; return this; }
    public ExceptionContext $return(Valued value) { continuation().value(value); return this; }

    protected MonadicValuable responseBlock;
    public MonadicValuable responseBlock() { return this.responseBlock; }
    public ExceptionContext responseBlock(MonadicValuable block) { this.responseBlock = block; return this; }

    protected NiladicValuable retryBlock;
    public NiladicValuable retryBlock() { return this.retryBlock; }
    public ExceptionContext retryBlock(NiladicValuable block) { this.retryBlock = block; return this; }
    public ExceptionContext retry(NiladicValuable block) { retryBlock(block); return this; }
    public ExceptionContext retry() { $return(RetryToken); return this; }

    protected MonadicValuable resultContinuation;
    public MonadicValuable resultContinuation() { return this.resultContinuation; }
    public ExceptionContext resultContinuation(MonadicValuable block) { this.resultContinuation = block; return this; }
    public ExceptionContext resume(Valued value) { resultContinuation().value(value); return this; }

    protected Valued evaluateResponse_for(Arguable block, HandledException ex) {
        return (0 == block.argumentCount().intValue()) ?
            ((NiladicValuable)block).value() : (evaluateResponse_with((MonadicValuable)block, ex)); }

    protected Valued evaluateResponse_with(MonadicValuable block, HandledException ex) {
        resultContinuation(Enclosure.withBlock((f) -> { return f.getValue(0).value(); }));
        return block.value(ex); }

    protected Valued evaluateProtected(NiladicValuable block) {
        continuation(Enclosure.withBlock((f) -> { return f.getValue(0).value(); }));
        return block.value(); }

    public Valued handle(HandledException ex) {
        Valued result = null;
        Stack<ExceptionContext> stack = contexts();
//        var environ = pop();
        try {
            push();
            result = this.evaluateResponse_for(responseBlock(), ex);
        }
        finally {
            pop();
//            push(environ);
        }
        return result;
    }

    public Valued activateDuring(NiladicValuable block) {
        Valued result = null;
        Stack<ExceptionContext> stack = contexts();
//        var environ = pop();
        try {
            push();
            retryBlock(block);
            do {
                result = this.evaluateProtected(retryBlock());
            }
            while (RetryToken == result);
        }
        finally {
            pop();
//            push(environ);
        }
        return result;
    }

    public static Valued during_handle(NiladicValuable aBlock, MonadicValuable handlerBlock) {
        ExceptionContext context = new ExceptionContext().withHandler((Enclosure)handlerBlock);
        return context.activateDuring(aBlock); }

    public static Valued activateHandler(HandledException ex) {
        Enclosure handler = findHandler(ex.valueType());
        return hasAny(handler)? handler.handleSignaled(ex) : ex.defaultAction(); }

    private final HashMap<Class,Enclosure> handlers = emptyMap(Class.class, Enclosure.class);
    private HashMap<Class,Enclosure> handlerMap() { return this.handlers; }
    private Collection<Enclosure> handlers() { return handlerMap().values(); }
    protected boolean hasHandlers() { return !handlerMap().isEmpty(); }
    protected void clearHandlers() { handlerMap().clear(); }
    protected ExceptionContext withHandler(Enclosure handlerBlock) {
        responseBlock(handlerBlock); handlerBlock.context(this);
        handlerBlock.coveredExceptions().forEach((h) -> handlerMap().put(h, handlerBlock));
        return this; }

    public Enclosure handlerFor(HandledException ex) { return nullOr((x) -> handlerFor(x.valueType()), ex); }
    public Enclosure handlerFor(Class type) { return findFirst(handlers(), (h) -> h.handles(type)); }
    public boolean handles(Class type) {return matchAny(handlers(), (h) -> h.handles(type)); }

} // ExceptionContext
