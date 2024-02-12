package Hoot.Compiler.Scopes;

import java.util.*;
import Hoot.Runtime.Names.Operator;
import Hoot.Runtime.Values.Variable;
import Hoot.Runtime.Notes.DetailedType;
import static Hoot.Runtime.Functions.Utils.*;

/**
 * A binary method signature.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class BinarySignature extends BasicSignature {

    public BinarySignature() { super(); }
    public static BinarySignature with(DetailedType resultType, List<Variable> args, Operator op) {
        BinarySignature result = new BinarySignature();
        result.resultType = resultType;
        result.args.withAll(args);
        result.op = op;
        return result; }

    protected Operator op;
    @Override public String name() { return emptyOr(namedOp -> namedOp.methodName(), this.op); }
    @Override public BasicSignature eraseTypes() {
        return BinarySignature.with(DetailedType.RootType, map(arguments(), arg -> arg.withErasure()), op); }

} // BinarySignature
