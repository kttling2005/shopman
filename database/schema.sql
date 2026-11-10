-- ShopMan database schema (MySQL 8 / MariaDB 10.5+)
-- Follows "Hình 2.1. Thiết kế cơ sở dữ liệu của hệ thống" in the PTTK report.
-- Statistic tables (tblRevenueStatistics, ...) are intentionally NOT created: the report
-- (Phần II, Bước 5) removes them because statistics are derived from business tables.
--
-- Additions to the ERD, each needed by a rule written in the report text:
--   tblMember.email UNIQUE        -> "email chưa tồn tại" (registration exception 5.2)
--   tblInvoice.salesEmployeeId    -> "hóa đơn do một nhân viên bán hàng duyệt"
--   tblInvoice.deliveryEmployeeId -> "hóa đơn được giao bởi một nhân viên giao hàng"
--   tblGoodsReceipt.managerId     -> "nhân viên quản lí lập phiếu nhập"
--   indexes on the columns used by the revenue statistics query

CREATE DATABASE IF NOT EXISTS shopman CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE shopman;

CREATE TABLE IF NOT EXISTS tblMember (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(50)  NOT NULL,
    password    VARCHAR(255) NOT NULL,            -- PBKDF2 hash, never plain text
    name        VARCHAR(100) NOT NULL,
    dateOfBirth DATE         NULL,
    address     VARCHAR(255) NULL,
    email       VARCHAR(100) NOT NULL,
    phone       VARCHAR(15)  NULL,
    description TEXT         NULL,
    CONSTRAINT uq_member_username UNIQUE (username),
    CONSTRAINT uq_member_email    UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tblCustomer (
    id         INT PRIMARY KEY,
    customerId VARCHAR(20) NOT NULL,
    CONSTRAINT uq_customer_code UNIQUE (customerId),
    CONSTRAINT fk_customer_member FOREIGN KEY (id) REFERENCES tblMember (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tblEmployee (
    id       INT PRIMARY KEY,
    position VARCHAR(50) NULL,
    CONSTRAINT fk_employee_member FOREIGN KEY (id) REFERENCES tblMember (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tblSalesEmployee (
    id INT PRIMARY KEY,
    CONSTRAINT fk_sales_employee FOREIGN KEY (id) REFERENCES tblEmployee (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tblManager (
    id INT PRIMARY KEY,
    CONSTRAINT fk_manager_employee FOREIGN KEY (id) REFERENCES tblEmployee (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tblDeliveryEmployee (
    id INT PRIMARY KEY,
    CONSTRAINT fk_delivery_employee FOREIGN KEY (id) REFERENCES tblEmployee (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tblCategory (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description TEXT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tblProduct (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    categoryId    INT NULL,
    name          VARCHAR(150)  NOT NULL,
    unitPrice     DECIMAL(12,2) NOT NULL,
    description   TEXT NULL,
    image         VARCHAR(255) NULL,
    stockQuantity INT NOT NULL DEFAULT 0,
    CONSTRAINT chk_product_price CHECK (unitPrice >= 0),
    CONSTRAINT chk_product_stock CHECK (stockQuantity >= 0),
    CONSTRAINT fk_product_category FOREIGN KEY (categoryId) REFERENCES tblCategory (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tblSupplier (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    address     VARCHAR(255) NULL,
    phone       VARCHAR(15)  NULL,
    email       VARCHAR(100) NULL,
    description TEXT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tblInvoice (
    invoiceId          INT AUTO_INCREMENT PRIMARY KEY,
    customerId         INT NOT NULL,                       -- tblCustomer.id
    orderDate          DATETIME NOT NULL,
    shippingAddress    VARCHAR(255) NOT NULL,
    paymentMethod      VARCHAR(50)  NOT NULL,
    totalAmount        DECIMAL(12,2) NOT NULL,
    salesEmployeeId    INT NULL,
    deliveryEmployeeId INT NULL,
    CONSTRAINT fk_invoice_customer FOREIGN KEY (customerId)         REFERENCES tblCustomer (id),
    CONSTRAINT fk_invoice_sales    FOREIGN KEY (salesEmployeeId)    REFERENCES tblSalesEmployee (id),
    CONSTRAINT fk_invoice_delivery FOREIGN KEY (deliveryEmployeeId) REFERENCES tblDeliveryEmployee (id),
    INDEX idx_invoice_orderDate (orderDate)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tblInvoiceDetail (
    id        INT AUTO_INCREMENT PRIMARY KEY,
    invoiceId INT NOT NULL,
    productId INT NOT NULL,
    quantity  INT NOT NULL,
    unitPrice DECIMAL(12,2) NOT NULL,
    subtotal  DECIMAL(12,2) NOT NULL,
    CONSTRAINT chk_invoicedetail_qty CHECK (quantity > 0),
    CONSTRAINT fk_invoicedetail_invoice FOREIGN KEY (invoiceId) REFERENCES tblInvoice (invoiceId) ON DELETE CASCADE,
    CONSTRAINT fk_invoicedetail_product FOREIGN KEY (productId) REFERENCES tblProduct (id),
    INDEX idx_invoicedetail_product (productId, invoiceId)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- One row per status change; the current status is the latest row of an invoice.
-- Values: pending, approved, rejected, shipping, delivered, cancelled
CREATE TABLE IF NOT EXISTS tblStatusHistory (
    id         INT AUTO_INCREMENT PRIMARY KEY,
    invoiceId  INT NOT NULL,
    status     VARCHAR(50) NOT NULL,
    updateTime DATETIME NOT NULL,
    CONSTRAINT fk_status_invoice FOREIGN KEY (invoiceId) REFERENCES tblInvoice (invoiceId) ON DELETE CASCADE,
    INDEX idx_status_latest (invoiceId, updateTime, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tblGoodsReceipt (
    receiptId   INT AUTO_INCREMENT PRIMARY KEY,
    supplierId  INT NOT NULL,
    managerId   INT NULL,
    receiptDate DATE NOT NULL,
    CONSTRAINT fk_receipt_supplier FOREIGN KEY (supplierId) REFERENCES tblSupplier (id),
    CONSTRAINT fk_receipt_manager  FOREIGN KEY (managerId)  REFERENCES tblManager (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS tblGoodsReceiptDetail (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    receiptId        INT NOT NULL,
    productId        INT NOT NULL,
    quantityReceived INT NOT NULL,
    importPrice      DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_receiptdetail_receipt FOREIGN KEY (receiptId) REFERENCES tblGoodsReceipt (receiptId) ON DELETE CASCADE,
    CONSTRAINT fk_receiptdetail_product FOREIGN KEY (productId) REFERENCES tblProduct (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
