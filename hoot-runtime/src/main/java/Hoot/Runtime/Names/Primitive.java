package Hoot.Runtime.Names;

import java.io.*;
import java.util.*;
import java.time.*;
import java.math.*;
import java.time.format.*;
import java.lang.reflect.*;
import static java.lang.Math.*;
import static java.lang.reflect.Modifier.*;
import org.apache.commons.lang3.StringUtils;
import static org.apache.commons.lang3.StringUtils.*;

import Hoot.Runtime.Faces.Valued;
import Hoot.Runtime.Faces.Logging;
import Hoot.Runtime.Faces.IntegerValue;
import Hoot.Runtime.Behaviors.HootRegistry;
import static Hoot.Runtime.Names.Name.Dot;
import static Hoot.Runtime.Names.Keyword.Arrayed;
import static Hoot.Runtime.Functions.Exceptional.*;
import static Hoot.Runtime.Functions.Utils.*;
import java.util.Map.Entry;

/**
 * Knows primitive Java types.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 */
public class Primitive implements Logging {

    // system level logging
    public static void printError(String message) { System.err.println(message); }
    public static void printLine(String message) { System.out.println(message); }
    public static void print(String message) { System.out.print(message); }
    public static void printLine() { System.out.println(); }

    static final String LongReport = "%,d";
    public static String printLong(long longValue) { return String.format(LongReport, longValue); }

    public static final String WriteMode = "rw";
    public static final String CreateMode = "create";
    public static final String AppendMode = "append";
    public static final String TruncateMode = "truncate";
    public static RandomAccessFile writeFile(String fileName, String openMode) {
        File file = new File(fileName);
        if (file.exists()) {
            return nullOrTryLoudly(() -> {
                RandomAccessFile result = new RandomAccessFile(file, WriteMode);
                if (!AppendMode.equals(openMode)) { result.setLength(0); } // truncate file
                return result;
            });
        }
        else {
            return nullOrTryLoudly(() -> { return new RandomAccessFile(file, WriteMode); });
        }
    }

    public static final String ReadMode = "r";
    public static RandomAccessFile readFile(String fileName) { return readFile(fileName, ReadMode); }
    public static RandomAccessFile readFile(String fileName, String fileMode) {
        return nullOrTryLoudly(() -> {
            File file = new File(fileName);
            if (!file.exists()) return null;
            return new RandomAccessFile(file, fileMode);
        });
    }

    public static boolean isStatic(Field f) { return STATIC == (f.getModifiers() & STATIC); }
    public static boolean isStatic(Method m) { return STATIC == (m.getModifiers() & STATIC); }

    public static int minimumPriority() { return Thread.MIN_PRIORITY; }
    public static int maximumPriority() { return Thread.MAX_PRIORITY; }
    public static int normalPriority() { return Thread.NORM_PRIORITY; }

    public static void waitMilliseconds(IntegerValue duration) { runQuietly(() -> Thread.sleep(duration.intValue())); }

    static final DateTimeFormatter PreferredTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSS");
    public static String printTime(LocalDateTime timestamp) { return timestamp.format(PreferredTimeFormat); }
    public static String printTimeNow() { return printTime(LocalDateTime.now()); }
    public static String formatTime(LocalDateTime timestamp, String pattern) {
        return timestamp.format(DateTimeFormatter.ofPattern(pattern)); }

    public static int hierarchyDepth(Class<?> aClass) {
        int count = 0; Class<?> testClass = aClass;
        while (testClass != null) { count++; testClass = testClass.getSuperclass(); }
        return count; }

    public static int length(byte[] bytes) { return bytes.length; }
    public static <T> int length(T[] elements) { return elements.length; }
    public static String reverse(String value) { return StringUtils.reverse(value); }

    public static List<String> tokenize(String value, String separators) {
        return map(Collections.list(tokens(value, separators)), token -> (String)token); }

    public static StringTokenizer tokens(String value, String separators) {
        return new StringTokenizer(value, separators); }

    public static String systemValue(String valueName) { return System.getProperty(valueName); }
    public static String systemValue(String valueName, String defaultValue) {
        return System.getProperty(valueName, defaultValue); }

    public static Double radiansPerDegree() { return (Math.PI / 180.0f); }
    public static Double degreesPerRadian() { return (180.0f / Math.PI); }

    private static final int NegativeUnity = -1;
    public static int negativeUnity() { return NegativeUnity; }

