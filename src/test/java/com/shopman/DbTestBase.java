package com.shopman;

import com.shopman.dao.DatabaseConnection;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;

/**
 * Base class for tests that need MySQL. They run only when SHOPMAN_TEST_DB_URL is set, e.g.
 *   SHOPMAN_TEST_DB_URL="jdbc:mysql://localhost:3306/shopman_test?useUnicode=true&amp;characterEncoding=UTF-8"
 *   SHOPMAN_TEST_DB_USER=root  SHOPMAN_TEST_DB_PASSWORD=
 * The database must already exist and is wiped and rebuilt from database/schema.sql before every test,
 * so NEVER point it at your real "shopman" database.
 */
abstract class DbTestBase {

    private static final String[] TABLES = {
        "tblGoodsReceiptDetail", "tblGoodsReceipt", "tblStatusHistory", "tblInvoiceDetail", "tblInvoice",
        "tblProduct", "tblCategory", "tblSupplier", "tblManager", "tblSalesEmployee", "tblDeliveryEmployee",
        "tblEmployee", "tblCustomer", "tblMember"
    };

    @BeforeEach
    void rebuildDatabase() throws Exception {
        String url = System.getenv("SHOPMAN_TEST_DB_URL");
        Assumptions.assumeTrue(url != null && !url.isBlank(), "SHOPMAN_TEST_DB_URL not set - skipping database tests");
        String user = System.getenv().getOrDefault("SHOPMAN_TEST_DB_USER", "root");
        String password = System.getenv().getOrDefault("SHOPMAN_TEST_DB_PASSWORD", "");
        DatabaseConnection.configure(url, user, password);

        try (Connection c = DatabaseConnection.getConnection(); Statement st = c.createStatement()) {
            st.execute("SET FOREIGN_KEY_CHECKS = 0");
            for (String table : TABLES) {
                st.execute("DROP TABLE IF EXISTS " + table);
            }
            st.execute("SET FOREIGN_KEY_CHECKS = 1");
            for (String sql : schemaStatements()) {
                st.execute(sql);
            }
        }
    }

    private static String[] schemaStatements() throws IOException {
        StringBuilder sb = new StringBuilder();
        for (String line : Files.readAllLines(Path.of("database", "schema.sql"), StandardCharsets.UTF_8)) {
            if (!line.trim().startsWith("--")) {
                sb.append(line).append('\n');
            }
        }
        return java.util.Arrays.stream(sb.toString().split(";"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .filter(s -> !s.toUpperCase().startsWith("CREATE DATABASE") && !s.toUpperCase().startsWith("USE "))
                .toArray(String[]::new);
    }

    // ---------- fixtures ----------

    protected static void insertCustomer(int id, String code) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO tblMember (id, username, password, name, email) VALUES (?, ?, 'x', ?, ?)")) {
                ps.setInt(1, id);
                ps.setString(2, "user" + id);
                ps.setString(3, "Khách " + id);
                ps.setString(4, "user" + id + "@example.com");
                ps.executeUpdate();
            }
            try (PreparedStatement ps = c.prepareStatement("INSERT INTO tblCustomer (id, customerId) VALUES (?, ?)")) {
                ps.setInt(1, id);
                ps.setString(2, code);
                ps.executeUpdate();
            }
        }
    }

    protected static void insertProduct(int id, String name, String price) throws SQLException {
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO tblProduct (id, name, unitPrice, stockQuantity) VALUES (?, ?, ?, 100)")) {
            ps.setInt(1, id);
            ps.setString(2, name);
            ps.setBigDecimal(3, new BigDecimal(price));
            ps.executeUpdate();
        }
    }

    /** One invoice line: product id and quantity (subtotal = quantity x product price). */
    protected record Line(int productId, int quantity) {
    }

    /**
     * Creates an invoice with the given order time and status history.
     * {@code statuses} is a list of {status, updateTime} pairs inserted in the order given.
     */
    protected static void insertInvoice(int id, int customerId, LocalDateTime orderDate, String[][] statuses, Line... lines)
            throws SQLException {
        try (Connection c = DatabaseConnection.getConnection()) {
            try (PreparedStatement ps = c.prepareStatement(
                    "INSERT INTO tblInvoice (invoiceId, customerId, orderDate, shippingAddress, paymentMethod, totalAmount) "
                            + "VALUES (?, ?, ?, 'Hà Nội', 'COD', 0)")) {
                ps.setInt(1, id);
                ps.setInt(2, customerId);
                ps.setTimestamp(3, Timestamp.valueOf(orderDate));
                ps.executeUpdate();
            }
            for (Line line : lines) {
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO tblInvoiceDetail (invoiceId, productId, quantity, unitPrice, subtotal) "
                                + "SELECT ?, id, ?, unitPrice, unitPrice * ? FROM tblProduct WHERE id = ?")) {
                    ps.setInt(1, id);
                    ps.setInt(2, line.quantity());
                    ps.setInt(3, line.quantity());
                    ps.setInt(4, line.productId());
                    ps.executeUpdate();
                }
            }
            for (String[] s : statuses) {
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT INTO tblStatusHistory (invoiceId, status, updateTime) VALUES (?, ?, ?)")) {
                    ps.setInt(1, id);
                    ps.setString(2, s[0]);
                    ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.parse(s[1])));
                    ps.executeUpdate();
                }
            }
        }
    }

    protected static String[][] delivered(String orderTime) {
        return new String[][] {{"pending", orderTime}, {"delivered", LocalDateTime.parse(orderTime).plusDays(1).toString()}};
    }
}
