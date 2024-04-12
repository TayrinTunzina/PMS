package com.example.pms;

public class UserService {
    private static String loggedInUserId;
    private static String loggedInUserRole; // Add this field

    public static String getLoggedInUserId() {
        return loggedInUserId;
    }

    public static void setLoggedInUserId(String userId) {
        loggedInUserId = userId;
    }

    // Add this method
    public static String getLoggedInUserRole() {
        return loggedInUserRole;
    }

    // Add this method
    public static void setLoggedInUserRole(String userRole) {
        loggedInUserRole = userRole;
    }
}
