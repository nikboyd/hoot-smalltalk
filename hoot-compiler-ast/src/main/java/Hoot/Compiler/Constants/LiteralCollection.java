package Hoot.Compiler.Constants;

import Hoot.Runtime.Emissions.*;
import Hoot.Runtime.Faces.Selector;

/**
 * A literal collection.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 1999,2025 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public abstract class LiteralCollection extends Scalar {

    public LiteralCollection() { super(); }
    public LiteralCollection(NamedItem container) { super(container); }
    public LiteralCollection(NamedItem container, java.lang.String aString) { super(container, aString); }

    @Override public boolean optimizes(Selector selector) { return !file().needsCollections(); }
    @Override public Emission emitOperand() {
        return file().needsCollections() ? super.emitOperand() : emitCast(declaredType(), super.emitOperand()); }

} // LiteralCollection
