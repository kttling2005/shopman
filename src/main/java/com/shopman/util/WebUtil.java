package com.shopman.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Base64;

/** Small request helpers shared by the servlets. */
public final class WebUtil {

    public static final String CSRF_PARAM = "csrf";
    private static final String CSRF_ATTR = "csrfToken";
    private static final SecureRandom RANDOM = new SecureRandom();

    private WebUtil() {
    }

    /** Returns the session's CSRF token, creating it (and the session) on first use. */
    public static String csrfToken(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        String token = (String) session.getAttribute(CSRF_ATTR);
        if (token == null) {
            byte[] bytes = new byte[24];
            RANDOM.nextBytes(bytes);
            token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
            session.setAttribute(CSRF_ATTR, token);
        }
        return token;
    }

    /** True when the POSTed token matches the session's token. */
    public static boolean csrfValid(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }
        String expected = (String) session.getAttribute(CSRF_ATTR);
        String actual = request.getParameter(CSRF_PARAM);
        return expected != null && actual != null
                && MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8), actual.getBytes(StandardCharsets.UTF_8));
    }

    /** Parses yyyy-MM-dd (what {@code <input type="date">} sends). Null for blank or malformed text. */
    public static LocalDate parseDate(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(text.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /** Parses an int; returns {@code fallback} for blank or malformed text. */
    public static int parseInt(String text, int fallback) {
        try {
            return Integer.parseInt(text.trim());
        } catch (RuntimeException e) {
            return fallback;
        }
    }
}
