package com.unipi.students.model;

import com.unipi.students.common.HttpStatus;

public class ResponseEntity {

    private final Object object; // It doesnt have setters, so I can use it as a final variable??
    private final HttpStatus httpStatus; // It doesnt have setters, so I can use it as a final variable??

    public ResponseEntity(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public ResponseEntity(Object object, HttpStatus httpStatus) {
        this.object = object;
        this.httpStatus = httpStatus;
    }

    public Object getObject() {
        return object;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
