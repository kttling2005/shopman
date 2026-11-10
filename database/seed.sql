-- ShopMan demo data. Run AFTER schema.sql.
-- Reproduces the scenario of the report ("Xem thống kê mặt hàng theo doanh thu", 1/1/2026 - 30/4/2026):
--   HH01 Lò nướng ABC  : 2 sold,   8.750.000  (HD01 1/2/2026 KH01, HD02 3/4/2026 KH02)
--   HH02 Bánh quy XYZ  : 210 sold, 4.200.000
-- Invoice 4 is still "pending": it must NOT appear in the statistics.
--
-- Demo logins (password for all three: Shopman@123)
--   quanli01  manager      maianh  customer      vanbinh  customer

USE shopman;

INSERT INTO tblMember (id, username, password, name, dateOfBirth, address, email, phone, description) VALUES
 (1, 'maianh',   'pbkdf2_sha256$310000$KXR5yKDQaUP3qX+7MpWqGg==$pzyfxnAPG3ZZ/vzSfroEhVlU3OlMIiUJgFimpzvGG1k=', 'Nguyễn Mai Anh', '2005-12-29', 'Hà Đông, Hà Nội',  'a@stu.ptit.edu.vn',   '0981025133', NULL),
 (2, 'vanbinh',  'pbkdf2_sha256$310000$KXR5yKDQaUP3qX+7MpWqGg==$pzyfxnAPG3ZZ/vzSfroEhVlU3OlMIiUJgFimpzvGG1k=', 'Trần Văn Bình',  '2004-05-12', 'Cầu Giấy, Hà Nội', 'binh@stu.ptit.edu.vn', '0912345678', NULL),
 (3, 'quanli01', 'pbkdf2_sha256$310000$KXR5yKDQaUP3qX+7MpWqGg==$pzyfxnAPG3ZZ/vzSfroEhVlU3OlMIiUJgFimpzvGG1k=', 'Lê Hải Nam',     '1998-03-02', 'Thanh Xuân, Hà Nội', 'nam@shopman.vn',      '0933222111', NULL);

INSERT INTO tblCustomer (id, customerId) VALUES (1, 'KH01'), (2, 'KH02');
INSERT INTO tblEmployee (id, position) VALUES (3, 'Quản lí');
INSERT INTO tblManager (id) VALUES (3);

INSERT INTO tblCategory (id, name, description) VALUES
 (1, 'Đồ gia dụng', 'Lò nướng, nồi, dụng cụ bếp'),
 (2, 'Thực phẩm',   'Bánh, gạo, gia vị');

INSERT INTO tblSupplier (id, name, address, phone, email, description) VALUES
 (1, 'Công ty TNHH Gia Dụng An Phát', '12 Phố Huế, Hai Bà Trưng, Hà Nội', '0243555010', 'anphat@example.vn', 'Cung cấp đồ gia dụng nhà bếp');

INSERT INTO tblProduct (id, categoryId, name, unitPrice, description, image, stockQuantity) VALUES
 (1, 1, 'Lò nướng ABC', 4375000, 'Lò nướng điện dung tích 30 lít', NULL, 18),
 (2, 2, 'Bánh quy XYZ',   20000, 'Bánh quy bơ hộp 200g',           NULL, 300);

INSERT INTO tblInvoice (invoiceId, customerId, orderDate, shippingAddress, paymentMethod, totalAmount) VALUES
 (1, 1, '2026-02-01 09:15:00', 'Hà Đông, Hà Nội',  'COD', 6375000),
 (2, 2, '2026-04-03 14:20:00', 'Cầu Giấy, Hà Nội', 'COD', 5575000),
 (3, 2, '2026-03-10 10:05:00', 'Cầu Giấy, Hà Nội', 'COD', 1000000),
 (4, 1, '2026-04-20 16:40:00', 'Hà Đông, Hà Nội',  'COD', 100000);

INSERT INTO tblInvoiceDetail (invoiceId, productId, quantity, unitPrice, subtotal) VALUES
 (1, 1,   1, 4375000, 4375000),
 (1, 2, 100,   20000, 2000000),
 (2, 1,   1, 4375000, 4375000),
 (2, 2,  60,   20000, 1200000),
 (3, 2,  50,   20000, 1000000),
 (4, 2,   5,   20000,  100000);

INSERT INTO tblStatusHistory (invoiceId, status, updateTime) VALUES
 (1, 'pending',   '2026-02-01 09:15:00'), (1, 'approved',  '2026-02-01 10:00:00'), (1, 'shipping',  '2026-02-02 08:00:00'), (1, 'delivered', '2026-02-02 15:30:00'),
 (2, 'pending',   '2026-04-03 14:20:00'), (2, 'approved',  '2026-04-03 15:00:00'), (2, 'shipping',  '2026-04-04 08:00:00'), (2, 'delivered', '2026-04-04 16:10:00'),
 (3, 'pending',   '2026-03-10 10:05:00'), (3, 'approved',  '2026-03-10 11:00:00'), (3, 'shipping',  '2026-03-11 08:00:00'), (3, 'delivered', '2026-03-11 14:00:00'),
 (4, 'pending',   '2026-04-20 16:40:00');
