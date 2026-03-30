package com.springmart.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 商品登録・更新 API のリクエストボディ。
 * Jackson はデフォルトで setter 経由でフィールドに値を入れるため、setter を明示する。
 */
public class ProductRequest {
    @NotBlank(message = "商品名は必須です")
    private String name;

    private String description;

    @NotNull(message = "価格は必須です")
    @Min(value = 0, message = "価格は0以上である必要があります")
    private Integer price;

    @Min(value = 0, message = "初期在庫数は0以上である必要があります")
    private Integer initialStock;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Integer getPrice() {
        return price;
    }

    public Integer getInitialStock() {
        return initialStock;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public void setInitialStock(Integer initialStock) {
        this.initialStock = initialStock;
    }
}
