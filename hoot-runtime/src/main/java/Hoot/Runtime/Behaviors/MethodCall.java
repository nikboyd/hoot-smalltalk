package Hoot.Runtime.Behaviors;

import static Hoot.Runtime.Functions.Utils.*;
import Hoot.Runtime.Faces.Selector;
import Hoot.Runtime.Values.Value;
import java.util.*;

/**
 * Calls a method.
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2025 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class MethodCall implements Call {

    @Override public String methodName() { return this.methodName; }
    @Override public Object receiver() { return values().isEmpty()? null: values().get(0).value(); }
    @Override public List<Object> argumentValues() {
        List<Object> list = count() == 0? emptyList(): map(argList(), arg -> arg.value());
        return list; }

    public MethodCall() {}
    public MethodCall(List<Value<?>> values) { values().addAll(values); }
    public static <R> R make(Selector s) { return MethodCall.with().call(s); }

    public static MethodCall withWrapped(Object... values) {
        List<Value<?>> vs = map(wrap(values), v -> Value.with(v));
        if (!vs.isEmpty()) vs.get(0).makeSelfish();
        return new MethodCall(vs); }

    public static MethodCall with(Value<?>... values) {
        return new MethodCall(wrap(values)); }
    
    final List<Value<?>> values = emptyList();
    private List<Value<?>> values() { return this.values; }
    private int count() { return values().size(); }

    private List<Value<?>> argList() {
        return (values().get(0).isSelfish() ? 
            values().subList(1, count()) : values()); }

    private String methodName = "";
    @SuppressWarnings("unchecked")
    public <R> R call(Selector s) {
        this.methodName = s.name();
        return (R)callMethod();
    }

} // MethodCall
