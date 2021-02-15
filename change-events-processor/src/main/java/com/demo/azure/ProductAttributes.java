package com.demo.azure;

import java.util.List;

public class ProductAttributes {
    private String description;
    private String brand;
    private List<String> tags;
    private List<String> categories;

    public String getDescriptionn() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

    @Override
    public String toString() {
        return "ProductAttributes [brand=" + brand + ", categories=" + categories + ", description=" + description
                + ", tags=" + tags + "]";
    }

}