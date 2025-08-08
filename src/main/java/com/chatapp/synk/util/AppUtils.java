package com.chatapp.synk.util;

public class AppUtils {
    private AppUtils() {
        // Private constructor to prevent instantiation
    }

    public static boolean isValidPhoneNumber(String input) {
        return input.matches("^\\d{10}$"); // Adjust pattern if needed
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

}
