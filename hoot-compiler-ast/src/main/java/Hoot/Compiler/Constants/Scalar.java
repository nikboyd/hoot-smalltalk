package Hoot.Compiler.Constants;

import Hoot.Runtime.Emissions.Emission;
import Hoot.Runtime.Emissions.NamedItem;
import static Hoot.Runtime.Functions.Exceptional.*;

/**
 * A scalar constant.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 1999,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public abstract class Scalar extends Constant {

    public Scalar() { super(); }
    public Scalar(String value) { this(); this.value = value; }
    public Scalar(NamedItem container) { this(container, Empty); }
    public Scalar(NamedItem container, String aString) { super(container); value = aString; }
    @Override public String description() { return getClass().getName() + " = " + rawValue(); }

    protected String value = Empty;
    public String rawValue() { return this.value; }
    public void value(String value) { this.value = value; }
    @Override public String encodedValue() { return rawValue(); }

    public abstract Class primitiveType();
    public abstract String primitiveFactoryName();
    public String declaredType() { return primitiveFactoryName(); }

    @Override public Emission emitOperand() { return emitScalar(); }
    @Override public Emission emitPrimitive() { return emitItem(encodedValue()); }
    public Emission emitScalar() { return emitScalar(encodedValue(), primitiveFactoryName()); }

    static final Class DefaultType = java.lang.Object.class;
    @Override public Class resolvedType() {
        return defaultOrTryQuietly(() -> Class.forName(resolvedTypeName()), DefaultType); }

} // Scalar
