package Hoot.Runtime.Notes;

import java.util.*;
import java.util.function.Predicate;

import Hoot.Runtime.Emissions.Emission;
import Hoot.Runtime.Emissions.EmissionSource;
import static Hoot.Runtime.Functions.Utils.*;
import static Hoot.Runtime.Names.Keyword.*;
import static Hoot.Runtime.Notes.Note.*;

/**
 * A notation list.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 * @see <a href="https://gitlab.com/hoot-smalltalk/hoot-smalltalk/tree/master/LICENSE.txt">LICENSE for more details</a>
 */
public class NoteList implements EmissionSource {

    public static NoteList with(List<Note> notes) { return new NoteList().noteAll(notes); }

    private final List<Note> notes = emptyList(Note.class);
    public List<Note> notes() { return this.notes; }
    public NoteList noteAll(List<Note> notes) { notes.forEach(n -> note(n)); return this; }
    public void note(Note note) { if (!anyMatch(n -> n.name().equals(note.name()))) notes().add(note); }
    boolean anyMatch(Predicate<? super Note> predicate) { return !isEmpty() && matchAny(notes(), predicate); }
    public List<String> names() { return map(notes(), note -> note.name()); }

    public void makeStatic() { note(StaticNote); }
    public void makeTransient() { note(TransientNote); }
    public void makeProtectedIfNeeded() { if (!hasAccess()) note(ProtectedNote); }

    public boolean isEmpty() { return notes().isEmpty(); }
    public boolean isVoid() { return anyMatch(note -> note.isVoid()); }
    public boolean isStatic() { return anyMatch(note -> note.isStatic()); }
    public boolean isStacked() { return anyMatch(note -> note.isStacked()); }
    public boolean isTransient() { return anyMatch(note -> note.isTransient()); }
    public boolean isPrimitive() { return anyMatch(note -> note.isPrimitive()); }
    public boolean isAbstract() { return anyMatch(note -> note.isAbstract()); }
    public boolean isAbstractive() { return anyMatch(note -> note.isAbstract() || note.isNative()); }
    public boolean isPublic() { return anyMatch(note -> note.isPublic()); }
    public boolean isProtected() { return anyMatch(note -> note.isProtected()); }
    public boolean isProperty() { return anyMatch(note -> note.isProperty()); }
    public boolean hasAccess() { return anyMatch(note -> note.isAccess()); }

    public List<Note> selectNotes(Predicate<Note> predicate) { return select(notes(), predicate); }
    public String notice() {
        List<Note> results = selectNotes(n -> n.isNotice());
        return results.isEmpty() ? Empty : results.get(0).notice();
    }

    public List<String> typeDecor() { return selectDecor(Public, n -> n.isTypeDecorOnly()); }
    public List<String> classDecor() { return selectDecor(Public, n -> n.isClassDecorOnly()); }
    public List<String> selectDecor(String defaultDecor, Predicate<Note> predicate) {
        List<String> results = mapList(notes(), predicate, n -> n.decor());
        if (!defaultDecor.isEmpty() && !this.hasAccess()) {
            results.add(0, defaultDecor.toLowerCase());
        }
        return results;
    }

    public List<Emission> metaNotesOnlyDecor() { return emitOnlyDecor(Static, note -> note.isClassDecorOnly()); }
    public List<Emission> classNotesOnlyDecor() { return emitOnlyDecor(Empty, note -> note.isClassDecorOnly()); }
    public List<Emission> classNotesWithoutDecor() { return emitOnlyNotes(note -> note.isClassNoteOnly()); }
    public List<Emission> assignedNotesOnlyDecor() { return emitOnlyDecor(Empty, note -> note.isVariableDecorOnly()); }
    public List<Emission> variableNotesOnlyDecor() { return emitOnlyDecor(Protected, note -> note.isVariableDecorOnly()); }
    public List<Emission> argumentNotesOnlyDecor() { return emitOnlyDecor(Empty, note -> note.isArgumentDecorOnly()); }
    public List<Emission> methodNotesWithoutDecor() { return emitOnlyNotes(note -> note.isMethodNoteOnly()); }
    public List<Emission> methodNotesOnlyDecor() { return emitOnlyDecor(Public, note -> note.isMethodDecorOnly()); }

    public Emission emitLines() { return emitLines(emitCodeList()); }
    @Override public Emission emitItem() { return emitSequence(emitCodeList()); }

    public List<Emission> emitOnlyNotes(Predicate<Note> predicate) {
        return mapList(notes(), predicate, note -> note.emitItem()); }

    public List<Emission> emitOnlyDecor(String defaultDecor, Predicate<Note> predicate) {
        return map(selectDecor(defaultDecor, predicate), d -> emitItem(d)); }

    public List<Emission> emitCodeList() {
        return mapList(notes(), note -> !Decor.hootNote(note.name()), note -> note.emitItem()); }

} // NoteList
