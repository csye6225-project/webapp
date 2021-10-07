package com.example.demo.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EmailValidator {
    public static boolean isEmail(String s) {
        if (s == null) {
            return false;
        }
        String regEx = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(s);
        return m.matches();
    }
}
