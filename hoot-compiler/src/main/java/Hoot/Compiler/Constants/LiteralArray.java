package Hoot.Compiler.Constants;

import java.util.*;
import Hoot.Runtime.Emissions.*;
import static Hoot.Runtime.Functions.Utils.*;
import static Hoot.Runtime.Names.Keyword.*;
import static Hoot.Runtime.Behaviors.HootRegistry.*;

/**
 * A literal Array.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 1999,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 * @see <a href="https://gitlab.com/hoot-smalltalk/hoot-smalltalk/tree/master/LICENSE.txt">LICENSE for more details</a>
 */
public class LiteralArray extends LiteralCollection {

    public LiteralArray() { super(); }
    public LiteralArray(List<Constant> items) { this(); values.addAll(items); }
    public static LiteralArray withItems(List<Constant> items) { return new LiteralArray(items); }

    @Override public Class primitiveType() { return Object[].class; }
    @Override public String encodedValue() { return emitArray().render(); }
    @Override public String primitiveFactoryName() { return Array; }
    @Override public String resolvedTypeName() {
        return (file().needsCollections() ? RootType().fullName() : ArrayType().fullName()); }

    protected List<Constant> values = emptyList(Constant.class);
    public List<Constant> values() { return copyList(this.values); }
    public Emission emitArray() { return emitArray(map(values, v -> v.emitOperand())); }
    @Override public Emission emitOperand() { return emitCast(declaredType(), emitArray()); }
    @Override public Emission emitPrimitive() { return emitArrayPrim(map(values, v -> v.emitOperand())) ; }

} // LiteralArray
