package org.evomaster.client.java.instrumentation.coverage.methodreplacement.classes;

import org.evomaster.client.java.instrumentation.coverage.methodreplacement.DateTimeParsingUtils;
import org.evomaster.client.java.instrumentation.coverage.methodreplacement.MethodReplacementClass;
import org.evomaster.client.java.instrumentation.coverage.methodreplacement.Replacement;
import org.evomaster.client.java.instrumentation.heuristic.Truthness;
import org.evomaster.client.java.instrumentation.heuristic.TruthnessUtils;
import org.evomaster.client.java.instrumentation.shared.ReplacementType;
import org.evomaster.client.java.instrumentation.shared.StringSpecialization;
import org.evomaster.client.java.instrumentation.shared.StringSpecializationInfo;
import org.evomaster.client.java.instrumentation.staticstate.ExecutionTracer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.util.Objects;

import static org.evomaster.client.java.instrumentation.coverage.methodreplacement.DistanceHelper.*;


public class LocalDateClassReplacement implements MethodReplacementClass {

    @Override
    public Class<?> getTargetClass() {
        return LocalDate.class;
    }

    @Replacement(type = ReplacementType.EXCEPTION, replacingStatic = true)
    public static LocalDate parse(CharSequence input, String idTemplate) {

        if (input != null && ExecutionTracer.isTaintInput(input.toString())) {
            ExecutionTracer.addStringSpecialization(input.toString(),
                    new StringSpecializationInfo(StringSpecialization.DATE_YYYY_MM_DD, null));
        }

        if (idTemplate == null) {
            return LocalDate.parse(input);
        }

        try {
            LocalDate res = LocalDate.parse(input);
            ExecutionTracer.executedReplacedMethod(idTemplate, ReplacementType.EXCEPTION, new Truthness(1, 0));
            return res;
        } catch (RuntimeException e) {
            double h = DateTimeParsingUtils.getDistanceToISOLocalDate(input);
            ExecutionTracer.executedReplacedMethod(idTemplate, ReplacementType.EXCEPTION, new Truthness(h, 1));
            throw e;
        }
    }



    @Replacement(type = ReplacementType.BOOLEAN)
    public static boolean equals(LocalDate caller, Object anObject, String idTemplate) {
        Objects.requireNonNull(caller);

        if (idTemplate == null) {
            return caller.equals(anObject);
        }

        final Truthness t;
        if (anObject == null || !(anObject instanceof LocalDate)) {
            t = new Truthness(0d, 1d);
        } else {
            final long a = caller.toEpochDay();
            final long b = ((LocalDate) anObject).toEpochDay();
            t = TruthnessUtils.getEqualityTruthness(a, b);
        }
        ExecutionTracer.executedReplacedMethod(idTemplate, ReplacementType.BOOLEAN, t);
        return caller.equals(anObject);
    }

    @Replacement(type = ReplacementType.BOOLEAN)
    public static boolean isBefore(LocalDate caller, ChronoLocalDate when, String idTemplate) {
        Objects.requireNonNull(caller);
        return LocalDateTimeClassReplacement.isBefore(
                toLocalDateTime(caller),
                when == null ? null : toChronoLocalDateTime(when),
                idTemplate);

    }

    private static LocalDateTime toLocalDateTime(LocalDate localDate) {
        Objects.requireNonNull(localDate);
        return localDate.atTime(LocalTime.MIDNIGHT);
    }

    private static ChronoLocalDateTime toChronoLocalDateTime(ChronoLocalDate chronoLocalDate) {
        Objects.requireNonNull(chronoLocalDate);
        return chronoLocalDate.atTime(LocalTime.MIDNIGHT);
    }

    @Replacement(type = ReplacementType.BOOLEAN)
    public static boolean isAfter(LocalDate caller, ChronoLocalDate when, String idTemplate) {
        Objects.requireNonNull(caller);
        return LocalDateTimeClassReplacement.isAfter(
                toLocalDateTime(caller),
                when == null ? null : toChronoLocalDateTime(when),
                idTemplate);
    }

    @Replacement(type = ReplacementType.BOOLEAN)
    public static boolean isEqual(LocalDate caller, ChronoLocalDate other, String idTemplate) {
        Objects.requireNonNull(caller);
        return LocalDateTimeClassReplacement.isEqual(
                toLocalDateTime(caller),
                other == null ? null : toChronoLocalDateTime(other),
                idTemplate
        );
    }

}