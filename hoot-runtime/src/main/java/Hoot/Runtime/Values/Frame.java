package Hoot.Runtime.Values;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import Hoot.Runtime.Faces.Valued;
import Hoot.Runtime.Faces.Logging;
import Hoot.Runtime.Faces.Selector;
import Hoot.Runtime.Blocks.Enclosure;
import Hoot.Runtime.Behaviors.Invoke;
import static Hoot.Runtime.Blocks.Enclosure.*;
import static Hoot.Runtime.Functions.Utils.*;
import static Hoot.Runtime.Faces.Logging.*;

/**
 * A block closure stack frame. Presumes usage by a single thread.
 * Contains an ordered, named list of values, and a value stack.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 * @see <a href="https://gitlab.com/hoot-smalltalk/hoot-smalltalk/tree/master/LICENSE.txt">LICENSE for more details</a>
 */
public class Frame implements Logging {

    public Frame() { }
    public Frame(String scopeID) { this.scopeID = scopeID; } //makeCurrent(); }
    public static Frame withValues(Value<?> ... values) { return new Frame().with(values); }
    public static final String className() { return Frame.class.getSimpleName(); }
    public static final String name(int scopeLevel) { return "f" + scopeLevel; }
    public void purge() { valueMap().clear(); values().clear(); stack().clear(); }

    private String scopeID = Empty;
    public String scope() { return this.scopeID; }
    public String describe() { return knowsMethod() ? scope() : ""+this.hashCode(); }
    public boolean matches(Frame frame) { return hasAny(frame) && scope().equals(frame.scope()); }
    public boolean knowsMethod() { return !scope().isEmpty(); }

    public <R> R exit(String scopeID, R result) { return currentClosure().exitMethod(this, result); }
    public <R> R exit(String scopeID, Function<Frame,R> block) {
        return currentClosure().exitMethod(this, hasNone(block)? null : block.apply(this)); }

    private final Stack<Value<?>> stack = new Stack<>();
    private Stack<Value<?>> stack() { return this.stack; }
    private Value<?> stackAt(int index) { return stack().get(index); }
    private int selfIndex() { return findSelf(stackDepth() - 1); }
    private boolean selfAt(int index) { return stackAt(index).isSelfish(); }
    private int findSelf(int index) { // note: index result after loop
        for (; index > 0; index--) if (selfAt(index)) return index; return index; }

    public int stackDepth() { return stack().size(); }
    public boolean hasResult() { return (stackDepth() > 0); }
    public Value<?> result() { return (stack().empty() ? null : top()); }

    public void push(Value<?> v) { stack().push(v); }
    public <V> V push(V value) { push(Value.with(value)); return value; }
    public <V> V topValue() { return top().value(); }
    public <V> V topValue(Class<V> valueType) { return topValue(); }
    public <V> V popValue() { return pop().value(); }
    public <V> V popValue(Class<V> valueType) { return popValue(); }
    public Value<?> top() { return stack().peek(); }
    public Value<?> pop() { return stack().pop(); }

    public Frame popFrame(int count) { return new Frame().with(popReversed(count)); }
    public Value<?>[] popForward(int count) { return popOut(count, true); }
    public Value<?>[] popReversed(int count) { return popOut(count, false); }

    private static final Value<?>[] EmptyResult = { };
    private Value<?>[] popOut(int count, boolean forward) {
        if (count < 0 || count > stackDepth()) return EmptyResult;
        Value<?>[] results = new Value<?>[count]; int index = 0;
        if (forward)
            while (index < count) results[index++] = pop();
        else // reversed
            while (count > 0) results[--count] = pop();

        return results;
    }


    // a map of named values for lookups
    private final HashMap<String, Value<?>> map = new HashMap<>();
    private HashMap<String, Value<?>> valueMap() { return this.map; }
    private boolean isMapped(Value<?> v) { return valueMap().containsKey(v.name()); }

    @SuppressWarnings("unchecked")
    private static <V> Class<Value<V>> getType(Value<?> v) { return (Class<Value<V>>)v.getClass(); }
    private <V> Value<V> get(String name) { Value<?> v = map.get(name); return Value.asValue(getType(v), v); }
    private <V> Value<V> get(int index) { Value<?> v = values.get(index); return Value.asValue(getType(v), v); }

    // values held by this frame
    private final List<Value<?>> values = new ArrayList<>();
    private List<Value<?>> values() { return this.values; }
    public boolean valid(int index) { return (index >= 0 && index < countValues()); }
    public boolean hasValue(String name) { return !isEmpty(name) && valueMap().containsKey(name); }
    private <V> Value<V> value(String name) { return get(name); }
    private <V> Value<V> value(int index) { return get(index); }
    public <V> Value<V> getValue(int index) { return valid(index) ? value(index) : null; }
    public <V> Value<V> getValue(String name) { return hasValue(name) ? value(name) : null; }
    public <V> V getValue(int index, Class<V> valueType) { return Value.as(valueType, getValue(index)); }
    public <V> V getValue(String name, Class<V> valueType) { return Value.as(valueType, getValue(name)); }
    public int countValues() { return values().size(); }

    public Frame with(Value<?> ... values) { return this.with(wrap(values)); }
    public Frame with(List<Value<?>> values) { values.forEach((v) -> { adopt(v); }); return this; }
    public Frame withAll(Object... values) {
        int[] index = {0}; wrap(values).forEach(v -> bind(index[0]++, v)); return this; }

    public <V> Frame bind(String valueName, V value) {
        return valueName.isEmpty() ? this : with(Value.named(valueName, value)); }

    public <V> Frame bind(int index, V value) {
        if (valid(index)) value(index).bind(value);
        else adopt(Value.from(index).bind(value));
        return this; }

    private void adopt(Value<?> v) { if (isMapped(v)) { bind(v); } else { add(v); mapValue(v); } }
    private void add(Value<?> v) { if (v.isSelfish()) values().add(0, v); else values().add(v); }
    private void bind(Value<?> v) { getValue(v.name()).bind(v.value()); }
    private void mapValue(Value<?> v) { valueMap().put(v.name(), v); }

    public <R> R evaluate(Supplier block) { return evaluate(Enclosure.withBlock(block)); }
    public <R> R evaluate(Consumer<Frame> block) { return evaluate(Enclosure.withBlock(block)); }
    public <R> R evaluate(Function<Frame,Valued> block) { return evaluate(Enclosure.withBlock(block)); }
    public <R> R evaluate(Enclosure closure) { return closure.withFrame(this).value(); }

    static final String Comma = ", ";
    static final String FrameReport = "Frame<%s>:[ %s ]";
    @Override public String toString() {
        String types = joinWith(Comma, map(values(), v -> v.valueType().getSimpleName()));
        String valued = joinWith(Comma, map(values(), v -> v.toString()));
        String result = format(FrameReport, types, valued); return result; }


    /**
     * Pops message operands from the stack, and calls the selected receiver method with its arguments.
     * @param <R> a result type
     * @param selector a method selector
     * @return the result of the method call
     * @throws Throwable if raised
     */
    public <R> R perform(Selector selector) throws Throwable {
        return Invoke.with(popReversed(countArguments())).call(selector); }

    public int countArguments() {
        return (stackDepth() == 0) ? 0 :
               (selfIndex() < 0) ? stackDepth() : stackDepth() - selfIndex(); }

} // Frame
