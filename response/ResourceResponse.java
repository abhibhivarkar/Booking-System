package com.example.booking.response;

public class ResourceResponse {
    private String message;
    private Object data;

    public ResourceResponse() {}

    public ResourceResponse(String message, Object data) {
        this.message = message;
        this.data = data;
    }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
}
