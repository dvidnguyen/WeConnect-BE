package com.example.WeConnect_BE.Util;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.AbstractMap;
import java.util.Base64;

public class CursorUtil {
    private CursorUtil(){}

    public static String encode(LocalDateTime sentAt, String id) {
        String raw = sentAt.toInstant(ZoneOffset.UTC) + "|" + id;
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static AbstractMap.SimpleEntry<Instant, String> decode(String cursor) {
        String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
        String[] p = raw.split("\\|", 2);
        return new AbstractMap.SimpleEntry<>(Instant.parse(p[0]), p[1]);
    }
}
