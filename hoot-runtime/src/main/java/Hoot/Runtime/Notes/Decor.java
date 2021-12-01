package Hoot.Runtime.Notes;

import java.util.*;
import static Hoot.Runtime.Functions.Utils.*;

/**
 * Hoot decor.
 *
 * <h4>Decor responsibilities:</h4>
 * <ul>
 * <li>knows the standard Hoot decorations</li>
 * <li>detects the standard Hoot decorations</li>
 * <li>translates Hoot decorations into their Java equivalents</li>
 * </ul>
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 * @see <a href="https://gitlab.com/hoot-smalltalk/hoot-smalltalk/tree/master/LICENSE.txt">LICENSE for more details</a>
 */
public class Decor {

    public Decor(String... notes) { this(wrap(notes)); }
    private Decor(List<String> notes) { notes.forEach(n -> makeNote(n)); }
    public static Decor with(List<String> notes) { return new Decor(notes); }
    public static Decor with(String... notes) { return new Decor(notes); }

    private final List<String> notes = emptyList(String.class);
    public List<String> notes() { return copyList(this.notes); }
    public boolean isEmpty() { return this.notes.isEmpty(); }

    // Hoot decorators
    public static final String Notice = "Notice";
    public boolean hasNotice() { return hasNote(Notice); }

    public static final String Property = "Property";
    public boolean hasProperty() { return hasNote(Property); }

    public static final String Primitive = "Primitive";
    public boolean hasPrimitive() { return hasNote(Primitive); }

    public static final String Generic = "Generic";
    public boolean hasGeneric() { return hasNote(Generic); }

    // Java decorators
    public static final String Final = "Final";
    public static final String Native = "Native";
    public static final String Private = "Private";
    public static final String Abstract = "Abstract";
    public static final String Transient = "Transient";
    public static final String Synchronized = "Synchronized";

    public static final String Protected = "Protected";
    public boolean hasProtected() { return hasNote(Protected); }

    public static final String Public = "Public";
    public boolean hasPublic() { return hasNote(Public); }

    public static final String Static = "Static";
    public boolean hasStatic() { return hasNote(Static); }

    public static final String Default = "Default";
    public boolean hasDefault() { return hasNote(Default); }

    public static final String DefaultMethodAccess = Public;
    public static final String DefaultVariableAccess = Protected;
    private static final String[] MethodAccess = { Public, Protected, Private };
    public static final List<String> AccessNotes = wrap(MethodAccess);
    public boolean hasAccess() { return hasNoteType(AccessNotes); }
    public boolean hasNote(String note) { return matchAny(this.notes, n -> n.equals(note)); }
    public boolean hasNoteType(final List<String> notes) { return matchAny(this.notes, n -> notes.contains(n)); }

    public void makeStatic() { makeNote(Static); }
    public void makeProtected() { if (!hasAccess()) this.notes.add(Protected); }
    public void makePublic() { if (!hasAccess()) this.notes.add(Public); }
    public void makeNote(List<String> notes) { notes.forEach(n -> makeNote(n)); }
    public void makeNote(String note) { if (!hasNote(note)) this.notes.add(note); }
    public List<String> notesWithout(String... strays) { return notesWithout(wrap(strays)); }
    public List<String> notesWithout(List<String> strays) { return select(this.notes, n -> !strays.contains(n)); }

    private static final String[] NotesFilters = { Notice, Property, Primitive };
    public static final List<String> HootNotes = wrap(NotesFilters);
    public static boolean hootNote(String note) { return HootNotes.contains(note); }

    private static final String[] ClassTypes = { Abstract, Final };
    public static final List<String> ClassNotes = wrap(ClassTypes);
    public static boolean classNote(String note) { return ClassNotes.contains(note); }

    private static final String[] MethodTypes = {
        Public, Protected, Private, Abstract, Default, Native, Static, Final, Synchronized };
    public static final List<String> MethodNotes = wrap(MethodTypes);
    public static boolean methodNote(String note) { return MethodNotes.contains(note); }

    private static final String[] VariableTypes = { Public, Protected, Private, Static, Final };
    public static final List<String> VariableNotes = wrap(VariableTypes);
    public static boolean variableNote(String note) { return VariableNotes.contains(note); }

    private static final String[] ArgumentTypes = { Final };
    public static final List<String> ArgumentNotes = wrap(ArgumentTypes);
    public static boolean argumentNote(String note) { return ArgumentNotes.contains(note); }

    private static final String[] AbstractedBlocks = { Abstract, Native };
    public static final List<String> AbstractNotes = wrap(AbstractedBlocks);
    public boolean isAbstracted() { return hasNoteType(AbstractNotes); }

    /**
     * Contains decor, and provides access.
     */
    public static interface DecorSource {

        public Decor decor();

    } // DecorSource

} // Decor
