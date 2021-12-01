package Hoot.Compiler.Notes;

import java.util.*;
import Hoot.Runtime.Notes.Note;
import Hoot.Runtime.Emissions.Emission;
import static Hoot.Runtime.Functions.Utils.*;
import static Hoot.Runtime.Names.Primitive.*;
import Hoot.Compiler.Constants.NamedValue;

/**
 * A keyword note.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 * @see <a href="https://gitlab.com/hoot-smalltalk/hoot-smalltalk/tree/master/LICENSE.txt">LICENSE for more details</a>
 */
public class KeywordNote extends Note {

    protected KeywordNote(String name) { super(name); }
    public static KeywordNote with(String name, List<NamedValue> nakeds, List<NamedValue> values) {
        return new KeywordNote(name).nakeds(nakeds).values(values); }

    List<NamedValue> nakeds = emptyList(NamedValue.class);
    public List<NamedValue> nakedValues() { return this.nakeds; }
    public KeywordNote nakeds(List<NamedValue> nakeds) { this.nakeds.addAll(nakeds); return this; }
    public int nakedCount() { return this.nakeds.size(); }

    List<NamedValue> values = emptyList(NamedValue.class);
    public List<NamedValue> namedValues() { return this.values; }
    public KeywordNote values(List<NamedValue> values) { this.values.addAll(values); return this; }
    public int namedCount() { return this.values.size(); }

    @Override public Emission emitItem() {
        if (nakedCount() > 0) return emitNote().values(emitNakedValues());
        if (namedCount() > 0) return emitNote().values(emitNamedValues());
        return emitNote();
    }

    public Emission emitNamedValues() { return emitList(map(namedValues(), v -> v.emitItem())); }
    public Emission emitNakedValues() { return emitList(map(nakedValues(), v -> v.emitItem())); }

    @Override public String notice() {
        return (!this.isNotice() || nakedCount() == 0) ? Empty :
                trimQuotes(SingleQuote, nakedValues().get(0).scalarValue().rawValue()); }

} // KeywordNote
