package Hoot.Compiler.Constants;

import java.util.*;
import Hoot.Runtime.Faces.Named;
import Hoot.Runtime.Emissions.*;
import Hoot.Runtime.Names.TypeName;
import Hoot.Runtime.Values.Variable;
import Hoot.Runtime.Behaviors.Typified;
import static Hoot.Runtime.Names.Keyword.*;
import static Hoot.Runtime.Functions.Utils.*;
import static Hoot.Runtime.Names.TypeName.EmptyType;

/**
 * A reference to self, super, or some scoped variable.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 * @see <a href="https://gitlab.com/hoot-smalltalk/hoot-smalltalk/tree/master/LICENSE.txt">LICENSE for more details</a>
 */
public class LiteralName extends Constant implements Named {

    public LiteralName() { super(); }
    public LiteralName(String name) { super(); this.name = name; }
    public static LiteralName with(String name, int line) { return new LiteralName(name).withLine(line); }
    public static LiteralName with(String name) { return new LiteralName(name); }
    public static LiteralName literalSelf() { return with(Self); }

    protected String name = Empty;
    @Override public String name() { return this.name; }
    @Override public boolean selfIsPrimary() { return referencesSelf(); }
    public boolean referencesSelf() { return name().equals(Self); }
    public boolean referencesSuper() { return name().equals(Super); }
    public boolean referencesLocal() { return matchAny(blockScopes(), s -> s.hasLocal(name())); }
    public boolean referencesMember() { return matchAny(faceHeritage(), f -> f.resolves(this)); }

    public List<Typified> faceHeritage() {
        List<Typified> results = emptyList(Typified.class);
        results.add((Typified)facialScope());
        results.addAll(facialScope().simpleHeritage());
        return results; }

    public Variable referencedLocal() {
        return nullOr(s -> s.localNamed(name()), findFirst(blockScopes(), bs -> bs.hasLocal(name()))); }

    public Variable referencedMember() {
        return nullOr(s -> s.localNamed(name()), findFirst(faceHeritage(), fs -> fs.resolves(this))); }

    @Override public TypeName typeResolver() {
        if (referencesLocal()) return referencedLocal().typeResolver();
        if (referencesMember()) return referencedMember().typeResolver();
        return EmptyType;
    }

    private Emission emitNamedValue(Variable v) { return emitNamedValue(name(), v.emitType(false)); }

    @Override public Emission emitOperand() {
        if (referencesSelf()) return emitSelf();
        if (referencesSuper()) return emitSuper();

        if (containerScope().isFacial()) {
            return emitItem(name());
        }

        if (referencesLocal()) {
            Variable v = referencedLocal();
            if (v.isStacked()) return emitNamedValue(v);
        }

        return emitItem(name());
    }

} // LiteralName
