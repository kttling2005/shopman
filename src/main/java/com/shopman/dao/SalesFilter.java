package com.shopman.dao;

import com.shopman.model.InvoiceStatus;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;

/**
 * SQL shared by the revenue queries. A sale counts when the invoice's LATEST status row is
 * {@link InvoiceStatus#COUNTED_AS_REVENUE} and its order date lies in [start 00:00, end + 1 day 00:00).
 * The half-open range makes the end date inclusive even for orders placed at 23:59.
 */
final class SalesFilter {

    /** Needs the invoice aliased as {@code i}. Parameters: from, toExclusive, status. */
    static final String WHERE_SOLD_IN_PERIOD =
            " i.orderDate >= ? AND i.orderDate < ? AND ("
            + "SELECT s.status FROM tblStatusHistory s WHERE s.invoiceId = i.invoiceId "
            + "ORDER BY s.updateTime DESC, s.id DESC LIMIT 1) = ? ";

    private SalesFilter() {
    }

    /** Binds the three period parameters starting at {@code index}; returns the next free index. */
    static int bindPeriod(PreparedStatement ps, int index, LocalDate start, LocalDate end) throws SQLException {
        ps.setTimestamp(index++, Timestamp.valueOf(start.atStartOfDay()));
        ps.setTimestamp(index++, Timestamp.valueOf(end.plusDays(1).atStartOfDay()));
        ps.setString(index++, InvoiceStatus.COUNTED_AS_REVENUE);
        return index;
    }
}