    static final String DigitValue = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static String printDecimal(long... values) { // upper, lower, scale
        StringBuilder b = new StringBuilder();
        long upper = values[0];
        long lower = values[1];
        long scale = values[2];
        long d = (long)Math.pow(10, scale);
        long n = upper * d;

        n += lower >> 1; n /= lower;
        if (upper < 0L) {
            b.append('-'); n = 0 - n;
        }

        long quo = n / d;
        long rem = n % d;
        int s = (int)scale;

        b.append(quo);
        b.append('.');
        while (s-- > 0) {
            rem = rem * 10L;
            int digit = (int)(rem / d);
            b.append(DigitValue.charAt(digit));
            rem = rem % d;
        }

        b.append('s');
        b.append(scale);
        return b.toString();
    }

    @SuppressWarnings("UseSpecificCatch")
    public static boolean class_hasMethod(Class<?> aClass, String methodName) {
        try {
            return hasOne(aClass.getMethod(methodName));
        }
        catch (Exception e) {
            return false;
        }
    }

    public static java.net.URL locateClass(Class<?> aClass) {
        return aClass.getProtectionDomain().getCodeSource().getLocation();
    }

    public static Object newInstance(Class<?> aClass) {
        try {
            return aClass.newInstance();
        }
        catch (IllegalAccessException | InstantiationException ex) {
            printError(aClass.getCanonicalName() + " can't create a new instance");
            return HootRegistry.Nil();
        }
    }

    public static int gcd(int a, int b) { return b == 0 ? a : gcd(b, a % b); }
    public static Integer[] rationalize(int num, int den) {
        int n = den < 0 ? -num : num;
        int d = abs(den);
        int gcd = gcd(n, d);

        if (d == gcd) {
            Integer[] results = { n / gcd, 1 };
            return results;
        }

        if (1 == gcd) {
            Integer[] results = { n, d };
            return results;
        }

        Integer[] results = { n / gcd, d / gcd };
        return results;
    }

    public static double truncate(double value) { return (value < 0.0d ? Math.ceil(value) : Math.floor(value)); }

    public static BigDecimal decimalFrom(long numerator, long denominator, int scale) {
        return new BigDecimal(numerator)
            .divide(new BigDecimal(denominator),
                scale, RoundingMode.HALF_UP); }

    public static final Primitive Reporter = new Primitive();
    public static BigInteger[] integersFrom(Double... ds) {
        BigInteger[] results = {
            BigDecimal.valueOf(ds[0]).toBigInteger(),
            BigDecimal.valueOf(ds[1]).toBigInteger() };
        Reporter.whisper(results[0].toString());
        Reporter.whisper(results[1].toString());
        return results; }

    public static BigInteger[] fractionalize(double value, int sig) {
        double limit = Math.pow( 10.0d, sig );

        double n1 = truncate(value); double n2 = 1.0d;
        double d1 = 1.0d; double d2 = 0.0d;

        double i  = n1; double x  = 0.0d;
        double f = value - n1;

        while (f != 0.0d) {
            double dn = 1.0d / f;
            i = truncate(dn); f = dn - i;

            x = n2; n2 = n1;
            n1 = ( n1 * i ) + x;

            x = d2; d2 = d1;
            d1 = ( d1 * i ) + x;
            if (limit < d1) {
                if (n2 == 0.0d) {
                    return integersFrom(n1, d1);
                }
                else {
                    return integersFrom(n2, d2);
                }
            }
        }

        return integersFrom(n1, d1);
    }

    public static Long[] parseDecimal(String value) {
        int point = value.indexOf( '.' );
        int sMark = value.indexOf( 's' );
        String iString = value.substring( 0, point );
        String fString = value.substring( point + 1, sMark );
        String sString = value.substring( sMark + 1 );

        long scale = 0;
        long numerator = 0;
        long denominator = 1;
        for( int i = 0; i < fString.length(); i++ ) denominator *= 10;
        Long[] results = { numerator, denominator, scale };

        // integer digits + fraction digits / scale of 10s
        runQuietly(() -> {
            results[2] = Long.parseLong(sString);
            results[0] = Long.parseLong(iString + fString);
        });
        return results;
    }

    public static int invertBits(int bits) { return ~bits; }
    public static long invertBits(long bits) { return ~bits; }
    public static int xorBits(int a, int b) { return a ^ b; }
    public static long xorBits(long a, long b) { return a ^ b; }

    public static Double elementaryMaxDouble() { return java.lang.Double.MAX_VALUE; }
    public static Double elementaryMinDouble() { return java.lang.Double.MIN_VALUE; }

