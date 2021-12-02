package Hoot.Runtime.Exceptions;

import Hoot.Runtime.Behaviors.Typified;
import Hoot.Runtime.Blocks.Enclosure;
import Hoot.Runtime.Faces.Valued;

/**
 * Defines protocols for handled exceptions.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public interface HandledException extends Valued {

    Enclosure currentHandler();
    Enclosure currentHandler(Enclosure c);
    default boolean hasCurrentHandler() { return null != currentHandler(); }

    default Valued defaultAction() { UnhandledException.type().raise(this); return this; }
    default Enclosure defaultContinuation() { return Enclosure.defaultContinuation(); }

    public static interface Metatype extends Typified {}

} // HandledException
