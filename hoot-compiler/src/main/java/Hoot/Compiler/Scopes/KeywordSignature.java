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
 * @see <a href="https://gitlab.com/hoot-smalltalk/hoot-smalltalk/tree/master/LICENSE.txt">LICENSE for more details</a>
 */
public class KeywordSignature extends BasicSignature {

    public KeywordSignature() { super(); }
    public static KeywordSignature emptyNiladic() { return with(null, emptyList(Variable.class)); }
    public static KeywordSignature with(DetailedType resultType, List<Variable> args) {
        List<String> tails = emptyList(String.class);
        for (Variable arg : args) tails.add(Colon);
        return with(resultType, args, emptyList(String.class), tails); }

    public static KeywordSignature with(
            DetailedType resultType, List<Variable> args, List<String> heads, List<String> tails) {
        KeywordSignature result = new KeywordSignature();
        result.resultType = resultType;
        result.args.withAll(args);
        result.heads.addAll(heads);
        result.tails.addAll(tails);
        return result;
    }

    List<String> heads = emptyList(String.class);
    List<String> tails = emptyList(String.class);
    @Override public String name() { return Keyword.with(heads, tails).methodName(); }
    @Override public BasicSignature eraseTypes() {
        return KeywordSignature.with(DetailedType.RootType,
                map(arguments(), arg -> arg.withErasure()), heads, tails); }

} // KeywordSignature
