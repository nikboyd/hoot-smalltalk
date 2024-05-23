package Hoot.Compiler.Scopes;

import Hoot.Runtime.Behaviors.Scope;
import Hoot.Runtime.Emissions.NamedItem;
import Hoot.Runtime.Notes.DetailedType;
import Hoot.Runtime.Notes.TypeList;

/**
 * Describes the heritage of a type.
 * 
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2024 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class TypeHeritage extends NamedItem implements ScopeSource {

    Face faceScope;
    @Override public Face face() { return this.faceScope; }
    public Face face(Face f) { this.faceScope = f; return f; }
    public TypeHeritage() { super(Scope.currentFace()); }
    @Override public File file() { return face().file(); }

    public String subtypeName() { return subType().typeName().name(); }
    public TypeList details() { return subType().details(); }
    public DetailedType subType() { return this.subtype; }
    DetailedType subtype;

    @Override public boolean hasNoHeritage() { return heritage().isEmpty(); }
    public TypeList heritage() { return this.heritage; }
    TypeList heritage;

    public String subtypeKeyword() { return this.keyword; }
    String keyword;

    @Override public String comment() { return this.comment; }
    String comment;

} // TypeHeritage
