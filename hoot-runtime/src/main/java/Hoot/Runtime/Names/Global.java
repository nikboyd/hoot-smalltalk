package Hoot.Runtime.Names;

import Hoot.Runtime.Behaviors.Scope;
import java.util.*;

import Hoot.Runtime.Values.Operand;
import Hoot.Runtime.Emissions.Emission;
import static Hoot.Runtime.Names.Name.*;
import static Hoot.Runtime.Functions.Utils.*;

/**
 * A global reference.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class Global extends Operand {

    protected Global() { super(Scope.current()); }
    protected Global(List<String> names) { this(); this.names.addAll(names); }

    @Override public int hashCode() { return isEmpty() ? super.hashCode() : fullName().hashCode(); }
    protected boolean equals(Global g) { return (isEmpty() && g.isEmpty()) || fullName().equals(g.fullName()); }
    @Override public boolean equals(Object global) {
        return hasAny(global) && getClass() == global.getClass() && falseOr(g -> this.equals(g), (Global) global); }

    public static Global named(String name) { return Global.with(name); }
    public static Global with(String... names) { return Global.withList(wrap(names)); }
    public static Global withList(List<String> names) { return new Global(names).fixArrayed(); }

    private boolean primary = false;
    public boolean isPrimary() { return this.primary; }
    public Global makePrimary() { this.primary = true; return this; }

    protected boolean arrayedType = false;
    public boolean isArrayed() { return arrayedType; }
    public Global makeArrayed(boolean value) { this.arrayedType = value; return this; }
    protected Global fixArrayed() {
        if (!isEmpty() && shortName().endsWith(Arrayed)) {
            this.names.add(removeTail(this.names.remove(countNames() - 1), Arrayed));
            makeArrayed(true);
        }
        return this;
    }

    private final List<String> names = emptyList(String.class);
    public int countNames() { return names.size(); }
    public List<String> names() { return isEmpty() ? emptyList(String.class) : copyList(this.names); }
    public String metaName(boolean asType) { return formType(typeName(), asType ? Name.Metatype : Name.Metaclass); }
    public String typeName() { return typeResolver().typeName(); }

    @Override public boolean isEmpty() { return names.isEmpty(); }
    @Override public TypeName typeResolver() { return TypeName.with(names()).makeArrayed(isArrayed()); }

    public boolean isSingular() { return countNames() == 1; }
    public boolean isVoid()    { return isSingular() && shortName().equals(Keyword.Void); }
    public boolean isBoolean() { return isSingular() && shortName().equals(Keyword.Boolean); }
    public boolean isGenericType() { return isSingular() && shortName().endsWith(Keyword.TypeSuffix); }
    public boolean isPrimitiveBoolean() { return !isSingular() && shortName().equals(Keyword.Boolean); }

    @Override public String fullName() { return joinWith(Blank, names()); }
    @Override public String shortName() { return isEmpty() ? Empty : names().get(countNames() - 1); }
    @Override public String name() { return this.isPrimary() ? formType(names()) : typeName(); }

    public Map<String, Emission> extractGenerics(boolean wantsBases) {
        HashMap<String, Emission> results = emptyMap(Emission.class);
        if (this.isGenericType()) results.put(shortName(), emitItem());
        return results;
    }

    public List<String> inferGenerics() {
        List<String> results = emptyList(String.class);
        if (this.isGenericType()) results.add(shortName());
        return results;
    }

    @Override public Emission emitOperand() { return emitItem(name()); }
    public Emission emitMetaName(boolean asType) { return emitItem(metaName(asType)); }

} // Global
