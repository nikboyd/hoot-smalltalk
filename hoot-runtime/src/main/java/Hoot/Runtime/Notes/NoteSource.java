package Hoot.Runtime.Notes;

import java.util.*;
import Hoot.Runtime.Emissions.Emission;
import Hoot.Runtime.Emissions.EmissionSource;
import static Hoot.Runtime.Emissions.Emission.emit;

/**
 * A source of decor and note emissions.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2025 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public interface NoteSource extends EmissionSource {

    default Emission emitExtendedType(Emission type, Emission base) {
        return emit("ExtendedType").type(type).base(base); }

    default Emission emitDetailedType(Emission type, Emission details) {
        return emit("DetailedType").type(type).with("details", details); }

    default Emission emitNote(String name) { return emit("Note").name(name); }
    default Emission emitDetails(List<Emission> details) { return emit("Details").values(details); }

} // NoteSource
