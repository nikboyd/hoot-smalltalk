package Hoot.Runtime.Faces;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.StringUtils;
import static Hoot.Runtime.Functions.Utils.*;

/**
 * Grafts standard logging methods onto any class.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2019 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public interface Logging {

    public static Logger logger(Class<?> aClass) { return LoggerFactory.getLogger(aClass); }
    default Logger logger() { return logger(getClass()); }

    default boolean testOrReport(Throwable ex) {
        if (hasNo(ex)) return false;
        if (hasNo(ex.getMessage())) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    default void whisper(String message) { logger().debug(message); }
    default void whisper(Throwable ex) { if (testOrReport(ex)) logger().debug(ex.getMessage(), ex); }

    default void report(String message) { logger().info(message); }
    default void report(Throwable ex) { if (testOrReport(ex)) logger().info(ex.getMessage(), ex); }

    default void warn(String message) { logger().warn(message); }
    default void warn(Throwable ex) { if (testOrReport(ex)) logger().warn(ex.getMessage(), ex); }

    default void error(String message) { logger().error(message); }
    default void error(String message, Throwable ex) { error(message); error(ex); }
    default void error(Throwable ex) { if (testOrReport(ex)) logger().error(ex.getMessage(), ex); }

    default String format(String report, Object ... values) { return String.format(report, values); }
    static int countMatches(CharSequence seq, CharSequence chars) { return StringUtils.countMatches(seq, chars); }
    static String strip(String value, String chars) { return StringUtils.strip(value, chars); }

    static boolean notEmpty(CharSequence seq) { return !isEmpty(seq); }
    static boolean isEmpty(CharSequence seq) { return StringUtils.isEmpty(seq); }
    static final String Empty = "";

} // Logging
