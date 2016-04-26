package com.jforex.programming.misc;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

public final class DateTimeUtil {

    public final static String defaultDateFormat = "yyyy-MM-dd HH:mm:ss.SSS";
    public final static DateTimeFormatter defaultformatter = DateTimeFormatter.ofPattern(defaultDateFormat);
    public final static ZoneId dukascopyZoneId = ZoneId.ofOffset("UTC", ZoneOffset.UTC);
    private final static int closingHour = 21;
    private final static int openingHour = 0;

    public final static Instant instantFromMillis(final long millis) {
        return Instant.ofEpochMilli(millis);
    }

    public final static LocalDateTime dateTimeFromMillis(final long millis) {
        final Instant instant = instantFromMillis(millis);
        return LocalDateTime.ofInstant(instant, dukascopyZoneId);
    }

    public final static long millisFromDateTime(final LocalDateTime dateTime) {
        return dateTime.atZone(dukascopyZoneId).toInstant().toEpochMilli();
    }

    public final static LocalDateTime toDukascopyDateTime(final LocalDateTime dateTime) {
        final ZonedDateTime zonedDateTime = ZonedDateTime.of(dateTime, ZoneId.systemDefault());
        final ZonedDateTime zonedDateTimeWithInstant = zonedDateTime.withZoneSameInstant(DateTimeUtil.dukascopyZoneId);
        return zonedDateTimeWithInstant.toLocalDateTime();
    }

    public final static long millisFromNano(final long nanos) {
        return TimeUnit.NANOSECONDS.toMillis(nanos);
    }

    public final static boolean isWeekendDateTime(final LocalDateTime dateTime) {
        switch (dateTime.getDayOfWeek()) {
        case FRIDAY:
            return dateTime.getHour() > closingHour;
        case SATURDAY:
            return true;
        case SUNDAY:
            return dateTime.getHour() < openingHour;
        default:
            return false;
        }
    }

    public final static boolean isWeekendMillis(final long millis) {
        return isWeekendDateTime(dateTimeFromMillis(millis));
    }
}
