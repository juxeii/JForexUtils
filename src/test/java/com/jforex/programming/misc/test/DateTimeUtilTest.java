package com.jforex.programming.misc.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;

import org.junit.Before;
import org.junit.Test;

import com.dukascopy.api.JFException;
import com.jforex.programming.misc.DateTimeUtil;

public class DateTimeUtilTest {

    private final DateTimeFormatter formatter = DateTimeUtil.defaultformatter;
    private final String testDateTimeString = "2016-04-26 08:12:42.123";
    private final LocalDateTime testDateTime = LocalDateTime.parse(testDateTimeString, formatter);
    private long testMillis;
    private LocalDateTime dateTimeOfMillis;

    @Before
    public void setUp() throws JFException {
        testMillis = DateTimeUtil.localMillisFromDateTime(testDateTime);
        dateTimeOfMillis = DateTimeUtil.dateTimeFromMillis(testMillis);
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
}
