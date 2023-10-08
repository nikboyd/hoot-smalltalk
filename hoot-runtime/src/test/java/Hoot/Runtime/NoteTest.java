package Hoot.Runtime;

import org.junit.*;
import static org.junit.Assert.*;
import Hoot.Runtime.Notes.Note;
import Hoot.Runtime.Notes.NoteList;
import static Hoot.Runtime.Names.Keyword.*;

/**
 * Confirms proper operation of notes.
 * @author Nik Boyd <nik.boyd@educery.dev>
 */
public class NoteTest {

    @Test public void noteTest() {
        NoteList list = NoteList.with(Note.StaticNote, Note.ProtectedNote);
        assertFalse(list.names().isEmpty());
        assertFalse(list.notes().isEmpty());
        assertFalse(list.isAbstract());
        assertFalse(list.isAbstractive());
        assertFalse(list.isPublic());
        assertFalse(list.isPrimitive());
        assertFalse(list.isProperty());
        assertFalse(list.isTransient());
        assertFalse(list.isStacked());
        assertFalse(list.isVoid());
        assertTrue(list.isProtected());
        assertTrue(list.isStatic());
        list.classNotesWithoutDecor();
        list.classNotesOnlyDecor();
        list.argumentNotesOnlyDecor();
        list.assignedNotesOnlyDecor();
        list.methodNotesOnlyDecor();
        list.methodNotesWithoutDecor();

        assertTrue(Note.StaticNote.isStatic());
        assertTrue(Note.TransientNote.isTransient());
        assertTrue(Note.ProtectedNote.isProtected());
        assertTrue(Note.ProtectedNote.isMethodDecorOnly());
        assertTrue(Note.with(Abstract).isAbstract());
        assertTrue(Note.with(Public).isPublic());
        assertTrue(Note.with(Private).isPrivate());
        assertTrue(Note.with(Native).isNative());
        assertTrue(Note.with(Void).isVoid());
        assertTrue(Note.with(Notice).isNotice());
        assertTrue(Note.with(Primitive).isPrimitive());
        assertTrue(Note.with(Property).isProperty());
    }

} // NoteTest