    public static Float elementaryMaxFloat() { return java.lang.Float.MAX_VALUE; }
    public static Float elementaryMinFloat() { return java.lang.Float.MIN_VALUE; }

    public static Long elementaryMaxLong() { return java.lang.Long.MAX_VALUE; }
    public static Long elementaryMinLong() { return java.lang.Long.MIN_VALUE; }

    public static Integer elementaryMaxInteger() { return java.lang.Integer.MAX_VALUE; }
    public static Integer elementaryMinInteger() { return java.lang.Integer.MIN_VALUE; }

    public static Short elementaryMaxShort() { return java.lang.Short.MAX_VALUE; }
    public static Short elementaryMinShort() { return java.lang.Short.MIN_VALUE; }

    public static Boolean elementaryFalse() { return java.lang.Boolean.FALSE; }
    public static Boolean elementaryTrue() { return java.lang.Boolean.TRUE; }

    public static float getFloat(String value) { return Float.parseFloat(value); }

    public static final String Dollar = "$";
    public static final String Pound = "#";
    public static String getLiteral(String value) {
        if (isEmpty(value)) return Empty;
        if (value.startsWith(Dollar)) return quoteWith(SingleQuote, value.substring(1));
        if (value.startsWith(Pound)) return quoteWith(NativeQuote, value.substring(1));
        if (value.startsWith(SingleQuote)) return quoteNatively(value);
        return value; }

    public static final String SingleQuote = "'";
    public static String quoteLiterally(String value) { return quoteWith(SingleQuote, value); }
    public static String trimQuotes(String quote, String value) { return StringUtils.strip(value, quote); }
    public static String trimQuotes(String value) { return StringUtils.strip(value, NativeQuote); }
    public static String trimQuoted(String value) { return StringUtils.strip(value, SingleQuote); }

    public static final String NativeQuote = "\"";
    public static final String DoubledQuotes = "''";
    public static String quoteNatively(String value) {
        return quoteWith(NativeQuote, trimQuotes(SingleQuote, value).replace(DoubledQuotes, SingleQuote)); }

    public static String quoteWith(String quote, String value) {
        return (isEmpty(value) ? quote + quote : quote + value + quote); }

    public static Valued shallowCopyOf(Valued v) { return v; }
    public static Valued deepCopyOf(Valued v) { return v; }

    protected static final String[] ElementPackages = { "java.", "javax.", };
    protected static final List<String> ElementaryPackages = wrap(ElementPackages);

    public static final String[] SerializedNames = { "Cloneable", "Serializable", "CharSequence", "Comparable", };
    public static final List<String> SerializedTypes = wrap(SerializedNames);

    protected static final Map<String, String> ConversionTypes = emptyWordMap();
    protected static final Map<String, String> PrimitiveWrappers = emptyWordMap();
    protected static final Map<String, String> PrimitiveUnwrappers = emptyWordMap();
    protected static final Map<String, String> ElementaryWrappers = emptyWordMap();
    protected static final Map<String, String> ElementaryUnwrappers = emptyWordMap();
    protected static final Map<String, Class> PrimitiveTypes = emptyMap(Class.class);
    protected static final List<String> Primitives = emptyList(String.class);
    public    static final Class<?> JavaRoot = java.lang.Object.class;

    static {
        initializePrimitiveTypes();
        initializePrimitiveWrappers();
        initializeObjectWrappers();
    }

    protected static void initializePrimitiveTypes() {
        PrimitiveTypes.put("void", void.class);
        PrimitiveTypes.put("boolean", boolean.class);
        PrimitiveTypes.put("byte", byte.class);
        PrimitiveTypes.put("char", char.class);
        PrimitiveTypes.put("int", int.class);
        PrimitiveTypes.put("short", short.class);
        PrimitiveTypes.put("long", long.class);
        PrimitiveTypes.put("float", float.class);
        PrimitiveTypes.put("double", double.class);
        PrimitiveTypes.put("string", String.class);
        PrimitiveTypes.put("decimal", java.math.BigDecimal.class);
    }

