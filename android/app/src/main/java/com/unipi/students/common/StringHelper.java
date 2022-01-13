package com.unipi.students.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

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

    public static String getRandomHashcode() {
        try {
            String text;
            int targetStringLength = 64;
            Random random = new Random();
            StringBuilder buffer = new StringBuilder(targetStringLength);
            for (int i = 0; i < targetStringLength; i++) {
                int randomLimitedInt = 97 + (int) (random.nextFloat() * (122 - 97 + 1));
                buffer.append((char) randomLimitedInt);
            }
            text = buffer.toString();

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(text.getBytes(StandardCharsets.UTF_8));
            String hexStr = "";
            for (byte b : digest) {
                hexStr += Integer.toString((b & 0xff) + 0x100, 16).substring(1);
            }
            return hexStr;
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
}
