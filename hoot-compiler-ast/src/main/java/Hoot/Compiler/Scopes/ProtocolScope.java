package Hoot.Compiler.Scopes;

import java.util.*;

import Hoot.Runtime.Emissions.Emission;
import static Hoot.Runtime.Emissions.Emission.emit;
import Hoot.Runtime.Emissions.Item;

/**
 * A class or type member protocol scope.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2025 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class ProtocolScope extends Item {

    public ProtocolScope(String n, String s) { this.className = n; this.selector = s; }
    public static ProtocolScope with(String className, String selector) { return new ProtocolScope(className, selector); }

    public boolean metaScope() { return !mainScope(); }
    public boolean mainScope() { return selector.isEmpty(); }
    public String selector() { return this.selector; }
    String selector = "";

    public String className() { return this.className; }
    String className = "";
    
    static final String ProtoForm = "%s %s members";
    public void reportScope() {
        report(String.format(ProtoForm, className(), mainScope()? "main": selector()));
    }

//    public static ProtocolScope with(ProtocolSignatureContext context) {
//        return new ProtocolScope().context(context);
//    }
//
//    public ProtocolSignatureContext context() {
//        return contextAs(ProtocolSignatureContext.class);
//    }

//    public List<ClassMemberContext> members() {
//        return accessList(context(), c -> c.members);
//    }

//    public List<Variable> variables() {
//        return filterList(Variable.class, members(), m -> m.v.item);
//    }

//    public List<MethodScope> methods() {
//        return filterList(MethodScope.class, members(), m -> m.item);
//    }

//    public String selector() {
//        return accessText(context(), c -> c == null ? Empty : c.selector);
//    }
//
//    public String comment() {
//        return Comment.findComment(context().g.g, context().k.stop);
//    }

//    @Override
//    public boolean isEmpty() {
//        if (super.isEmpty()) return true;
//        return isListEmpty(context(), c -> c.members);
//    }

//    public boolean isMetaScope() {
//        return ClassSelector.equals(selector());
//    }

//    public static List<Emission> memberScopesCode(List<ProtocolScopeContext> scopes) {
//        return scopes.stream()
//                .map(s -> (ProtocolScope) s.item)
//                .filter(s -> !s.isMetaScope())
//                .map(s -> s.emitItem())
//                .collect(Collectors.toList());
//    }
//
//    public static List<Emission> metaScopesCode(List<ProtocolScopeContext> scopes) {
//        return scopes.stream()
//                .map(s -> (ProtocolScope) s.item)
//                .filter(s -> s.isMetaScope())
//                .map(s -> s.emitItem())
//                .collect(Collectors.toList());
//    }

    public List<Emission> classMembersCode() {
        ArrayList<Emission> results = new ArrayList<>();
        if (isEmpty()) return results;

//        for (Variable v : variables()) {
//            results.add(v.emitItem());
//        }

//        for (MethodScope m : methods()) {
//            results.add(m.emitCode());
//        }

        return results;
    }

    @Override
    public Emission emitItem() {
        return emit("Sections").with("sections", classMembersCode());
    }

} // ProtocolScope
