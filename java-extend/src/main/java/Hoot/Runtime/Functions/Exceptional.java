package Hoot.Runtime.Functions;

import java.util.function.Consumer;
import static Hoot.Runtime.Functions.Utils.*;

/**
 * Common standard utility functions for executing closures and handling exceptions.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2025 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public interface Exceptional extends Hoot.Runtime.Faces.Logging {

    static Exceptional StandardReporter = new Exceptional(){};
    public static interface Handler<T extends Throwable> extends Consumer<T>, Hoot.Runtime.Faces.Logging {}
    public static Handler<Throwable> ErrorHandler = (Throwable ex) -> { StandardReporter.error(ex); };
    public static Handler<Throwable> DebugHandler = (Throwable ex) -> { StandardReporter.whisper(ex.toString()); };

    static void handleSafely(Throwable ex, Handler<Throwable> handler) { if (hasOne(handler)) handler.accept(ex); }
    static void runSafely(Runnable... r) { if (hasAny(r)) r[0].run(); }
    static final Runnable NoOp = () -> { }; // does nothing

    static <R> R defaultAfter(Throwable ex, Handler<Throwable> h, R defaultValue) { handleSafely(ex, h); return defaultValue; }
    static String emptyAfter(Throwable ex, Handler<Throwable> handler) { return defaultAfter(ex, handler, Empty); }

    static <R> R nullAfter(Runnable... r) { runSafely(r); return null; }
    static <R> R nullAfter(Throwable ex, Handler<Throwable> handler) { handleSafely(ex, handler); return null; }

    public static void runLoudly(Runner r, Handler<Throwable> h) { runLoudly(r, h, NoOp); }
    public static void runLoudly(Runner r, Runnable... finish) { runLoudly(r, ErrorHandler, finish); }
    public static void runLoudly(Runner r, Handler<Throwable> h, Runnable... finish) { Runner.runSurely(r, h, finish); }
    public static void runQuietly(Runner r, Runnable... finish) { Runner.runSurely(r, DebugHandler, finish); }

    public static <R> R nullOrTryLoudly(Result<? extends R> r, Runnable... finish) { return defaultOrTryLoudly(r, null, finish); }
    public static <R> R nullOrTryQuietly(Result<? extends R> r, Runnable... finish) { return defaultOrTryQuietly(r, null, finish); }

    public static String emptyOrTryLoudly(Result<String> r, Runnable... finish) { return defaultOrTryLoudly(r, Empty, finish); }
    public static String emptyOrTryQuietly(Result<String> r, Runnable... finish) { return defaultOrTryQuietly(r, Empty, finish); }

    public static <R> R defaultOrTryLoudly(Result<R> r, R defaultValue) { return defaultOrTryLoudly(r, defaultValue, NoOp); }
    public static <R> R defaultOrTryQuietly(Result<R> r, R defaultValue) { return defaultOrTryQuietly(r, defaultValue, NoOp); }

    public static <R> R defaultOrTryLoudly(Result<R> r, R defaultValue, Runnable... finish) {
        return Result.defaultOrTrySurely(r, ErrorHandler, defaultValue, finish); }

    public static <R> R defaultOrTryQuietly(Result<R> r, R defaultValue, Runnable... finish) {
        return Result.defaultOrTrySurely(r, DebugHandler, defaultValue, finish); }

    public static <T, R> R defaultOrTryLoudly(Argued<T, R> r, T item, R defaultValue, Runnable... finish) {
        return Argued.defaultOrTrySurely(r, item, ErrorHandler, defaultValue, finish); }

    public static <T, R> R defaultOrTryQuietly(Argued<T, R> r, T item, R defaultValue, Runnable... finish) {
        return Argued.defaultOrTrySurely(r, item, DebugHandler, defaultValue, finish); }

    public static <T, R> R nullOrTryLoudly(Argued<T, ? extends R> r, T item, Runnable... finish) {
        return defaultOrTryLoudly(r, item, null, finish); }

    public static <T, R> R nullOrTryQuietly(Argued<T, ? extends R> r, T item, Runnable... finish) {
        return defaultOrTryQuietly(r, item, null, finish); }

    public static <T> String emptyOrTryLoudly(Argued<T, String> r, T item, Runnable... finish) {
        return defaultOrTryLoudly(r, item, Empty, finish); }

    public static <T> String emptyOrTryQuietly(Argued<T, String> r, T item, Runnable... finish) {
        return defaultOrTryQuietly(r, item, Empty, finish); }


    /**
     * Runs a closure without result.
     */
    public static interface Runner {
        void run() throws Throwable; // augments standard Runnable interface

        default void runSurely(Handler<Throwable> handler, Runnable... finish) {
            try { run(); } catch (Throwable ex) { handleSafely(ex, handler); } finally { runSafely(finish); } }

        static void runSurely(Runner r, Handler<Throwable> handler, Runnable... finish) {
            if (hasOne(r)) r.runSurely(handler, finish); else runSafely(finish); }

    } // Runner

    /**
     * Runs a closure producing a result.
     * @param <R> a result type
     */
    public static interface Result<R> {
        R apply() throws Throwable; // augments standard Supplier interface

        default R valueSurely(Handler<Throwable> handler, R defaultValue, Runnable... finish) {
            try { return apply(); } catch (Throwable ex) { return defaultAfter(ex, handler, defaultValue); }
            finally { runSafely(finish); } }

        static <R> R defaultOrTrySurely(Result<R> r, Handler<Throwable> handler, R defaultValue, Runnable... finish) {
            if (hasNone(r)) { runSafely(finish); return defaultValue; }
            return r.valueSurely(handler, defaultValue, finish); }

    } // Result<R>

    /**
     * Runs a closure producing a result from an argument.
     * @param <T> an item type
     * @param <R> a result type
     */
    public static interface Argued<T, R> {
        R apply(T item) throws Throwable; // augments standard Function interface

        default R valueSurely(T item, Handler<Throwable> handler, R defaultValue, Runnable... finish) {
            try { return apply(item); } catch (Throwable ex) { return defaultAfter(ex, handler, defaultValue); }
            finally { runSafely(finish); } }

        static <T, R> R defaultOrTrySurely(Argued<T, R> r, T item, Handler<Throwable> handler, R defaultValue, Runnable... finish) {
            if (hasNone(r)) { runSafely(finish); return defaultValue; }
            return r.valueSurely(item, handler, defaultValue, finish); }

    } // Argued<T, R>

} // Exceptional