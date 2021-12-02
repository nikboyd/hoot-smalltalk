package Hoot.Runtime.Emissions;

import java.util.*;
import org.apache.commons.lang3.StringUtils;

import Hoot.Runtime.Faces.Named;
import Hoot.Runtime.Behaviors.Typified;
import static Hoot.Runtime.Functions.Utils.*;
import static Hoot.Runtime.Names.Primitive.SerializedTypes;

/**
 * A named item.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public abstract class NamedItem extends Item implements Named {

    public NamedItem(Item container) { super(container); }
    public NamedItem(NamedItem container) { super(container); }
    @Override public String name() { return getClass().getSimpleName(); }

    public NamedItem metaSignature() { return null; }
    public boolean isInterface() { return false; }

    public String comment() { return Empty; }
    public String baseName() { return Empty; }

    public boolean hasHeritage() { return !hasNoHeritage(); }
    public boolean hasNoHeritage() { return simpleHeritage().isEmpty() && typeHeritage().isEmpty(); }

    public List<Typified> simpleHeritage() { return emptyList(Typified.class); }
    public List<Typified> typeHeritage() { return emptyList(Typified.class); }

    static final String MissingReport = "%s search from %s can't find %s";
    void warnMissing(String search, String name) { warn(format(MissingReport, search, fullName(), name)); }

    public boolean reportWhetherKnown(String search, Typified type, String name) {
        if (StringUtils.isEmpty(name) || hasNo(type)) {
//            warnMissing(search, name);
            return false;
        }

        if (SerializedTypes.contains(name)) return true;
        if (fullName().startsWith("Samples.")) return true;
        if (fullName().equals("Hoot.Magnitudes.Character")) return true;
        return true; }

} // NamedItem
