package com.unipi.students.common;

import android.util.Base64;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.UnsupportedEncodingException;

public class JWTUtils {

    public static String[] decoded(String JWTEncoded) {
        try {
            String[] split = JWTEncoded.split("\\.");

            JsonNode node = new ObjectMapper().readTree(getJson(split[1]));
            String department = node.get("roles").get(0).get("authorizations").get(0).get("department").asText();
            String category = node.get("roles").get(0).get("authorizations").get(0).get("category").asText();

            return new String[]{department, category};
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String getJson(String strEncoded) throws UnsupportedEncodingException {
        byte[] decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE);
        return new String(decodedBytes, "UTF-8");
    }
}
