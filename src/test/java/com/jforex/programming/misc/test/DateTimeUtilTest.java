package com.jforex.programming.misc.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;

import org.junit.Before;
import org.junit.Test;

import com.jforex.programming.misc.DateTimeUtil;

import com.dukascopy.api.JFException;

public class DateTimeUtilTest {

    private final DateTimeFormatter formatter = DateTimeUtil.defaultformatter;
    private final String testDateTimeString = "2016-04-26 08:12:42.123";
    private final LocalDateTime testDateTime = LocalDateTime.parse(testDateTimeString, formatter);
    private long testMillis;
    private LocalDateTime dateTimeOfMillis;

    @Before
    public void setUp() throws JFException {
        testMillis = DateTimeUtil.millisFromDateTime(testDateTime);
        dateTimeOfMillis = DateTimeUtil.dateTimeFromMillis(testMillis);
    }

    private void assertWeekend(final String localDateTimeString,
                               final boolean isWeekend) {
        final LocalDateTime localDateTime = LocalDateTime.parse(localDateTimeString, formatter);
        if (!isWeekend)
            assertFalse(DateTimeUtil.isWeekendDateTime(localDateTime));
        else
            assertTrue(DateTimeUtil.isWeekendDateTime(localDateTime));
    }

    @Test
    public void testNanoToMillisIsCorrect() {
        assertThat(DateTimeUtil.millisFromNano(dateTimeOfMillis.getNano()), equalTo(123L));
    }

    @Test
    public void testConversionToMillisIsCorrect() {
        assertThat(dateTimeOfMillis.getYear(), equalTo(2016));
        assertThat(dateTimeOfMillis.getMonth(), equalTo(Month.APRIL));
        assertThat(dateTimeOfMillis.getDayOfMonth(), equalTo(26));
        assertThat(dateTimeOfMillis.getHour(), equalTo(8));
        assertThat(dateTimeOfMillis.getMinute(), equalTo(12));
        assertThat(dateTimeOfMillis.getSecond(), equalTo(42));
        assertThat(dateTimeOfMillis.getDayOfWeek(), equalTo(DayOfWeek.TUESDAY));
    }

    @Test
    public void testToDukascopyTimeIsCorrect() {
        final LocalDateTime dukascopyTime = DateTimeUtil.toDukascopyDateTime(testDateTime);

        assertThat(testDateTime.getHour(), equalTo(dukascopyTime.getHour() + 2));
    }

    @Test
    public void testIsWeekendCorrect() {
        assertFalse(DateTimeUtil.isWeekendDateTime(testDateTime));
        assertFalse(DateTimeUtil.isWeekendMillis(testMillis));

        assertWeekend("2016-04-29 21:59:42.123", false);
        assertWeekend("2016-04-29 22:00:00.000", true);
        assertWeekend("2016-04-29 22:00:01.000", true);
        assertWeekend("2016-04-30 02:12:12.000", true);
        assertWeekend("2016-04-31 23:45:00.000", true);
        assertWeekend("2016-04-31 23:00:00.000", true);
        assertWeekend("2016-05-01 00:00:00.000", false);
    }
}
