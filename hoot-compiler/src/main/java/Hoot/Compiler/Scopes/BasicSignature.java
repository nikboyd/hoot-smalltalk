package Hoot.Compiler.Scopes;

import java.util.*;
import Hoot.Runtime.Notes.NoteList;
import Hoot.Runtime.Behaviors.Scope;
import Hoot.Runtime.Values.*;
import Hoot.Runtime.Notes.DetailedType;
import static Hoot.Runtime.Functions.Utils.*;
import static Hoot.Runtime.Names.Operator.*;
import static Hoot.Runtime.Emissions.Emission.*;
import Hoot.Runtime.Emissions.*;

/**
 * A basic method signature.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public abstract class BasicSignature extends NamedItem implements ScopeSource {

    public BasicSignature() { super(Scope.current()); }
    protected Table args = new Table(Scope.current());
    public Table args() { return this.args; }
    public List<Variable> arguments() { return args().definedSymbols(); }
    @Override public void clean() { super.clean(); args().clean(); }
    public boolean hasLocal(String symbolName) { return args().containsSymbol(symbolName); }
    public Variable localNamed(String symbolName) { return args().symbolNamed(symbolName); }

    protected DetailedType resultType;
    public DetailedType resultType() { return this.resultType; }
    public boolean needsDefaultType() { return hasNo(resultType()); }
    public boolean hasResultType() { return !needsDefaultType(); }
    public boolean returnsVoid() {
        return needsDefaultType() ? false : resultType().typeName().isVoid(); }

    @Override public String description() {
        return method().isStatic() ?
                face().typeName() + AssociateStatic + name() :
                face().typeName() + Associate + name();
    }

    @Override public NoteList notes() { return method().notes(); }
    public BasicSignature eraseTypes() { return this; }
    public Emission emitDecorators() { return emitItems(notes().methodNotesOnlyDecor()); }
    public boolean hasResult() { return !(method().isConstructor() && !returnsVoid()); }

    public String defaultTypeName() { return face().defaultName(); }
    public DetailedType defaultType() { return DetailedType.named(defaultTypeName()); }
    public List<Emission> emitArguments() { return map(arguments(), a -> a.emitArgument()); }

    static final String Details = "Details";
    public Emission emitDetails() { return emitItems(emit(Details).values(emitGenericDetails())); }

    public DetailedType resolvedType() {
        if (method().isConstructor()) return defaultType();
        return needsDefaultType() ?  defaultType() : resultType(); }

    public Emission emitResultType() {
        if (method().isConstructor()) return null;
        return needsDefaultType() ?
                emitItem(defaultTypeName()) : // default type
                resultType().emitCode(false); // specified type
    }

    protected HashMap<String, Emission> knownTypes = emptyMap(Emission.class);
    public void mergeDetails(Map<String, Emission> map) {
        map.keySet().forEach(key -> {
            knownTypes.putIfAbsent(key, map.get(key));
        });
    }

    public void mergeDetails() {
        mergeDetails(resultType());
//        for (Variable v : arguments()) {
//            if (v.hasTypeNote()) mergeDetails(v.notedType());
//        }
        selectList(arguments(),
            v -> v.hasTypeNote()).forEach(
                v -> mergeDetails(v.notedType()));
    }

    public void mergeDetails(DetailedType type) {
        if (type == null) return;
        boolean wantsBases = true;
        mergeDetails(type.extractGenerics(wantsBases));
    }

    private List<Emission> emitGenericDetails() {
        HashSet<String> types = copySet(knownTypes.keySet());
        if (!method().isStatic() && !face().isMetaface()) {
            types.removeAll(facialScope().knownTypes());
        }

        List<Emission> results = emptyList(Emission.class);
        types.forEach(typeName -> results.add(knownTypes.get(typeName)));
        return results;
    }

    @Override public Emission emitItem() { return emitMethodSignature(
        name(), emitDecorators(), emitDetails(), emitResultType(), emitArguments()); }

} // BasicSignature
