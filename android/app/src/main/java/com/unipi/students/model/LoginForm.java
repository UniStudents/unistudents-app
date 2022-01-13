package com.unipi.students.model;

import java.util.Map;

public class LoginForm {

    private String username;
    private String password;
    private Map<String, String> cookies;

    public LoginForm(String username, String password, Map<String, String> cookies) {
        this.username = username;
        this.password = password;
        this.cookies = cookies;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Map<String, String> getCookies() {
        return cookies;
    }

    public void setCookies(Map<String, String> cookies) {
        this.cookies = cookies;
    }

    @Override
    public String toString() {
        return "LoginForm{" +
                "username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", cookies=" + cookies +
                '}';
    }
}