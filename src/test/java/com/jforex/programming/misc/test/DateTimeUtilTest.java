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
import com.jforex.programming.test.common.CommonUtilForTest;

public class DateTimeUtilTest extends CommonUtilForTest {

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
    public void testConstructorIsPrivate() throws Exception {
        assertPrivateConstructor(DateTimeUtil.class);
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
    public void dateTimeToStringIsCorrect() {
        final LocalDateTime localDateTime = LocalDateTime.of(2016,
                                                             Month.APRIL,
                                                             26,
                                                             8,
                                                             12);

        final String dateTimeString = DateTimeUtil.dateTimeToString(localDateTime);

        assertThat(dateTimeString, equalTo("2016-04-26 08:12:00.000"));
    }

    @Test
    public void millisToStringIsCorrect() {
        final long millis =
                DateTimeUtil.localMillisFromDateTime(LocalDateTime.of(2016,
                                                                      Month.APRIL,
                                                                      26,
                                                                      8,
                                                                      12));

        final String dateTimeString = DateTimeUtil.millisToString(millis);

        assertThat(dateTimeString, equalTo("2016-04-26 08:12:00.000"));
    }
}
