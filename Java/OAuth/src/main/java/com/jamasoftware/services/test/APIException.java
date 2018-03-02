package com.jamasoftware.services.test;

public class APIException extends Exception {
    private int statusCode;
    private String responseMessage;

    public APIException(int statusCode, String responseMessage) {
        this.statusCode = statusCode;
        this.responseMessage = responseMessage;
    }

    @Override
    public String getMessage() {
        return "Error response from Jama API: \n" + responseMessage;
    }

    @Override
    public String toString() { return getStatusCode() + ": " + getMessage(); }

    private int getStatusCode() {
        return statusCode;
    }
}
