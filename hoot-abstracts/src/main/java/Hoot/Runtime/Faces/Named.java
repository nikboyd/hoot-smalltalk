package Hoot.Runtime.Faces;

import java.util.*;
import org.apache.commons.lang3.StringUtils;

import Hoot.Runtime.Emissions.EmissionSource;
import static Hoot.Runtime.Names.Keyword.Self;
import static Hoot.Runtime.Names.Operator.Dot;
import static Hoot.Runtime.Functions.Utils.*;
import static Hoot.Runtime.Faces.Logging.*;

/**
 * A named component.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2025 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public interface Named extends Valued {

    default CharSequence name() { return EmissionSource.Empty; } // implementors should override
    default String shortName() { return name().toString(); } // implementors should override
    default String fullName() { return name().toString(); } // implementors should override

    default int nestLevel() { return 0; }
    default boolean isSelfish() { return Self.equals(shortName()); }
    default boolean isTransient() { return false; }
    default void makeTransient() { } // override this when overriding isTransient

    default String qualify(Named component) { return qualify(component.shortName()); }
    default String qualify(String componentName) {
        return (isEmpty(componentName)) ? Empty : fullName() + Dot + componentName.trim(); }

} // Named
