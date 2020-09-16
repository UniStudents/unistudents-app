package com.unipi.students.common;

public class StringHelper {

    public static String removeTones(String string) {
        string = string.replace("Ά", "Α");
        string = string.replace("Έ", "Ε");
        string = string.replace("Ή", "Η");
        string = string.replace("Ί", "Ι");
        string = string.replace("Ό", "Ο");
        string = string.replace("Ύ", "Υ");
        string = string.replace("Ώ", "Ω");
        return string;
    }
}
