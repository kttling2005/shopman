package com.shopman.model;

import com.shopman.dao.CustomerDAO;
import com.shopman.dao.DatabaseConnection;
import com.shopman.dao.MemberDAO;
import com.shopman.util.PasswordHasher;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Member (tblMember). Carries the registration logic of the module
 * "Đăng kí thành viên": {@link #validateInfo(String)} then {@link #register()}.
 */
public class Member {

    public enum RegistrationResult { SUCCESS, USERNAME_EXISTS, EMAIL_EXISTS, FAILED }

    private static final Pattern USERNAME = Pattern.compile("^[A-Za-z0-9_.]{4,50}$");
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE = Pattern.compile("^(0|\\+84)\\d{9}$");

    private int id;
    private String username;
    private String password;
    private String name;
    private LocalDate dateOfBirth;
    private String address;
    private String email;
    private String phone;
    private String note;

    public Member() {
    }

    protected Member(Member other) {
        this.id = other.id;
        this.username = other.username;
        this.password = other.password;
        this.name = other.name;
        this.dateOfBirth = other.dateOfBirth;
        this.address = other.address;
        this.email = other.email;
        this.phone = other.phone;
        this.note = other.note;
    }

    /**
     * Checks the registration data. Returns field name -> Vietnamese message;
     * an empty map means the data is valid (exceptions 5.1, 5.3, 5.4 of the use case).
     * Keys: name, dateOfBirth, phone, email, address, username, password, confirmPassword.
     */
    public Map<String, String> validateInfo(String confirmPassword) {
        Map<String, String> errors = new LinkedHashMap<>();
        String required = "Vui lòng nhập thông tin này";

        if (isBlank(name)) {
            errors.put("name", required);
        } else if (name.trim().length() > 100) {
            errors.put("name", "Họ tên tối đa 100 ký tự");
        }

        if (dateOfBirth == null) {
            errors.put("dateOfBirth", required);
        } else if (dateOfBirth.isAfter(LocalDate.now()) || dateOfBirth.getYear() < 1900) {
            errors.put("dateOfBirth", "Ngày sinh không hợp lệ");
        }

        if (isBlank(phone)) {
            errors.put("phone", required);
        } else if (!PHONE.matcher(phone.trim()).matches()) {
            errors.put("phone", "Số điện thoại không hợp lệ (ví dụ 0981025133)");
        }

        if (isBlank(email)) {
            errors.put("email", required);
        } else if (email.trim().length() > 100 || !EMAIL.matcher(email.trim()).matches()) {
            errors.put("email", "Email không hợp lệ");
        }

        if (isBlank(address)) {
            errors.put("address", required);
        } else if (address.trim().length() > 255) {
            errors.put("address", "Địa chỉ tối đa 255 ký tự");
        }

        if (isBlank(username)) {
            errors.put("username", required);
        } else if (!USERNAME.matcher(username.trim()).matches()) {
            errors.put("username", "Tên đăng nhập gồm 4–50 ký tự: chữ, số, dấu _ hoặc .");
        }

        if (isBlank(password)) {
            errors.put("password", required);
        } else if (password.length() < 6 || password.length() > 72) {
            errors.put("password", "Mật khẩu phải từ 6 đến 72 ký tự");
        }

        if (isBlank(confirmPassword)) {
            errors.put("confirmPassword", required);
        } else if (!errors.containsKey("password") && !confirmPassword.equals(password)) {
            errors.put("confirmPassword", "Mật khẩu xác nhận không trùng khớp");
        }

        if (note != null && note.length() > 500) {
            errors.put("note", "Ghi chú tối đa 500 ký tự");
        }
        return errors;
    }

    /** Opens its own connection and registers the member as a customer in one transaction. */
    public RegistrationResult register() throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection()) {
            return register(connection);
        }
    }

    /**
     * Sequence of the report (Hình 4.2): existsUsername -> existsEmail -> save the customer -> save the member,
     * all in one transaction. The member row is inserted first because tblCustomer.id is a foreign key
     * to tblMember.id. After a successful call {@code password} holds the hash, not the typed text.
     */
    public RegistrationResult register(Connection connection) throws SQLException {
        normalize();
        boolean previousAutoCommit = connection.getAutoCommit();
        connection.setAutoCommit(false);
        String plainPassword = password;
        try {
            MemberDAO memberDAO = new MemberDAO(connection);
            if (memberDAO.existsUsername(username)) {
                connection.rollback();
                return RegistrationResult.USERNAME_EXISTS;
            }
            if (memberDAO.existsEmail(email)) {
                connection.rollback();
                return RegistrationResult.EMAIL_EXISTS;
            }
            password = PasswordHasher.hash(plainPassword);
            if (!memberDAO.insertMember(this)) {
                connection.rollback();
                return RegistrationResult.FAILED;
            }
            Customer customer = new Customer(this);
            if (!customer.saveInfoCustomer(connection)) {
                connection.rollback();
                return RegistrationResult.FAILED;
            }
            connection.commit();
            return RegistrationResult.SUCCESS;
        } catch (SQLException e) {
            connection.rollback();
            password = plainPassword;
            RegistrationResult duplicate = duplicateKeyResult(e);
            if (duplicate != null) { // two people registering the same value at the same moment
                return duplicate;
            }
            throw e;
        } finally {
            connection.setAutoCommit(previousAutoCommit);
        }
    }

    private static RegistrationResult duplicateKeyResult(SQLException e) {
        if (e.getErrorCode() != 1062 && !"23000".equals(e.getSQLState())) {
            return null;
        }
        String message = e.getMessage() == null ? "" : e.getMessage().toLowerCase();
        if (message.contains("email")) {
            return RegistrationResult.EMAIL_EXISTS;
        }
        if (message.contains("username")) {
            return RegistrationResult.USERNAME_EXISTS;
        }
        return null;
    }

    private void normalize() {
        username = trim(username);
        name = trim(name);
        address = trim(address);
        email = email == null ? null : email.trim().toLowerCase();
        phone = trim(phone);
        note = isBlank(note) ? null : note.trim();
    }

    private static String trim(String s) {
        return s == null ? null : s.trim();
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
