package util;

import exception.ValidationException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ParamUtil {

    private ParamUtil() {
    }

    public static LocalDate parseDate(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Некорректная дата: " + value);
        }
    }

    public static UUID parseUuid(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static List<UUID> parseUuidList(String[] values) {
        List<UUID> ids = new ArrayList<>();
        if (values == null) {
            return ids;
        }
        for (String value : values) {
            UUID id = parseUuid(value);
            if (id != null) {
                ids.add(id);
            }
        }
        return ids;
    }

    public static List<String> parseCommaSeparated(String value) {
        if (value == null || value.isBlank()) {
            return new ArrayList<>();
        }
        return Arrays.asList(value.split(","));
    }
}
