package com.github.seregamorph.jackson.partial;

class Validate {

    public static void isTrue(boolean expression, String message, Object... values) {
        if (!expression) {
            throw new IllegalArgumentException(String.format(message, values));
        }
    }

    private Validate() {
    }
}
