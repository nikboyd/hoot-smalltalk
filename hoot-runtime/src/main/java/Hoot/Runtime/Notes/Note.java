package Hoot.Runtime.Notes;

import Hoot.Runtime.Emissions.Emission;
import static Hoot.Runtime.Names.Keyword.*;
import static Hoot.Runtime.Emissions.Emission.*;

/**
 * A notation.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2025 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class Note implements NoteSource {

    protected Note(String name) { this.name = name; }
    public static Note with(String name) { return new Note(name); }

    protected String name = Empty;
    public String name() { return this.name; }
    public String decor() { return name().toLowerCase(); }

    public boolean isStacked() { return Stacked.equals(name()); }
    public boolean isAccess() { return Decor.AccessNotes.contains(name()); }
    public boolean isStatic() { return Static.equals(name()); }
    public boolean isProperty() { return Property.equals(name()); }
    public boolean isPublic() { return Public.equals(name()); }
    public boolean isTransient() { return Transient.equals(name()); }
    public boolean isProtected() { return Protected.equals(name()); }
    public boolean isPrivate() { return Private.equals(name()); }
    public boolean isNotice() { return Notice.equals(name()); }
    public boolean isPrimitive() { return Primitive.equals(name()); }
    public boolean isAbstract() { return Abstract.equals(name()); }
    public boolean isNative() { return Native.equals(name()); }
    public boolean isVoid() { return Void.equals(name()); }

    public boolean isTypeDecorOnly() { return !Decor.hootNote(name()); }
    public boolean isClassDecorOnly() { return Decor.classNote(name()) && !Decor.hootNote(name()); }
    public boolean isClassNoteOnly() { return !Decor.classNote(name()) && !Decor.hootNote(name()); }
    public boolean isMethodDecorOnly() { return Decor.methodNote(name()) && !Decor.hootNote(name()); }
    public boolean isMethodNoteOnly() { return !Decor.methodNote(name()) && !Decor.hootNote(name()); }
    public boolean isVariableDecorOnly() { return Decor.variableNote(name()) && !Decor.hootNote(name()); }
    public boolean isArgumentDecorOnly() { return Decor.argumentNote(name()) && !Decor.hootNote(name()); }

    public Emission emitDecor() { return emitItem(decor()); }
    public Emission emitNote() { return emitNote(name()); }
    @Override public Emission emitItem() { return emitNote().values(NoValue); }

    public String notice() { return Empty; } // override this!

    public static final Note StaticNote = Note.with(Static);
    public static final Note TransientNote = Note.with(Transient);
    public static final Note ProtectedNote = Note.with(Protected);
    public static final Note OverrideNote = Note.with(Override);
    public static final Note DefaultNote = Note.with(Default);

    /**
     * A note list source.
     */
    public static interface ListSource {

        public NoteList notes();

    } // ListSource

} // Note
