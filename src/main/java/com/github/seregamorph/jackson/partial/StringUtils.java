package com.github.seregamorph.jackson.partial;

/**
 * Copied from apache commons and spring core
 */
class StringUtils {

    static boolean isBlank(CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    static boolean isNotBlank(final CharSequence cs) {
        return !isBlank(cs);
    }

    static String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }

        char baseChar = str.charAt(0);
        char updatedChar = Character.toUpperCase(baseChar);
        if (baseChar == updatedChar) {
            return str;
        }

        char[] chars = str.toCharArray();
        chars[0] = updatedChar;
        return new String(chars, 0, chars.length);
    }

    private StringUtils() {
    }
}
