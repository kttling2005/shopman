package com.shopman.model;

import com.shopman.dao.CustomerDAO;
import java.sql.Connection;
import java.sql.SQLException;

/** Customer (tblCustomer) = a member who can place orders. */
public class Customer extends Member {

    private String customerId;

    public Customer() {
    }

    public Customer(Member member) {
        super(member);
    }

    /**
     * Saves the customer row. The business code is derived from the unique member id
     * ("KH" + two-digit id, e.g. KH01), so two registrations can never get the same code.
     * Must run after the member row exists (tblCustomer.id references tblMember.id).
     */
    public boolean saveInfoCustomer(Connection connection) throws SQLException {
        this.customerId = String.format("KH%02d", getId());
        return new CustomerDAO(connection).insertCustomer(this);
    }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }
}
