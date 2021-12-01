package Hoot.Runtime.Behaviors;

import java.util.*;
import Hoot.Runtime.Faces.Named;
import Hoot.Runtime.Names.TypeName;
import static Hoot.Runtime.Functions.Utils.*;

/**
 * Describes a method signature.
 *
 * @author nik <nikboyd@sonic.net>
 * @see "Copyright 2010,2021 Nikolas S Boyd."
 * @see "Permission is granted to copy this work provided this copyright statement is retained in all copies."
 * @see <a href="https://gitlab.com/hoot-smalltalk/hoot-smalltalk/tree/master/LICENSE.txt">LICENSE for more details</a>
 */
public interface Signed extends Named {

    boolean isStatic();
    Typified faceType();
    int argumentCount();
    List<Typified> argumentTypes();
    List<TypeName> argumentTypeNames();

    default boolean overridesHeritage() {
        Set<Typified> heritage = copySet(faceType().fullInheritance());
        return matchAny(heritage, type -> {
            Signed s = type.getSigned(this);
            return hasAny(s) && !this.isStatic() && this.matches(s); }); }

    default boolean matches(Signed s) {
        boolean matchCounts = argumentCount() == s.argumentCount();
        boolean matchNames = methodName().equals(s.methodName());
        boolean faceDiffers = !faceName().equals(s.faceName());
        boolean sameArgTypes = matchesArgumentTypes(s);

        boolean result = matchCounts && matchNames && faceDiffers && sameArgTypes;
        reportMatch(s, result);
        return result; }

    default boolean matchesArgumentTypes(Signed s) {
        if (argumentCount() != s.argumentCount()) return false;
        List<TypeName> aTypes = argumentTypeNames();
        List<TypeName> bTypes = s.argumentTypeNames();

        for (int index = 0; index < aTypes.size(); index++) {
            if (hasNo(aTypes.get(index))) return reportMissingArg(index);
            if (hasNo(bTypes.get(index))) return reportMissingArg(index);
            if (!aTypes.get(index).matches(bTypes.get(index))) return false;
        }

        // all args match
        return true; }

    default String faceName() { return faceType().fullName(); }
    default String methodName() { return name().toString(); }
    default String fullSignature() { return shortSignature(); }
    default String erasedSignature() { return shortSignature(); }
    default String shortSignature() { return methodName(); }
    default String matchSignature() { return methodName(); }
    default String matchErasure() { return methodName(); }

    default boolean anyGenericArgs() { return matchAny(argumentTypeNames(), type -> type.isGeneric()); }
    default boolean overridesArguments(Signed s) { return matchesArgumentTypes(s); }
    default boolean overrides(Signed s) {
        boolean matchCounts = argumentCount() == s.argumentCount();
        boolean matchNames = methodName().equals(s.methodName());
        boolean canOverride = !isStatic() && !faceName().equals(s.faceName());
        boolean overage = matchNames && matchCounts && canOverride;
        boolean overrides = matchesArgumentTypes(s);
        boolean result = overage && overrides;

        if (canOverride)
            reportMatch(s, overrides);
        return result; }


    static final String SigsReport = "sigs %s::%s %s %s::%s";
    default boolean reportMatch(Signed s, boolean matched) {
        String comp = (matched ? "~=" : "!=");
        Object[] sigs = {
            faceType().shortName(), matchSignature(), comp,
            s.faceType().shortName(), s.matchSignature(),
        };
        if (matched) {
            whisper(format(SigsReport, sigs));
        }
        else {
            whisper(format(SigsReport, sigs));
        }
        return matched; }

    static final String MissingReport = "missing arg %d in %s::%s";
    default boolean reportMissingArg(int index) {
        report(format(MissingReport, index, getClass().getCanonicalName(), methodName())); return false; }

} // Signed
