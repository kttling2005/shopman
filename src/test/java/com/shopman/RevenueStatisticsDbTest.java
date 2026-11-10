package com.shopman;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.shopman.model.InvoiceDetail;
import com.shopman.model.ProductRevenue;
import com.shopman.model.RevenueStatistics;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Fixture (period under test = 2026-01-01 .. 2026-04-30). "counted" = latest status is delivered.
 *   P1 price 100, P2 price 50, P3 price 50, customers KH01 / KH02
 *   i1  2026-01-01 00:00:00  delivered           P1 x2          counted (start day, first second)
 *   i2  2026-04-30 23:59:59  delivered           P2 x4, P3 x4   counted (end day, last second)
 *   i3  2026-02-10 12:00:00  delivered           P1 x3          counted
 *   i4  2025-12-31 23:59:59  delivered           P1 x10         before the period
 *   i5  2026-05-01 00:00:00  delivered           P1 x10         after the period
 *   i6  2026-03-01           pending only        P1 x10         not sold yet
 *   i7  2026-03-02           ... then cancelled  P1 x10         sold but cancelled afterwards
 *   i8  2026-03-05           history rows inserted newest-first; latest by time is delivered, P2 x1  counted
 */
class RevenueStatisticsDbTest extends DbTestBase {

    private static final LocalDate START = LocalDate.of(2026, 1, 1);
    private static final LocalDate END = LocalDate.of(2026, 4, 30);

    @BeforeEach
    void fixture() throws Exception {
        insertCustomer(1, "KH01");
        insertCustomer(2, "KH02");
        insertProduct(1, "Lò nướng ABC", "100");
        insertProduct(2, "Bánh quy XYZ", "50");
        insertProduct(3, "Gạo ST25", "50");

        insertInvoice(1, 1, LocalDateTime.parse("2026-01-01T00:00:00"), delivered("2026-01-01T00:00:00"), new Line(1, 2));
        insertInvoice(2, 2, LocalDateTime.parse("2026-04-30T23:59:59"), delivered("2026-04-30T23:59:59"), new Line(2, 4), new Line(3, 4));
        insertInvoice(3, 2, LocalDateTime.parse("2026-02-10T12:00:00"), delivered("2026-02-10T12:00:00"), new Line(1, 3));
        insertInvoice(4, 1, LocalDateTime.parse("2025-12-31T23:59:59"), delivered("2025-12-31T23:59:59"), new Line(1, 10));
        insertInvoice(5, 1, LocalDateTime.parse("2026-05-01T00:00:00"), delivered("2026-05-01T00:00:00"), new Line(1, 10));
        insertInvoice(6, 1, LocalDateTime.parse("2026-03-01T09:00:00"), new String[][] {{"pending", "2026-03-01T09:00:00"}}, new Line(1, 10));
        insertInvoice(7, 1, LocalDateTime.parse("2026-03-02T09:00:00"),
                new String[][] {{"pending", "2026-03-02T09:00:00"}, {"delivered", "2026-03-03T09:00:00"}, {"cancelled", "2026-03-04T09:00:00"}},
                new Line(1, 10));
        insertInvoice(8, 2, LocalDateTime.parse("2026-03-05T10:00:00"),
                new String[][] {{"delivered", "2026-03-06T10:00:00"}, {"pending", "2026-03-05T10:00:00"}}, new Line(2, 1));
    }

    @Test
    void listsProductsByRevenueDescendingWithTiesByCode() throws Exception {
        RevenueStatistics stats = new RevenueStatistics();
        List<ProductRevenue> items = stats.getProductRevenueStatistics(START, END);

        assertEquals(3, items.size());
        assertRow(items.get(0), "HH01", 5, "500");   // P1: i1 (2) + i3 (3)
        assertRow(items.get(1), "HH02", 5, "250");   // P2: i2 (4) + i8 (1)
        assertRow(items.get(2), "HH03", 4, "200");   // P3: i2 (4)
    }

    @Test
    void totalRevenueEqualsSumOfRows() throws Exception {
        RevenueStatistics stats = new RevenueStatistics();
        List<ProductRevenue> items = stats.getProductRevenueStatistics(START, END);

        BigDecimal sum = items.stream().map(ProductRevenue::getRevenue).reduce(BigDecimal.ZERO, BigDecimal::add);
        assertEquals(0, new BigDecimal("950").compareTo(stats.calculateTotalRevenue()));
        assertEquals(0, sum.compareTo(stats.calculateTotalRevenue()));
        assertEquals(14, stats.getTotalQuantity());
    }