    protected static void initializePrimitiveWrappers() {
        PrimitiveWrappers.put("boolean", "Boolean");
        PrimitiveWrappers.put("byte", "Number");
        PrimitiveWrappers.put("char", "Number");
        PrimitiveWrappers.put("short", "Number");
        PrimitiveWrappers.put("int", "Number");
        PrimitiveWrappers.put("long", "Number");
        PrimitiveWrappers.put("float", "Float");
        PrimitiveWrappers.put("double", "Double");

        PrimitiveUnwrappers.put("boolean", "asPrimitive");
        PrimitiveUnwrappers.put("byte", "primitiveByte");
        PrimitiveUnwrappers.put("char", "primitiveCharacter");
        PrimitiveUnwrappers.put("short", "primitiveShort");
        PrimitiveUnwrappers.put("int", "primitiveInteger");
        PrimitiveUnwrappers.put("long", "primitiveLong");
        PrimitiveUnwrappers.put("float", "asPrimitive");
        PrimitiveUnwrappers.put("double", "asPrimitive");

        Primitives.addAll(PrimitiveWrappers.keySet());
        Primitives.add("void");
    }

    protected static void initializeObjectWrappers() {
        ConversionTypes.put("Int", "int");
        ConversionTypes.put("Byte", "byte");
        ConversionTypes.put("java.lang.Byte", "byte");
        ConversionTypes.put("java.lang.Boolean", "boolean");
        ConversionTypes.put("java.lang.String", "java.lang.String");

        ElementaryWrappers.put("java.math.BigDecimal", "Fixed");
        ElementaryWrappers.put("java.lang.Boolean", "Boolean");
        ElementaryWrappers.put("java.lang.Float", "Float");
        ElementaryWrappers.put("java.lang.Double", "Double");
        ElementaryWrappers.put("java.lang.Character", "Character");
        ElementaryWrappers.put("java.lang.String", "String");
        ElementaryWrappers.put(JavaRoot.getCanonicalName(), "Object");

        ElementaryWrappers.put("Boolean", "Boolean");
        ElementaryWrappers.put("String", "String");
        ElementaryWrappers.put("Float", "Float");
        ElementaryWrappers.put("Double", "Double");
        ElementaryWrappers.put("Object", "Object");

        ElementaryUnwrappers.put("java.math.BigDecimal", "asPrimitive");
        ElementaryUnwrappers.put("java.lang.Boolean", "asPrimitive");
        ElementaryUnwrappers.put("java.lang.String", "asPrimitive");

        // can any others be predetermined?
    }

    public static Class typeNamed(String typeName) {
        return PrimitiveTypes.getOrDefault(typeName.toLowerCase(), null); }

    public static String simplifiedType(String typeName) { return typeName.replace(Arrayed, Empty).toLowerCase(); }
    public static Class getPrimitiveType(String typeName) { return PrimitiveTypes.get(typeName); }
    public static String convertType(String typeName) {
        String result = convertsType(typeName) ? ConversionTypes.get(typeName) : typeName;
        return result; }

    public static boolean convertsType(String typeName) {
        return !typeName.isEmpty() && ConversionTypes.containsKey(typeName); }

    public static boolean isPrimitiveType(String typeName) {
        return !typeName.isEmpty() &&
            Primitives.contains(simplifiedType(typeName)) &&
            !ElementaryWrappers.containsKey(typeName); }

    public static boolean isElementaryType(String typeName) {
        return !typeName.isEmpty() && ElementaryWrappers.containsKey(typeName); }

    public static boolean isEraseableType(String typeName) {
        return !typeName.isEmpty() &&
            (PrimitiveWrappers.containsKey(simplifiedType(typeName)) ||
            ElementaryWrappers.containsKey(typeName)); }

    public static boolean fromElementaryPackage(String typeName) {
        return !typeName.isEmpty() && typeName.contains(Dot) ?
            matchAny(ElementaryPackages, prefix -> simplifiedType(typeName).startsWith(prefix)) :
            matchAny(ElementaryPackages, prefix -> prefix.startsWith(simplifiedType(typeName))); }

    public static final String[] EmptyArray = { };
    public static String[] wrapsFrom(String typeName) {
        if (PrimitiveWrappers.containsKey(typeName)) {
            String[] result = { PrimitiveWrappers.get(typeName), PrimitiveUnwrappers.get(typeName) };
            return result;
        }

        if (ElementaryWrappers.containsKey(typeName)) {
            String[] result = { ElementaryWrappers.get(typeName), ElementaryUnwrappers.get(typeName) };
            return result;
        }

        String[] empty = EmptyArray;
        return empty;
    }

    public static <K, T> K findKey(T value, Map<K,T> map) {
        for (Entry<K,T> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) return entry.getKey();
        }
        return null;
    }

    public static final String Blank = " ";

} // Primitive
