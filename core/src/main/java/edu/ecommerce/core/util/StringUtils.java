package edu.ecommerce.core.util;

/**
 * Utility class for string operations.
 */
public final class StringUtils {

    /**
     * Private constructor to prevent instantiation.
     */
    private StringUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Checks if a string is a palindrome, ignoring case and non-alphanumeric characters.
     *
     * @param str the string to check
     * @return true if the string is a palindrome, false otherwise
     */
    public static boolean isPalindrome(String str) {
        if (str == null) {
            return false;
        }

        // Convert to lowercase and remove non-alphanumeric characters
        String cleaned = str.toLowerCase().replaceAll("[^a-z0-9]", "");

        // Check if the cleaned string equals its reverse
        String reversed = new StringBuilder(cleaned).reverse().toString();
        return cleaned.equals(reversed);
    }
}
