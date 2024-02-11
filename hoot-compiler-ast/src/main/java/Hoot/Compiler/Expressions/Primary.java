package Hoot.Compiler.Expressions;

import java.util.*;
import Hoot.Runtime.Names.Global;
import Hoot.Runtime.Behaviors.Scope;
import Hoot.Runtime.Emissions.Emission;
import Hoot.Runtime.Names.TypeName;
import Hoot.Runtime.Notes.Note;
import Hoot.Runtime.Values.*;
import Hoot.Compiler.Scopes.*;
import Hoot.Compiler.Constants.*;
import static Hoot.Runtime.Functions.Utils.*;

/**
 * A primary value.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 1999,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class Primary extends Operand {

    public Primary() { super(Scope.current()); }
    protected Primary(Operand item) { this(); item(item); }
    public static Primary with(Operand item) { return new Primary(item); }
    public static Primary frameGlobal() { return with(Global.named(Frame.className())); }
    @Override public void clean() { super.clean(); if (hasOne(item)) this.item.clean(); }

    protected Operand item;
    public String itemClassName() { return this.item.getClass().getSimpleName(); }
    private void item(Operand item) { if (hasOne(item)) this.item = item.inside(this); }
    private LiteralName variable() { return (LiteralName)this.item; }
    public LiteralSymbol asSymbol() { return (LiteralSymbol)this.item; }
    public LiteralString asString() { return (LiteralString)this.item; }
    public Global asGlobal() { return (Global)this.item; }
    private Nest nest() { return (Nest)this.item; }

    public Block block() { return isBlock() ? nest().nestedBlock() : null; }
    public Expression expression() { return isExpression() ? (Expression)this.item : null; }

    @Override public String name() { return isName() ? this.item.name() : Empty; }
    @Override public TypeName typeResolver() { return this.item.typeResolver(); }

    @Override public boolean isBlock() { return this.item instanceof Nest; }
    @Override public boolean containsExit() { return this.item.exitsMethod(); }
    @Override public boolean producesPredicate() { return this.item.producesPredicate(); }
    @Override public boolean hasOnlyValue() { return !isExpression() && !isBlock(); }
    @Override public boolean selfIsPrimary() { return this.item.selfIsPrimary(); }

    public boolean isLiteralName() { return this.item instanceof LiteralName; }
    public boolean isGlobal() { return this.item instanceof Global; }
    public boolean isName() { return isLiteralName() || isGlobal(); }
    public boolean isSelf() { return isLiteralName() && variable().referencesSelf(); }
    public boolean isSuper() { return isLiteralName() && variable().referencesSuper(); }
    public boolean isExpression() { return this.item instanceof Expression; }
    public boolean isBlock(Block scope) { return isBlock() && nest().nestedBlock().matches(scope); }

    @Override public Emission emitPrimitive() { return this.item.emitPrimitive(); }
    @Override public Emission emitOperand() { return this.item.emitOperand(); }

} // Primary
