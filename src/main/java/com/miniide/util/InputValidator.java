package com.miniide.util;

public class InputValidator {

    public static boolean validProjectName(String name) {
        return name.matches("[A-Za-z0-9 ]+");
    }

    public static boolean validPackageName(String name) {
        return name.matches("^(?=.*[A-Za-z])[A-Za-z0-9_]+(\\.[A-Za-z0-9_]+)*$");
    }

    public static boolean validJavaFileName(String name) {
        return name.matches("^[A-Z][A-Za-z0-9]*$");
    }

}