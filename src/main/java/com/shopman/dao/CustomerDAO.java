package com.shopman.dao;

import com.shopman.model.Customer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** tblCustomer access. */
public class CustomerDAO {

    private final Connection connection;

    public CustomerDAO(Connection connection) {
        this.connection = connection;
    }

    /** Inserts the customer row; {@code customer.getId()} must already be the member id. */
    public boolean insertCustomer(Customer customer) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO tblCustomer (id, customerId) VALUES (?, ?)")) {
            ps.setInt(1, customer.getId());
            ps.setString(2, customer.getCustomerId());
            return ps.executeUpdate() == 1;
        }
    }

    /** Finds a customer by business code (e.g. "KH01"); null when not found. */
    public Customer findById(String customerId) throws SQLException {
        String sql = "SELECT c.id, c.customerId, m.username, m.name, m.email, m.phone, m.address "
                + "FROM tblCustomer c JOIN tblMember m ON m.id = c.id WHERE c.customerId = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Customer c = new Customer();
                c.setId(rs.getInt("id"));
                c.setCustomerId(rs.getString("customerId"));
                c.setUsername(rs.getString("username"));
                c.setName(rs.getString("name"));
                c.setEmail(rs.getString("email"));
                c.setPhone(rs.getString("phone"));
                c.setAddress(rs.getString("address"));
                return c;
            }
        }
    }
}
