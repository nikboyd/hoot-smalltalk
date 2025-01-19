package Hoot.Compiler.Constants;

import Hoot.Runtime.Faces.Selector;
import Hoot.Runtime.Values.Operand;
import Hoot.Runtime.Behaviors.Scope;
import Hoot.Compiler.Scopes.ScopeSource;
import Hoot.Runtime.Emissions.NamedItem;

/**
 * A constant value.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 1999,2025 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public abstract class Constant extends Operand implements ScopeSource, LiteralSource {

    public Constant() { this(Scope.current()); }
    public Constant(NamedItem container) { super(container); }

    @Override public boolean isLiteral() { return true; }
    @Override public boolean optimizes(Selector selector) { return true; }
    public String encodedValue() { return emitPrimitive().render(); }

} // Constant
