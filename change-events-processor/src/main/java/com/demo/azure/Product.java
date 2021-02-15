package com.demo.azure;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "product_id", "product_name", "created_at", "product_details" })
public class Product {

    @JsonProperty("product_id")
    private Integer productId;

    @JsonProperty("product_name")
    private String productName;

    @JsonProperty("created_at")
    private Timestamp createdAt;

    @JsonProperty("product_details")
    private String productDetails;

    @JsonProperty("product_id")
    public Integer getProductId() {
        return productId;
    }

    @JsonProperty("product_id")
    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    @JsonProperty("product_name")
    public String getProductName() {
        return productName;
    }

    @JsonProperty("product_name")
    public void setProductName(String productName) {
        this.productName = productName;
    }

    @JsonProperty("created_at")
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    @JsonProperty("created_at")
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @JsonProperty("product_details")
    public String getProductDetails() {
        return productDetails;
    }

    @JsonProperty("product_details")
    public void setProductDetails(String productDetails) {
        this.productDetails = productDetails;
    }

    @Override
    public String toString() {
        return "Product [createdAt=" + createdAt + ", productDetails=" + productDetails + ", productId=" + productId
                + ", productName=" + productName + "]";
    }
}