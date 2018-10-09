package com.capitalone.creditocr.util;

import org.springframework.lang.Nullable;

import java.sql.Date;
import java.time.Instant;

public class TimeUtils {

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

}
