package com.shopman.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Invoice (tblInvoice) = online order. Only the fields the statistics screens need are filled by the DAOs. */
public class Invoice {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("d/M/yyyy");

    private int id;
    private LocalDateTime orderDate;
    private String shippingAddress;
    private String paymentMethod;
    private BigDecimal totalAmount;
    private Customer customer;

    /** Business code shown in the UI, e.g. id 1 -> "HD01". */
    public String getCode() {
        return String.format("HD%02d", id);
    }

    /** Date as written in the report, e.g. 1/2/2026. */
    public String getOrderDateText() {
        return orderDate == null ? "" : orderDate.format(DATE);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }
    public String getShippingAddress() { return shippingAddress; }
    public void setShippingAddress(String shippingAddress) { this.shippingAddress = shippingAddress; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
}
