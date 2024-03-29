package Hoot.Compiler.Scopes;

import java.util.*;
import Hoot.Runtime.Names.Keyword;
import Hoot.Runtime.Values.Variable;
import Hoot.Runtime.Notes.DetailedType;
import static Hoot.Runtime.Functions.Utils.*;
import static Hoot.Runtime.Names.Keyword.*;

/**
 * A keyword method signature.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class KeywordSignature extends BasicSignature {

    public KeywordSignature() { super(); }
    public static KeywordSignature emptyNiladic() { return with(null, emptyList(Variable.class)); }
    public static KeywordSignature with(DetailedType resultType, List<Variable> args) {
        List<Variable> allArgs = hasSome(args)? args: emptyList(Variable.class);
        List<String> tails = emptyList(String.class); for (Variable arg : allArgs) tails.add(Colon);
        return with(resultType, allArgs, emptyList(String.class), tails); }

    public static KeywordSignature with(
            DetailedType resultType, List<Variable> args, List<String> heads, List<String> tails) {
        KeywordSignature result = new KeywordSignature();
        result.resultType = resultType;
        result.args.withAll(hasSome(args)? args: emptyList(Variable.class));
        result.heads.addAll(hasSome(heads)? heads: emptyList(String.class));
        result.tails.addAll(hasSome(tails)? tails: emptyList(String.class));
        return result;
    }

    List<String> heads = emptyList(String.class);
    List<String> tails = emptyList(String.class);
    @Override public String name() { return Keyword.with(heads, tails).methodName(); }
    @Override public BasicSignature eraseTypes() {
        return KeywordSignature.with(DetailedType.RootType,
                map(arguments(), arg -> arg.withErasure()), heads, tails); }

} // KeywordSignature
