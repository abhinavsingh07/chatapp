package com.chatapp.synk.response;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

public class SuccessResponse<T> {

    private HttpStatus responseCode;
    private String message;
    private List<T> data;

    public SuccessResponse(HttpStatus responseCode, String message, List<T> data) {
        this.responseCode = responseCode;
        this.message = message;
        this.data = data;
    }

    public SuccessResponse(HttpStatus responseCode, String message) {
        this.responseCode = responseCode;
        this.message = message;
    }

    /** Returns 200 OK with the given body. */
    public static <T> ResponseEntity<SuccessResponse<T>> ok(HttpStatus code, String msg, List<T> data) {
        return ResponseEntity.ok(new SuccessResponse<>(code, msg, data));
    }

    /** Returns 200 OK with an empty data list. */
    public static <T> ResponseEntity<SuccessResponse<T>> ok(HttpStatus code, String msg) {
        return ResponseEntity.ok(new SuccessResponse<>(code, msg, Collections.emptyList()));
    }

    /** Returns the given HTTP status with the given body. */
    public static <T> ResponseEntity<SuccessResponse<T>> of(HttpStatus httpCode, HttpStatus bodyCode, String msg) {
        return ResponseEntity.status(httpCode)
                .body(new SuccessResponse<>(bodyCode, msg, Collections.emptyList()));
    }

    public HttpStatus getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(HttpStatus responseCode) {
        this.responseCode = responseCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }
}
