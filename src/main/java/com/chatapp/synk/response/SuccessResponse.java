package com.chatapp.synk.response;

import java.util.List;

public class SuccessResponse<T> {

    private String responseCode;
    private String message;
    private List<T> data;

    public SuccessResponse(String responseCode, String message, List<T> data) {
        this.responseCode = responseCode;
        this.message = message;
        this.data = data;
    }

    public SuccessResponse(String responseCode, String message) {
        this.responseCode = responseCode;
        this.message = message;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
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
