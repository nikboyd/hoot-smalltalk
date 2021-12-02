package Hoot.Runtime.Values;

import java.util.*;

import Hoot.Runtime.Behaviors.Scope;
import Hoot.Runtime.Behaviors.Typified;
import Hoot.Runtime.Emissions.Item;
import Hoot.Runtime.Maps.Library;
import Hoot.Runtime.Names.Primitive;
import Hoot.Runtime.Names.TypeName;
import static Hoot.Runtime.Functions.Utils.*;
import static Hoot.Runtime.Behaviors.HootRegistry.*;
import Hoot.Runtime.Emissions.NamedItem;

/**
 * Maintains an ordered and index-able collection of local variables as a symbol table.
 * Supports symbol lookup by name and writes variables in the order of their definition.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 1999,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class Table extends NamedItem {

    public Table(Scope container) { super(container); }

    /**
     * Maintains an indexed collection of symbols.
     */
    protected Map<String, Variable> contents = emptyMap(Variable.class);
    public int size() { return contents.size(); }

    /**
     * Maintains symbols in the order of their definition.
     */
    protected List<Variable> order = emptyList(Variable.class);
    public List<Variable> symbols() { return copyList(order); }
    public List<Variable> definedSymbols() { return select(order, v -> !v.isEmpty()); }
    @Override public boolean isEmpty() { return this.order == null || this.order.isEmpty(); }

    /**
     * Establishes (container) as the container for this scope.
     *
     * @param container the container scope.
     */
    @Override public void container(Item container) {
        super.container(container);
        if (!isEmpty()) {
            symbols().forEach(symbol -> symbol.container(container));
        }
    }

    public Table withAll(Table table) {
        table.order.forEach(v -> addSymbol(v));
        return this;
    }

    public Table withAll(Map<String, Variable> map) {
        map.values().forEach(v -> addSymbol(v));
        return this;
    }

    public Table withAll(List<Variable> symbols) {
        symbols.forEach(s -> addSymbol(s));
        return this;
    }

//    public String createSymbol() {
//        int nest = containerScope().nestLevel();
//        int size = size();
//        String name = "local" + nest + "_" + size;
//        Variable v = new Variable(containerScope());
//        v.name(name);
//        v.clean();
//        addSymbol(v);
//        return name;
//    }

    protected boolean captureSymbol(Variable symbol) {
        if (!symbol.isEmpty()) {
            if (!contents.containsKey(symbol.name())) {
                contents.put(symbol.name(), symbol);
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a new (empty) symbol to the table.
     */
    public void prepareSymbol() {
        if (!order.isEmpty()) {
            // first capture any existing symbol
            captureSymbol(currentSymbol());
        }
        order.add(new Variable(containerScope()));
    }

    /**
     * Adds a new (symbol) to the table.
     *
     * @param symbol a named symbol.
     */
    public void addSymbol(Variable symbol) {
        if (captureSymbol(symbol)) {
            order.add(symbol);
        }
    }

    /**
     * Returns the current symbol being defined by the compiler.
     *
     * @return the current symbol being defined by the compiler.
     */
    public Variable currentSymbol() {
        if (order.isEmpty()) {
            order.add(new Variable(containerScope()));
        }

        return order.get(order.size() - 1);
    }

    /**
     * Returns whether the local symbol table contains the named symbol.
     *
     * @param name the name of the desired symbol.
     * @return whether the local symbol table contains the named symbol.
     */
    public boolean containsSymbol(String name) {
        return contents.containsKey(name);
    }

    /**
     * Returns the named symbol.
     *
     * @param name the name of the desired symbol.
     * @return the named symbol, or null if the table does not contain the named symbol.
     */
    public Variable symbolNamed(String name) {
        return (Variable) contents.get(name);
    }

    /**
     * Cleans out any residue left from the parsing process and prepares the receiver for code generation.
     */
    @Override
    public void clean() {
        super.clean();
        if (order.isEmpty()) {
            return;
        }

        Variable end = order.get(order.size() - 1);
        if (end.isEmpty()) {
            order.remove(end);
        }

        symbols().forEach(symbol -> symbol.clean());
    }

    /**
     * Removes all the symbols from the table.
     */
    public void clear() {
        contents.clear();
        order.clear();
    }

    /**
     * Returns whether any of the symbols has a resolved type.
     *
     * @return whether any of the symbols has a resolved type.
     */
    public boolean hasTypedNames() { return matchAny(symbols(), s -> !RootType().matches(s.type())); }

    /**
     * @return whether all of the symbols have erasable types
     */
    public boolean hasErasableTypes() {
        for (Variable symbol : symbols()) {
            TypeName resolver = symbol.typeResolver();
            if (resolver.isUnknown()) return false;
            if (resolver.isEraseableType()) return true;

            Typified typeFace = Library.findFace(resolver.fullName());
            if (typeFace == null) return false;
            if (!typeFace.isEraseableType()) return false;
        }
        return true;
    }

    /**
     * Copies the symbols from the receiver to (aTable) without types.
     *
     * @param aTable a symbol table.
     */
//    public void eraseTypes(Table aTable) {
//        for (Variable symbol : symbols()) {
//            Variable newSymbol = Variable.named(symbol.name(), "", aTable.containerScope());
//            aTable.addSymbol(newSymbol);
//        }
//    }

    /**
     * Returns whether any of the symbols resolve to elementary type.
     *
     * @return whether any of the symbols resolve to elementary type.
     */
    public boolean hasElementaryNames() {
        for (Variable symbol : symbols()) {
            String typeName = symbol.type();
            if (Primitive.isElementaryType(typeName)) {
                return true;
            }

            Typified typeFace = Library.findFace(typeName);
            if (typeFace != null && Primitive.isElementaryType(typeFace.fullName())) {
                return true;
            }
        }
        return false;
    }
}
