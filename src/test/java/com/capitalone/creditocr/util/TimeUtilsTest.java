package com.capitalone.creditocr.util;

import org.junit.Test;

import java.time.Instant;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class TimeUtilsTest {

    @Test
    public void string2Instant() {
        var date = new GregorianCalendar();
        date.setTimeInMillis(0); // Clear current time, since the current time that is initially set will cause test to fail.
        date.set(2018, Calendar.NOVEMBER, 14, 0, 0, 0);

        var instant = date.toInstant();

        Map<String, Instant> cases = Map.of(
                "11/14/2018", instant,
                "11-14-2018", instant,
                "November 14 2018", instant,
                "November 14, 2018", instant,
                "11/14/18", instant,
                "11-14-18", instant,
                "Nov 14 2018", instant,
                "Nov. 14 2018", instant,
                "Nov 14, 2018", instant,
                "Nov. 14, 2018", instant
        );

        cases.forEach((k, v) -> assertEquals("date string = " + k,
                v, TimeUtils.string2Instant(k)));
    }
}