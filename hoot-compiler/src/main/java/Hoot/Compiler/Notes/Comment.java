package Hoot.Compiler.Notes;

import java.util.*;
import org.antlr.v4.runtime.*;

import Hoot.Runtime.Emissions.*;
import Hoot.Compiler.Scopes.File;
import static Hoot.Runtime.Functions.Utils.*;
import static Hoot.Compiler.HootParser.CodeComment;

/**
 * Discovers a code comment in proximity to another token.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class Comment extends Item {

    public Comment() { super(null); }
    public Comment(Token anchor) { this(); this.anchor = anchor; }
    public static Comment with(Token anchor) { return new Comment(anchor); }

    private Token anchor;
    public int getTokenIndex() { return anchor.getTokenIndex(); }
    @Override public Emission emitItem() { return emitEmpty(); }

    private static Comment getComment(int index) { return File.currentFile().getComment(index); }
    public static List<Comment> buildComments(CommonTokenStream tokens) {
        return mapList(findCommentTokens(tokens), c -> c != null, c -> Comment.with(c)); }

    private static List<Token> findCommentTokens(CommonTokenStream tokens) {
        return tokens.getTokens(0, tokens.size() - 1, CodeComment); }

    static final String Line = "\n";
    static final String LineMod = Line + Blank + Wild + Blank;
    public String getComment() { return anchor.getText().replace(Quote, Blank).trim().replace(Line, LineMod); }

    public static String findComment(Token lowerBound, Token upperBound) {
        int index = 0;
        Comment result = getComment(index);
        if (result == null) return Empty;

        String comment = result.getComment();
        while (result.getTokenIndex() < lowerBound.getTokenIndex()) {
            index++;
            result = getComment(index);
            if (result == null) return Empty;

            comment = result.getComment();
            if (!comment.isEmpty()) return comment;
        }

        if (upperBound == null || result.getTokenIndex() < upperBound.getTokenIndex()) {
            comment = result.getComment();
        }

        return comment;
    }

} // Comment
