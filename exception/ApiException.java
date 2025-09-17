package com.example.booking.exception;

public class ApiException {
    private String message;
    public ApiException(){}
    public ApiException(String message){ this.message = message; }
    public String getMessage(){ return message; }
    public void setMessage(String message){ this.message = message; }
}
