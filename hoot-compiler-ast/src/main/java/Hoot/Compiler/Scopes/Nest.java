package Hoot.Compiler.Scopes;

import Hoot.Runtime.Emissions.Emission;
import Hoot.Runtime.Values.Operand;

/**
 * A nested blocked.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 1999,2025 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class Nest extends Operand {

    public Nest(Block aBlock) { super(aBlock.container()); nestedBlock = aBlock.inside(this); }
    public static Nest emptyBlock() { return new Nest(Block.emptyBlock()); }

    Block nestedBlock;
    public Block nestedBlock() { return nestedBlock; }
    @Override public void clean() { super.clean(); nestedBlock.clean(); }
    @Override public boolean isNest() { return true; }
//    @Override public boolean isExit() { return nestedBlock().isExit(); }
    @Override public boolean containsExit() { return nestedBlock().containsExit(); }

    @Override public Emission emitOperand() { return emitOptimized(); }
    @Override public Emission emitOptimized() { return nestedBlock().emitOptimized(); }

} // Nest
