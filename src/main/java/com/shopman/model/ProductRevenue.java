package com.shopman.model;

import java.math.BigDecimal;

/** One row of the revenue list: a product with the quantity sold and revenue in the chosen period. */
public class ProductRevenue {

    private final Product product;
    private final int quantitySold;
    private final BigDecimal revenue;

    public ProductRevenue(Product product, int quantitySold, BigDecimal revenue) {
        this.product = product;
        this.quantitySold = quantitySold;
        this.revenue = revenue;
    }

    public Product getProduct() { return product; }
    public int getQuantitySold() { return quantitySold; }
    public BigDecimal getRevenue() { return revenue; }
}
