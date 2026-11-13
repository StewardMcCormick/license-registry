package util;

import java.util.UUID;

public class PathUtil {

    private PathUtil() {
    }

    public static String[] segments(String pathInfo) {
        if (pathInfo == null || pathInfo.isBlank()) {
            return new String[0];
        }
        return pathInfo.substring(1).split("/");
    }

    public static String segment(String[] segments, int index) {
        return index < segments.length ? segments[index] : null;
    }

    public static UUID segmentAsUuid(String[] segments, int index) {
        String value = segment(segments, index);
        if (value == null) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
