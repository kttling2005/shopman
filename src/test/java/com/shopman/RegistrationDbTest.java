package com.shopman;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.shopman.dao.CustomerDAO;
import com.shopman.dao.DatabaseConnection;
import com.shopman.model.Customer;
import com.shopman.model.Member;
import com.shopman.model.Member.RegistrationResult;
import com.shopman.util.PasswordHasher;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class RegistrationDbTest extends DbTestBase {

    private static Member member(String username, String email) {
        Member m = new Member();
        m.setName("Nguyen Van A");
        m.setDateOfBirth(LocalDate.of(2005, 12, 29));
        m.setPhone("0981025133");
        m.setEmail(email);
        m.setAddress("Ha Dong, Ha Noi");
        m.setUsername(username);
        m.setPassword("123456");
        return m;
    }

    private static int count(String table) throws Exception {
        try (Connection c = DatabaseConnection.getConnection();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM " + table)) {
            rs.next();
            return rs.getInt(1);
        }
    }

    @Test
    void registersMemberAndCustomer() throws Exception {
        Member m = member("nguyenvana", "A@stu.ptit.edu.vn");
        assertEquals(RegistrationResult.SUCCESS, m.register());

        assertEquals(1, count("tblMember"));
        assertEquals(1, count("tblCustomer"));
        try (Connection c = DatabaseConnection.getConnection(); Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT m.password, m.email, m.name, m.dateOfBirth, c.customerId FROM tblMember m JOIN tblCustomer c ON c.id = m.id")) {
            assertTrue(rs.next());
            assertNotEquals("123456", rs.getString("password"), "password must not be stored in plain text");
            assertTrue(PasswordHasher.verify("123456", rs.getString("password")));
            assertEquals("a@stu.ptit.edu.vn", rs.getString("email"), "email is stored in lower case");
            assertEquals("Nguyen Van A", rs.getString("name"));
            assertEquals(LocalDate.of(2005, 12, 29), rs.getDate("dateOfBirth").toLocalDate());
            assertEquals("KH01", rs.getString("customerId"));
        }
    }

    @Test
    void customerCanBeFoundByBusinessCode() throws Exception {
        member("nguyenvana", "a@x.vn").register();
        try (Connection c = DatabaseConnection.getConnection()) {
            Customer found = new CustomerDAO(c).findById("KH01");
            assertNotNull(found);
            assertEquals("nguyenvana", found.getUsername());
        }
    }

    @Test
    void rejectsExistingUsername() throws Exception {
        assertEquals(RegistrationResult.SUCCESS, member("nguyenvana", "a@x.vn").register());
        assertEquals(RegistrationResult.USERNAME_EXISTS, member("nguyenvana", "other@x.vn").register());
        assertEquals(1, count("tblMember"));
    }

    @Test
    void rejectsExistingEmailIgnoringCase() throws Exception { // exception 5.2
        assertEquals(RegistrationResult.SUCCESS, member("nguyenvana", "A@stu.ptit.edu.vn").register());
        assertEquals(RegistrationResult.EMAIL_EXISTS, member("someoneelse", "a@STU.ptit.edu.vn").register());
        assertEquals(1, count("tblMember"));
        assertEquals(1, count("tblCustomer"));
    }

    @Test
    void sqlInjectionTextIsStoredAsPlainData() throws Exception {
        Member m = member("nguyenvana", "a@x.vn");
        m.setName("Robert'); DROP TABLE tblMember;--");
        assertEquals(RegistrationResult.SUCCESS, m.register());
        assertEquals(1, count("tblMember"));
    }

    /** If saving the customer fails, the member row must be rolled back too (one transaction). */
    @Test
    void failureWhileSavingCustomerLeavesNoHalfRegisteredMember() throws Exception {
        // Occupy the business code the next registration will get: member id 101 -> "KH101".
        insertCustomer(100, "KH101");
        try (Connection c = DatabaseConnection.getConnection(); Statement st = c.createStatement()) {
            st.execute("ALTER TABLE tblMember AUTO_INCREMENT = 101");
        }
        int membersBefore = count("tblMember");

        boolean threw = false;
        try {
            member("nguyenvana", "a@x.vn").register();
        } catch (java.sql.SQLException expected) {
            threw = true;
        }
        assertTrue(threw, "duplicate customer code should surface as an SQL error");
        assertEquals(membersBefore, count("tblMember"), "member insert must be rolled back");
        assertEquals(1, count("tblCustomer"));
    }
}
