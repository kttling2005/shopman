package com.shopman.dao;

import com.shopman.model.Customer;
import com.shopman.model.Invoice;
import com.shopman.model.InvoiceDetail;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** tblInvoiceDetail queries. */
public class InvoiceDetailDAO {

    private final Connection connection;

    public InvoiceDetailDAO(Connection connection) {
        this.connection = connection;
    }

    /** Sales lines of one product in the period, oldest first. Same sale rule as the revenue list. */
    public List<InvoiceDetail> getTransactionsByProduct(int productId, LocalDate startDate, LocalDate endDate)
            throws SQLException {
        String sql = "SELECT d.id, d.quantity, d.unitPrice, d.subtotal, "
                + "i.invoiceId, i.orderDate, i.shippingAddress, i.paymentMethod, i.totalAmount, c.customerId "
                + "FROM tblInvoiceDetail d "
                + "JOIN tblInvoice i ON i.invoiceId = d.invoiceId "
                + "JOIN tblCustomer c ON c.id = i.customerId "
                + "WHERE d.productId = ? AND" + SalesFilter.WHERE_SOLD_IN_PERIOD
                + "ORDER BY i.orderDate ASC, i.invoiceId ASC, d.id ASC";
        List<InvoiceDetail> result = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, productId);
            SalesFilter.bindPeriod(ps, 2, startDate, endDate);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Customer customer = new Customer();
                    customer.setCustomerId(rs.getString("customerId"));

                    Invoice invoice = new Invoice();
                    invoice.setId(rs.getInt("invoiceId"));
                    invoice.setOrderDate(rs.getTimestamp("orderDate").toLocalDateTime());
                    invoice.setShippingAddress(rs.getString("shippingAddress"));
                    invoice.setPaymentMethod(rs.getString("paymentMethod"));
                    invoice.setTotalAmount(rs.getBigDecimal("totalAmount"));
                    invoice.setCustomer(customer);

                    InvoiceDetail detail = new InvoiceDetail();
                    detail.setId(rs.getInt("id"));
                    detail.setQuantity(rs.getInt("quantity"));
                    detail.setUnitPrice(rs.getBigDecimal("unitPrice"));
                    detail.setSubtotal(rs.getBigDecimal("subtotal"));
                    detail.setInvoice(invoice);
                    result.add(detail);
                }
            }
        }
        return result;
    }
}
