package com.springmart.dto;

public class ProductResponse {
    private Long id;
    private String name;
    private String description;
    private Integer price;

    public ProductResponse() {
    }

    public ProductResponse(Long id, String name, String description, Integer price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Integer getPrice() {
        return price;
    }
}
