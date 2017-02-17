package com.jforex.programming.misc.test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;

import org.junit.Test;

import com.jforex.programming.misc.DateTimeUtil;
import com.jforex.programming.test.common.CommonUtilForTest;

public class DateTimeUtilTest extends CommonUtilForTest {

    private final DateTimeFormatter formatter = DateTimeUtil.defaultformatter;
    private final String testDateTimeString = "2016-04-26 08:12:42.123";
    private final LocalDateTime testDateTime = LocalDateTime.parse(testDateTimeString, formatter);
    private final long localTestMillis = DateTimeUtil.millisFromDateTime(testDateTime);
    private LocalDateTime dateTimeOfMillis;

    @Test
    public void testConstructorIsPrivate() throws Exception {
        assertPrivateConstructor(DateTimeUtil.class);
    }

    @Test
    public void testConversionToMillisIsCorrect() {
        dateTimeOfMillis = DateTimeUtil.dateTimeFromMillis(localTestMillis);

        assertThat(dateTimeOfMillis.getYear(), equalTo(2016));
        assertThat(dateTimeOfMillis.getMonth(), equalTo(Month.APRIL));
        assertThat(dateTimeOfMillis.getDayOfMonth(), equalTo(26));
        assertThat(dateTimeOfMillis.getHour(), equalTo(8));
        assertThat(dateTimeOfMillis.getMinute(), equalTo(12));
        assertThat(dateTimeOfMillis.getSecond(), equalTo(42));
        assertThat(dateTimeOfMillis.getDayOfWeek(), equalTo(DayOfWeek.TUESDAY));
    }

    @Test
    public void testNanoToMillisIsCorrect() {
        dateTimeOfMillis = DateTimeUtil.dateTimeFromMillis(localTestMillis);

        assertThat(DateTimeUtil.millisFromNano(dateTimeOfMillis.getNano()), equalTo(123L));
    }

    @Test
    public void formattingIsCorrect() {
        final String dateTimeString = DateTimeUtil.format(testDateTime);

        assertThat(dateTimeString, equalTo(testDateTimeString));
    }

    @Test
    public void millisToStringIsCorrect() {
        final String dateTimeString = DateTimeUtil.formatMillis(localTestMillis);

        assertThat(dateTimeString, equalTo(testDateTimeString));
    }
}
