package com.unipi.students.common;

public enum HttpStatus {
    OK(200, "OK"),
    UNAUTHORIZED(401, "Unauthorized"),
    NOT_FOUND(404, "Not Found"),
    REQUEST_TIMEOUT(408, "Request Timeout"),
    INTERNAL_SERVER_ERROR(500, "Internal Server Error");

    private final int value;
    private final String reasonPhrase;

    private HttpStatus(int value, String reasonPhrase) {
        this.value = value;
        this.reasonPhrase = reasonPhrase;
    }

    public int value() {
        return this.value;
    }

    public String getReasonPhrase() {
        return this.reasonPhrase;
    }

    public String toString() {
        return this.value + " " + this.name();
    }
}
