package Hoot.Compiler.Expressions;

import java.util.*;
import Hoot.Compiler.Constants.Constant;
import Hoot.Compiler.Constants.LiteralName;
import Hoot.Runtime.Emissions.Emission;
import static Hoot.Runtime.Functions.Utils.hasOne;

/**
 * A constructor invocation.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class Construct extends KeywordMessage {

    public Construct() { super(); }
    public Construct(List<Formula> terms) { this(); this.terms.addAll(terms); }
    public static Construct with(Constant c, List<Formula> terms) {
        Construct result = new Construct(terms);
        return result.withTerm(
            Formula.with(UnarySequence.with(Primary.with((LiteralName)c)))); }

    @Override public boolean isConstruct() { return true; }
    @Override public Emission emitOperand() { return emitConstruct(formula().emitOperand(), formulaCodes()); }
    @Override public Emission emitPrimitive() { return emitConstruct(formula().emitPrimitive(), formulaPrims()); }

} // Construct
