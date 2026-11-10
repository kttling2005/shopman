package com.shopman.model;

/** Status values stored in tblStatusHistory.status. */
public final class InvoiceStatus {

    public static final String PENDING = "pending";
    public static final String APPROVED = "approved";
    public static final String REJECTED = "rejected";
    public static final String SHIPPING = "shipping";
    public static final String DELIVERED = "delivered";
    public static final String CANCELLED = "cancelled";

    /**
     * An invoice counts as a successful sale (and therefore as revenue) once its latest
     * status is this one. Change here if your group defines "bán thành công" differently.
     */
    public static final String COUNTED_AS_REVENUE = DELIVERED;

    private InvoiceStatus() {
    }
}
