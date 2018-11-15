package com.capitalone.creditocr.util;

import org.springframework.lang.Nullable;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;

public class TimeUtils {

    private static final String[] STRING_DATE_FORMATS = {
            "M/d/y",
            "M-d-y",
            "MMMMM d y",
            "MMMMM d, y",
            "MMMMM. d y",
            "MMMMM. d, y"
    };

    /**
     * Convert an {@link Instant} to a {@link Date}
     *
     * @param instant The object to convert
     * @return The converted date, or {@code null} if the parameter was null.
     */
    @Nullable
    public static Date instant2date(@Nullable Instant instant) {
        if (instant == null) {
            return null;
        } else {
            return new Date(instant.toEpochMilli());
        }
    }

    @Nullable
    public static Instant date2instant(@Nullable Date date) {
        if (date == null) {
            return null;
        } else {
            return Instant.ofEpochMilli(date.getTime());
        }
    }

    /**
     * Convert a string to an {@link Instant}
     * @param dateString The date string
     * @return The converted instant, or {@code null} if the parameter is null or the string is in an invalid format
     */
    @Nullable
    public static Instant string2Instant(@Nullable String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }

        for (String formatStr : STRING_DATE_FORMATS) {
            var format = new SimpleDateFormat(formatStr);

            try {
                return format.parse(dateString).toInstant();
            } catch (ParseException e) {
                // The format string we're trying didn't work. This is ok
            }
        }

        // No matching pattern.
        return null;
    }
}
