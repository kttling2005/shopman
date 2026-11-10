package com.shopman.model;

import java.math.BigDecimal;

/** Product (tblProduct). */
public class Product {

    private int id;
    private String name;
    private String description;
    private BigDecimal unitPrice;
    private int stockQuantity;
    private String image;

    /** Business code shown in the UI, e.g. id 1 -> "HH01". */
    public String getCode() {
        return String.format("HH%02d", id);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
}
