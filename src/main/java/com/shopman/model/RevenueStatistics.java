package com.shopman.model;

import com.shopman.dao.DatabaseConnection;
import com.shopman.dao.RevenueStatisticsDAO;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

/**
 * RevenueStatistics: revenue per product between two dates (use case "Xem thống kê mặt hàng theo doanh thu").
 * Nothing is stored: figures are computed from invoices (report, Phần II Bước 5).
 */
public class RevenueStatistics {

    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalRevenue = BigDecimal.ZERO;
    private List<ProductRevenue> items = Collections.emptyList();

    /**
     * Report step 7-18: loads the product list (highest revenue first) and the total for the period.
     * Both queries share one connection, so they see the same data.
     *
     * @throws IllegalArgumentException when a date is missing or the end date is before the start date
     */
    public List<ProductRevenue> getProductRevenueStatistics(LocalDate startDate, LocalDate endDate)
            throws SQLException {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Vui lòng chọn ngày bắt đầu và ngày kết thúc");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Ngày kết thúc phải sau hoặc trùng ngày bắt đầu");
        }
        this.startDate = startDate;
        this.endDate = endDate;
        try (Connection connection = DatabaseConnection.getConnection()) {
            RevenueStatisticsDAO dao = new RevenueStatisticsDAO(connection);
            this.items = dao.getRevenueByProduct(startDate, endDate);
            this.totalRevenue = dao.getTotalRevenue(startDate, endDate);
        }
        return items;
    }

    /** Total revenue of the period, as computed by {@link #getProductRevenueStatistics}. */
    public BigDecimal calculateTotalRevenue() {
        return totalRevenue;
    }

    public int getTotalQuantity() {
        return items.stream().mapToInt(ProductRevenue::getQuantitySold).sum();
    }

    public LocalDate getStartDate() { return startDate; }
    public LocalDate getEndDate() { return endDate; }
    public BigDecimal getTotalRevenue() { return totalRevenue; }
    public List<ProductRevenue> getItems() { return items; }
}
