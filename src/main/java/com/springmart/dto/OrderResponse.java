package com.springmart.dto;

public class OrderResponse {
    private Long orderId;
    private String status;
    private Integer totalPrice;

    public OrderResponse() {
    }

    public OrderResponse(Long orderId, String status, Integer totalPrice) {
        this.orderId = orderId;
        this.status = status;
        this.totalPrice = totalPrice;
    }

    public Long getOrderId() {
        return orderId;
    }

    public String getStatus() {
        return status;
    }

    public Integer getTotalPrice() {
        return totalPrice;
    }
}
