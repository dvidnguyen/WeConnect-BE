package com.example.WeConnect_BE.Util;

public class PhoneUtil {
    private PhoneUtil() {}

    public static boolean looksLikePhone(String q) {
        return q != null && q.replaceAll("\\D", "").length() >= 8;
    }
    public static boolean looksLikeEmail(String q) {
        return q != null && q.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    }
}
