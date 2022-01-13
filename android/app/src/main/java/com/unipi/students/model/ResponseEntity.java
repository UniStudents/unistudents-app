package com.unipi.students.model;

import com.unipi.students.common.HttpStatus;

public class ResponseEntity {

    private Object object;
    private HttpStatus httpStatus;

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

    public HttpStatus getStatusCode() {
        return httpStatus;
    }
}
