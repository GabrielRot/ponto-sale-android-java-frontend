package com.example.pontosale.utils;

public class Text {

    public static String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;

        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

}
