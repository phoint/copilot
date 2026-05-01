package edu.ecommerce.core.util;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

public class StringUtilsTest {

    @Test
    public void testIsPalindrome_NullString() {
        assertThat(StringUtils.isPalindrome(null)).isFalse();
    }

    @Test
    public void testIsPalindrome_EmptyString() {
        assertThat(StringUtils.isPalindrome("")).isTrue();
    }

    @Test
    public void testIsPalindrome_SingleCharacter() {
        assertThat(StringUtils.isPalindrome("a")).isTrue();
        assertThat(StringUtils.isPalindrome("A")).isTrue();
    }

    @Test
    public void testIsPalindrome_SimplePalindrome() {
        assertThat(StringUtils.isPalindrome("aba")).isTrue();
        assertThat(StringUtils.isPalindrome("Aba")).isTrue();
        assertThat(StringUtils.isPalindrome("ABA")).isTrue();
    }

    @Test
    public void testIsPalindrome_SimpleNonPalindrome() {
        assertThat(StringUtils.isPalindrome("abc")).isFalse();
        assertThat(StringUtils.isPalindrome("hello")).isFalse();
    }

    @Test
    public void testIsPalindrome_WithSpacesAndPunctuation() {
        assertThat(StringUtils.isPalindrome("A man, a plan, a canal: Panama")).isTrue();
        assertThat(StringUtils.isPalindrome("race a car")).isFalse();
    }

    @Test
    public void testIsPalindrome_WithNumbers() {
        assertThat(StringUtils.isPalindrome("12321")).isTrue();
        assertThat(StringUtils.isPalindrome("12345")).isFalse();
    }

    @Test
    public void testIsPalindrome_IgnoreCase() {
        assertThat(StringUtils.isPalindrome("Racecar")).isTrue();
        assertThat(StringUtils.isPalindrome("RaCeCaR")).isTrue();
    }

    @Test
    public void testIsPalindrome_WithSpecialCharacters() {
        assertThat(StringUtils.isPalindrome("A!b@a")).isTrue();
        assertThat(StringUtils.isPalindrome("A!b@c")).isFalse();
    }
}
