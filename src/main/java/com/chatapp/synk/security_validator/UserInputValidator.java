package com.chatapp.synk.security_validator;

import java.util.regex.Pattern;

public class UserInputValidator {
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+]?[0-9]{10,15}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern ALIAS_PATTERN = Pattern.compile("^[A-Za-z0-9_-]{1,30}$");
    // Validates that the string consists only of digits and is between 15 and 20
    // characters long
    private static final Pattern SNOWFLAKE_PATTERN = Pattern.compile("^\\d{15,20}$");

    public static boolean isValidLoginId(String loginId) {
        if (loginId == null || loginId.isEmpty())
            return false;
        String trimmed = loginId.trim();
        return PHONE_PATTERN.matcher(trimmed).matches() || EMAIL_PATTERN.matcher(trimmed).matches();
    }

    public static boolean isValidId(String id) {
        if (id == null || id.isEmpty())
            return false;

        // 1. Fast format check using regex
        if (!SNOWFLAKE_PATTERN.matcher(id).matches())
            return false;

        // 2. Range check to ensure it fits perfectly within a 64-bit signed Long
        try {
            long numericId = Long.parseLong(id);

            // Snowflake IDs must be positive integers (high bit is always 0)
            return numericId > 0;
        } catch (NumberFormatException e) {
            return false; // Fails if the number exceeds Long.MAX_VALUE (9223372036854775807)
        }
    }

    public static boolean isValidPhoneNumber(String input) {
        return input.matches("^\\d{10}$"); // Adjust pattern if needed
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}
