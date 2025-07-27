package com.chatapp.synk.util;

public class AppUtils {

    public static boolean isValidEmail(String input) {
        return input.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    public static boolean isValidPhoneNumber(String input) {
        return input.matches("^\\d{10}$"); // Adjust pattern if needed
    }
}
