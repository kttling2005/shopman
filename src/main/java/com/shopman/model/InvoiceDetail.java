package com.shopman.model;

import com.shopman.dao.DatabaseConnection;
import com.shopman.dao.InvoiceDetailDAO;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/** InvoiceDetail (tblInvoiceDetail): one product line of an invoice. */
public class InvoiceDetail {

    private int id;
    private int quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private Invoice invoice;
    private Product product;

    /**
     * Report step 23-30: the sales lines of one product between two dates, oldest first
     * (use case "Xem các giao dịch của mặt hàng").
     */
    public List<InvoiceDetail> getProductTransaction(int productId, LocalDate startDate, LocalDate endDate)
            throws SQLException {
        if (startDate == null || endDate == null || endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Khoảng thời gian không hợp lệ");
        }
        try (Connection connection = DatabaseConnection.getConnection()) {
            return new InvoiceDetailDAO(connection).getTransactionsByProduct(productId, startDate, endDate);
        }
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
    public Invoice getInvoice() { return invoice; }
    public void setInvoice(Invoice invoice) { this.invoice = invoice; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
}