    @Test
    void boundaryDaysAreInclusiveAndOutsideDaysExcluded() throws Exception {
        // Only the end day: i2 (23:59:59) is in, i5 (the next midnight) is out.
        List<ProductRevenue> lastDay = new RevenueStatistics().getProductRevenueStatistics(END, END);
        assertEquals(2, lastDay.size());
        assertRow(lastDay.get(0), "HH02", 4, "200");
        assertRow(lastDay.get(1), "HH03", 4, "200");

        // Only the start day: i1 (00:00:00) is in, i4 (the previous 23:59:59) is out.
        List<ProductRevenue> firstDay = new RevenueStatistics().getProductRevenueStatistics(START, START);
        assertEquals(1, firstDay.size());
        assertRow(firstDay.get(0), "HH01", 2, "200");
    }

    @Test
    void pendingAndLaterCancelledInvoicesAreNotRevenue() throws Exception {
        // i6 (pending) and i7 (cancelled) each hold 10 x P1; if they leaked in, P1 would be 1500+.
        List<ProductRevenue> items = new RevenueStatistics().getProductRevenueStatistics(
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 4));
        assertTrue(items.isEmpty());
    }

    @Test
    void latestStatusIsChosenByTimeNotByInsertOrder() throws Exception {
        List<ProductRevenue> items = new RevenueStatistics().getProductRevenueStatistics(
                LocalDate.of(2026, 3, 5), LocalDate.of(2026, 3, 5));
        assertEquals(1, items.size());
        assertRow(items.get(0), "HH02", 1, "50");
    }

    @Test
    void periodWithoutSalesGivesEmptyListAndZeroTotal() throws Exception { // exception 6
        RevenueStatistics stats = new RevenueStatistics();
        assertTrue(stats.getProductRevenueStatistics(LocalDate.of(2027, 1, 1), LocalDate.of(2027, 1, 31)).isEmpty());
        assertEquals(0, BigDecimal.ZERO.compareTo(stats.calculateTotalRevenue()));
    }

    @Test
    void rejectsMissingOrReversedDates() {
        RevenueStatistics stats = new RevenueStatistics();
        assertThrows(IllegalArgumentException.class, () -> stats.getProductRevenueStatistics(null, END));
        assertThrows(IllegalArgumentException.class, () -> stats.getProductRevenueStatistics(START, null));
        assertThrows(IllegalArgumentException.class, () -> stats.getProductRevenueStatistics(END, START));
    }

    @Test
    void transactionsOfAProductAreOldestFirstWithInvoiceAndCustomerCodes() throws Exception {
        List<InvoiceDetail> rows = new InvoiceDetail().getProductTransaction(1, START, END);

        assertEquals(2, rows.size()); // i4, i5, i6, i7 are excluded
        assertEquals("HD01", rows.get(0).getInvoice().getCode());
        assertEquals("1/1/2026", rows.get(0).getInvoice().getOrderDateText());
        assertEquals("KH01", rows.get(0).getInvoice().getCustomer().getCustomerId());
        assertEquals(2, rows.get(0).getQuantity());
        assertEquals(0, new BigDecimal("200").compareTo(rows.get(0).getSubtotal()));

        assertEquals("HD03", rows.get(1).getInvoice().getCode());
        assertEquals("10/2/2026", rows.get(1).getInvoice().getOrderDateText());
        assertEquals("KH02", rows.get(1).getInvoice().getCustomer().getCustomerId());
        assertEquals(3, rows.get(1).getQuantity());
    }

    @Test
    void transactionTotalsMatchTheRevenueRowOfTheSameProduct() throws Exception {
        for (ProductRevenue row : new RevenueStatistics().getProductRevenueStatistics(START, END)) {
            List<InvoiceDetail> rows = new InvoiceDetail().getProductTransaction(row.getProduct().getId(), START, END);
            assertEquals(row.getQuantitySold(), rows.stream().mapToInt(InvoiceDetail::getQuantity).sum());
            BigDecimal sum = rows.stream().map(InvoiceDetail::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
            assertEquals(0, row.getRevenue().compareTo(sum));
        }
    }

    @Test
    void transactionsForUnknownProductAreEmpty() throws Exception {
        assertTrue(new InvoiceDetail().getProductTransaction(999, START, END).isEmpty());
    }

    private static void assertRow(ProductRevenue row, String code, int quantity, String revenue) {
        assertEquals(code, row.getProduct().getCode());
        assertEquals(quantity, row.getQuantitySold());
        assertEquals(0, new BigDecimal(revenue).compareTo(row.getRevenue()),
                "revenue of " + code + " was " + row.getRevenue());
    }
}
