package com.example.booking.dto;

import java.math.BigDecimal;

public class ReservationRequest {
    private Long resourceId;
    private BigDecimal price;
    private String startTime; // ISO-8601 string - parse to Instant in service
    private String endTime;
    private String status; // optional, ADMIN can set, default PENDING

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
