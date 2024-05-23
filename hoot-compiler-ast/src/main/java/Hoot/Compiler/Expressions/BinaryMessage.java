package Hoot.Compiler.Expressions;

import java.util.*;
import Hoot.Runtime.Names.Operator;
import Hoot.Runtime.Behaviors.Scope;
import Hoot.Runtime.Emissions.Emission;
import static Hoot.Runtime.Functions.Utils.*;

/**
 * A binary message.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 1999,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class BinaryMessage extends Message {

    public BinaryMessage() { super(Scope.currentBlock()); }
    public BinaryMessage(Operator op) { this(); operator = op; }
    public static BinaryMessage with(Operator op, Formula term) { return new BinaryMessage(op).withTerm(term); }

    Operator operator;
    public Operator operator() { return this.operator; }
    private String binaryOpName() { return Blank + operator().name(); }
    @Override public String methodName() { return operator().methodName(); }
    @Override public boolean containsExit() { return primaryTerm().exitsMethod(); }
    public boolean isAssignment() { return operator().assignsValue(); }

    @Override public List<Emission> emitArguments() {
        Emission[] args = { primaryTerm().emitOperand() }; return wrap(args); }

    @Override public Emission emitOperand() {
        return (this.hasPrimitiveContext() && !operator().isCast()) ?
                emitPrimitiveOperation() : emitCall(methodName()); }

    @Override public Emission emitPrimitive() { return emitPrimitiveOperation(); }
    public Emission emitPrimitiveOperation() {
        return emitOperation(binaryOpName(), primaryTerm().emitPrimitive()); }

} // BinaryMessage
