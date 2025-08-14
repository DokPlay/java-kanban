package util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class CsvUtils {
    private CsvUtils() {}

    public static String escape(String s) {
        return s == null ? "" : s;
    }

    public static LocalDateTime parseTimeOrNull(String s, DateTimeFormatter fmt) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return LocalDateTime.parse(s, fmt);
        } catch (DateTimeParseException ex) {
            try {
                return LocalDateTime.parse(s); // ISO fallback
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }
    }
}
