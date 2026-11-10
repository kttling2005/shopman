package com.shopman.dao;

import com.shopman.model.Product;
import com.shopman.model.ProductRevenue;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Revenue queries for the statistics module. */
public class RevenueStatisticsDAO {

    private final Connection connection;

    public RevenueStatisticsDAO(Connection connection) {
        this.connection = connection;
    }

    /** Products sold in the period with quantity and revenue, highest revenue first (ties: lower id first). */
    public List<ProductRevenue> getRevenueByProduct(LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT p.id, p.name, p.description, p.unitPrice, p.stockQuantity, "
                + "SUM(d.quantity) AS qty, SUM(d.subtotal) AS revenue "
                + "FROM tblInvoiceDetail d "
                + "JOIN tblInvoice i ON i.invoiceId = d.invoiceId "
                + "JOIN tblProduct p ON p.id = d.productId "
                + "WHERE" + SalesFilter.WHERE_SOLD_IN_PERIOD
                + "GROUP BY p.id, p.name, p.description, p.unitPrice, p.stockQuantity "
                + "ORDER BY revenue DESC, p.id ASC";
        List<ProductRevenue> result = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            SalesFilter.bindPeriod(ps, 1, startDate, endDate);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Product p = new Product();
                    p.setId(rs.getInt("id"));
                    p.setName(rs.getString("name"));
                    p.setDescription(rs.getString("description"));
                    p.setUnitPrice(rs.getBigDecimal("unitPrice"));
                    p.setStockQuantity(rs.getInt("stockQuantity"));
                    result.add(new ProductRevenue(p, rs.getInt("qty"), rs.getBigDecimal("revenue")));
                }
            }
        }
        return result;
    }

    /** Sum of all line subtotals sold in the period; zero when there are no sales. */
    public BigDecimal getTotalRevenue(LocalDate startDate, LocalDate endDate) throws SQLException {
        String sql = "SELECT COALESCE(SUM(d.subtotal), 0) AS total "
                + "FROM tblInvoiceDetail d JOIN tblInvoice i ON i.invoiceId = d.invoiceId "
                + "WHERE" + SalesFilter.WHERE_SOLD_IN_PERIOD;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            SalesFilter.bindPeriod(ps, 1, startDate, endDate);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getBigDecimal("total") : BigDecimal.ZERO;
            }
        }
    }
}
