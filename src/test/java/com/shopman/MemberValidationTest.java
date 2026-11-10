package com.shopman;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.shopman.model.Member;
import com.shopman.util.PasswordHasher;
import java.time.LocalDate;
import java.util.Map;
import org.junit.jupiter.api.Test;

class MemberValidationTest {

    /** The data of the report's scenario (Phần I, kịch bản đăng kí thành viên). */
    private static Member scenarioMember() {
        Member m = new Member();
        m.setName("Nguyen Van A");
        m.setDateOfBirth(LocalDate.of(2005, 12, 29));
        m.setPhone("0981025133");
        m.setEmail("A@stu.ptit.edu.vn");
        m.setAddress("Ha Dong, Ha Noi");
        m.setUsername("nguyenvana");
        m.setPassword("123456");
        return m;
    }

    @Test
    void scenarioDataIsValid() {
        assertTrue(scenarioMember().validateInfo("123456").isEmpty());
    }

    @Test
    void blankRequiredFieldsAreReported() { // exception 5.4
        Map<String, String> errors = new Member().validateInfo("");
        for (String field : new String[] {"name", "dateOfBirth", "phone", "email", "address", "username", "password", "confirmPassword"}) {
            assertTrue(errors.containsKey(field), "expected an error for " + field);
        }
    }

    @Test
    void confirmPasswordMustMatch() { // exception 5.3
        Map<String, String> errors = scenarioMember().validateInfo("654321");
        assertEquals(1, errors.size());
        assertTrue(errors.containsKey("confirmPassword"));
    }

    @Test
    void invalidFormatsAreRejected() { // exception 5.1
        Member m = scenarioMember();
        m.setEmail("not-an-email");
        m.setPhone("12345");
        m.setUsername("a b");
        m.setPassword("123");
        m.setDateOfBirth(LocalDate.now().plusDays(1));
        Map<String, String> errors = m.validateInfo("123");
        assertTrue(errors.keySet().containsAll(java.util.List.of("email", "phone", "username", "password", "dateOfBirth")));
    }

    @Test
    void acceptsInternationalPhone() {
        Member m = scenarioMember();
        m.setPhone("+84981025133");
        assertFalse(m.validateInfo("123456").containsKey("phone"));
    }

    @Test
    void passwordHashIsSaltedAndVerifiable() {
        String h1 = PasswordHasher.hash("123456");
        String h2 = PasswordHasher.hash("123456");
        assertNotEquals(h1, h2);
        assertTrue(h1.length() <= 255);
        assertTrue(PasswordHasher.verify("123456", h1));
        assertFalse(PasswordHasher.verify("123457", h1));
        assertFalse(PasswordHasher.verify("123456", "garbage"));
        assertFalse(PasswordHasher.verify("123456", null));
    }
}
