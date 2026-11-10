package com.shopman.dao;

import com.shopman.model.Product;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** tblProduct access (only what the statistics module needs). */
public class ProductDAO {

    private final Connection connection;

    public ProductDAO(Connection connection) {
        this.connection = connection;
    }

    /** Null when the product does not exist. */
    public Product findById(int productId) throws SQLException {
        String sql = "SELECT id, name, description, unitPrice, stockQuantity, image FROM tblProduct WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Product p = new Product();
                p.setId(rs.getInt("id"));
                p.setName(rs.getString("name"));
                p.setDescription(rs.getString("description"));
                p.setUnitPrice(rs.getBigDecimal("unitPrice"));
                p.setStockQuantity(rs.getInt("stockQuantity"));
                p.setImage(rs.getString("image"));
                return p;
            }
        }
    }
}
