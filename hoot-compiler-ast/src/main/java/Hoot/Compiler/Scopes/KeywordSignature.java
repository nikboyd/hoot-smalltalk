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
 * @see "Copyright 2010,2025 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class KeywordSignature extends BasicSignature {

    public KeywordSignature() { super(); this.keyword = Keyword.with(); }
    public static KeywordSignature emptyNiladic() {
        return with(DetailedType.named(Keyword.Void), emptyList(Variable.class)); }

    public static KeywordSignature with(DetailedType resultType, List<Variable> args) {
        List<Variable> allArgs = hasSome(args)? args: emptyList(Variable.class);
        List<String> tails = emptyList(String.class); for (Variable arg : allArgs) tails.add(Colon);
        return with(resultType, allArgs, emptyList(String.class), tails); }


    public static KeywordSignature with(
            DetailedType resultType, List<Variable> args, Keyword keyword) {
        KeywordSignature result = new KeywordSignature();
        result.keyword = keyword;
        result.resultType = resultType;
        result.args.withAll(hasSome(args)? args: emptyList(Variable.class));
        return result;
    }

    public static KeywordSignature with(
            DetailedType resultType, List<Variable> args, List<String> heads, List<String> tails) {
        return with(resultType, args, Keyword.with(heads, tails));
    }

    Keyword keyword;
    @Override public String name() { return this.keyword.methodName(); }
    @Override public BasicSignature eraseTypes() {
        return KeywordSignature.with(DetailedType.RootType,
                map(arguments(), arg -> arg.withErasure()), keyword); }

} // KeywordSignature
