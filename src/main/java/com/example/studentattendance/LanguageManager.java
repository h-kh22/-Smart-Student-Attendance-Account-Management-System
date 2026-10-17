package com.example.studentattendance;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Application-wide language / locale manager.
 *
 * Holds the active Locale and provides the matching ResourceBundle.
 * Bundle base name maps to:
 *   resources/com/example/studentattendance/i18n/messages_en.properties
 *   resources/com/example/studentattendance/i18n/messages_ar.properties
 */
public final class LanguageManager {

    private static final String BUNDLE_BASE = "com.example.studentattendance.i18n.messages";
    private static Locale currentLocale = Locale.ENGLISH;

    private LanguageManager() {}

    // ---- Locale API ---------------------------------------------------------

    public static Locale getLocale() { return currentLocale; }

    public static void setLocale(Locale locale) { currentLocale = locale; }

    /** Load language preferences (stub for now). */
    public static void loadPreferences() {
        // For testing purposes, default to English locale.
        currentLocale = Locale.ENGLISH;
    }

    public static void setLanguage(String lang) {
        if ("ar".equalsIgnoreCase(lang)) {
            currentLocale = Locale.forLanguageTag("ar");
        } else {
            currentLocale = Locale.ENGLISH;
        }
    }

    /** Flips between Arabic and English. */
    public static void toggleLocale() {
        currentLocale = isArabic() ? Locale.ENGLISH : Locale.forLanguageTag("ar");
    }

    public static boolean isArabic() {
        return "ar".equals(currentLocale.getLanguage());
    }

    /** @return true when the UI should use RTL node orientation. */
    public static boolean isRTL() { return isArabic(); }

    public static javafx.geometry.NodeOrientation getNodeOrientation() {
        return isArabic() ? javafx.geometry.NodeOrientation.RIGHT_TO_LEFT : javafx.geometry.NodeOrientation.LEFT_TO_RIGHT;
    }

    // ---- ResourceBundle API -------------------------------------------------

    /**
     * Returns the ResourceBundle for the active locale, loaded through the
     * module class loader so it works correctly in the named module.
     */
    public static ResourceBundle getBundle() {
        return ResourceBundle.getBundle(BUNDLE_BASE, currentLocale,
                LanguageManager.class.getModule());
    }

    /**
     * Safe get: returns the translated string or "[key]" if the key is missing.
     */
    public static String get(String key) {
        try {
            return getBundle().getString(key);
        } catch (MissingResourceException e) {
            return "[" + key + "]";
        }
    }

    /**
     * Label shown on the toggle button. Always shows the OTHER language name.
     * EN active  -> shows Arabic label   AR active  -> shows English label
     */
    public static String getToggleLabel() {
        return isArabic() ? "\uD83C\uDF10 English" : "\uD83C\uDF10 \u0639\u0631\u0628\u064A";
    }

    /**
     * Localized display name for a role string.
     */
    public static String getRoleDisplay(String role) {
        if (role == null || role.isBlank()) return isArabic() ? "طالب" : "Student";
        String r = role.trim().toUpperCase();
        if (r.contains("ADMIN")) return isArabic() ? "مسؤول" : "Administrator";
        if (r.contains("TEACH") || r.contains("DOCTOR") || r.contains("PROF")) return isArabic() ? "دكتور / أستاذ" : "Doctor / Teacher";
        return isArabic() ? "طالب" : "Student";
    }
}