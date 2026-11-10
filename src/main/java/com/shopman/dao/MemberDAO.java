package com.shopman.dao;

import com.shopman.model.Member;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/** tblMember access. Takes the connection from the caller so several DAOs can share one transaction. */
public class MemberDAO {

    public static final String ROLE_MANAGER = "manager";
    public static final String ROLE_SALES = "sales";
    public static final String ROLE_DELIVERY = "delivery";
    public static final String ROLE_CUSTOMER = "customer";

    private final Connection connection;

    public MemberDAO(Connection connection) {
        this.connection = connection;
    }

    /** Inserts the member and writes the generated id back into {@code member}. */
    public boolean insertMember(Member member) throws SQLException {
        String sql = "INSERT INTO tblMember (username, password, name, dateOfBirth, address, email, phone, description) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, member.getUsername());
            ps.setString(2, member.getPassword());
            ps.setString(3, member.getName());
            ps.setDate(4, member.getDateOfBirth() == null ? null : Date.valueOf(member.getDateOfBirth()));
            ps.setString(5, member.getAddress());
            ps.setString(6, member.getEmail());
            ps.setString(7, member.getPhone());
            ps.setString(8, member.getNote());
            if (ps.executeUpdate() != 1) {
                return false;
            }
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    return false;
                }
                member.setId(keys.getInt(1));
                return true;
            }
        }
    }

    public boolean existsUsername(String username) throws SQLException {
        return exists("SELECT 1 FROM tblMember WHERE username = ?", username);
    }

    public boolean existsEmail(String email) throws SQLException {
        return exists("SELECT 1 FROM tblMember WHERE LOWER(email) = LOWER(?)", email);
    }

    /** Used by login: returns null when the username is unknown. */
    public Member findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password, name, dateOfBirth, address, email, phone, description "
                + "FROM tblMember WHERE username = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return null;
                }
                Member m = new Member();
                m.setId(rs.getInt("id"));
                m.setUsername(rs.getString("username"));
                m.setPassword(rs.getString("password"));
                m.setName(rs.getString("name"));
                Date dob = rs.getDate("dateOfBirth");
                m.setDateOfBirth(dob == null ? null : dob.toLocalDate());
                m.setAddress(rs.getString("address"));
                m.setEmail(rs.getString("email"));
                m.setPhone(rs.getString("phone"));
                m.setNote(rs.getString("description"));
                return m;
            }
        }
    }

    /** Role of a member, decided by which subtype table holds its id. Null if none. */
    public String findRole(int memberId) throws SQLException {
        if (existsId("tblManager", memberId)) {
            return ROLE_MANAGER;
        }
        if (existsId("tblSalesEmployee", memberId)) {
            return ROLE_SALES;
        }
        if (existsId("tblDeliveryEmployee", memberId)) {
            return ROLE_DELIVERY;
        }
        if (existsId("tblCustomer", memberId)) {
            return ROLE_CUSTOMER;
        }
        return null;
    }

    private boolean existsId(String table, int id) throws SQLException {
        // table names come from the fixed list above, never from user input
        try (PreparedStatement ps = connection.prepareStatement("SELECT 1 FROM " + table + " WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private boolean exists(String sql, String value) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, value);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
