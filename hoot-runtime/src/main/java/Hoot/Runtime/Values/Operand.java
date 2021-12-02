package Hoot.Runtime.Values;

import Hoot.Runtime.Faces.Named;
import Hoot.Runtime.Faces.Selector;
import Hoot.Runtime.Faces.Resulting;
import Hoot.Runtime.Names.TypeName;
import Hoot.Runtime.Emissions.Item;
import Hoot.Runtime.Emissions.Emission;
import static Hoot.Runtime.Behaviors.HootRegistry.*;
import Hoot.Runtime.Emissions.NamedItem;

/**
 * An operand. Many methods get overridden by subclasses.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public abstract class Operand extends NamedItem implements Resulting {

    protected Operand(Item container) { super(container); }
    protected Operand(NamedItem container) { super(container); }
    public Operand cleanTerm() { this.clean(); return this; }
    public Operand makeAssigned() { return this; }

    protected boolean isResult = false;
    public Operand makeResult() { this.isResult = true; return this; }
    @Override public boolean isResult() { return this.isResult; }

    public boolean hasCascades() { return false; }
    public boolean hasOnlyValue() { return false; }

    @Override public boolean isFramed() { return false; }
    public boolean isLiteral() { return false; }
    public boolean isConstruct() { return false; }
    public boolean isMessage() { return false; }
    public boolean isNest() { return false; }
//    public boolean isReference() { return false; }

    public boolean producesPredicate() { return false; }
    public boolean throwsException() { return false; }
    public boolean needsStatement() { return true; }
    public boolean refersToMetaclass() { return false; }
    public boolean receiverNeedsTerm() { return false; }
    public boolean selfIsPrimary() { return false; }

    public Named asReference() { return (Named)this; }
    public TypeName typeResolver() { return RootType(); }
    public Class resolvedType() { return RootClass(); }
    public String resolvedTypeName() { return typeResolver().fullName(); }
    public boolean resolvesToPrimitive() { return typeResolver().isPrimitiveType(); }

    /**
     * @return whether the receiver consumes an (operand)
     * @param operand a potentially consumable operand
     */
    public boolean consumes(Operand operand) { return false; }

    /**
     * @return whether a (selector) can be optimized against this operand
     * @param selector a method selector
     */
    public boolean optimizes(Selector selector) {
        if (selector.isOptimized()) return true;
        TypeName resolver = typeResolver();
        if (RootType().matches(resolver)) return false;
        if (BehaviorType().matches(resolver)) return false;
        return true; }

    @Override public Emission emitItem() { return emitOperand(); }
    @Override public Emission emitOptimized() { return emitOperand(); }
    @Override public Emission emitPrimitive() { return emitOperand(); }

    public Emission emitTerm() { return emitTerm(emitOperand()); }
    public Emission emitResult() { return emitResult(emitItem("Object"), emitOperand()); }
    public Emission emitOperand() { return emitEmpty(); } // override this!
    public Emission emitStatement() { return emitStatement(emitOperand()); }
    public Emission emitBooleanTerm() { return emitCast(BooleanType().fullName(), emitOperand()); }
    public Emission emitReceiver() { return (receiverNeedsTerm()) ? emitTerm() : emitOperand(); }

} // Operand
