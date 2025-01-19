package Hoot.Compiler.Scopes;

import Hoot.Runtime.Notes.*;

/**
 * A unary method signature.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2025 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class UnarySignature extends BasicSignature {

    public UnarySignature() { super(); }
    public static UnarySignature with(DetailedType resultType, String selector) {
        UnarySignature result = new UnarySignature();
        result.resultType = resultType;
        result.selector = selector;
        return result; }

    protected String selector = Empty;
    @Override public String name() { return selector; }
    @Override public BasicSignature eraseTypes() {
        return UnarySignature.with(DetailedType.RootType, selector); }

} // UnarySignature
