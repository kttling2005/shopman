# ShopMan – Member Registration & Product Revenue Statistics

Java 17 · Jakarta Servlet/JSP (Tomcat 10.1+) · JDBC · MySQL 8 (MariaDB works too)

Implements, from the PTTK report:

| Module | Use case | Screens |
|---|---|---|
| Member Registration | Đăng kí thành viên | `/register` |
| Product Revenue Statistics | Xem thống kê mặt hàng theo doanh thu, Xem giao dịch của mặt hàng | `/manager/statistics`, `/manager/statistics/revenue`, `/manager/statistics/revenue/transactions` |

A minimal `/login` + `/logout` is included only so the manager can reach the statistics pages
(the full Login module is not part of this delivery).

## 0. How the layout and the backend fit together

* **Real (backend) pages** use the same `assets/css/styles.css` as the layout and look identical to it:
  `/register`, `/login`, `/manager/statistics`, `/manager/statistics/revenue`, `/manager/statistics/revenue/transactions`.
* **All other screens** (customer shop, cart, orders, manager products/suppliers/receipts, sales, delivery,
  states board, the other four statistics) are the static layout screens in `src/main/webapp/ui/`.
  They show sample data only and are **prototypes**: open them at `/ui/index.html` (list of all screens).
  Their links already point to the real pages where one exists (login, register, logout, statistics).
* `/` shows the customer home screen, or sends a logged-in manager to the reports.
* The manager sidebar links to the prototype screens, so you can click through the whole system.
* When your group implements another module, replace its static file in `webapp/ui/` with a servlet + JSP
  (copy the pattern of `RevenueStatisticsServlet` / `revenue.jsp`) and change the links to it.
* The static prototypes are public (no login) and contain no real data.

## 1. Database

```sql
-- in MySQL Workbench or the mysql client
SOURCE database/schema.sql;   -- creates database `shopman` and all tables
SOURCE database/seed.sql;     -- demo data (optional)
```

Seed data reproduces the report's scenario (1/1/2026 – 30/4/2026):
HH01 Lò nướng ABC = 2 sold, 8.750.000 · HH02 Bánh quy XYZ = 210 sold, 4.200.000.
One *pending* invoice is included on purpose: it must not appear in the statistics.

Demo logins (password `Shopman@123`): `quanli01` (manager), `maianh`, `vanbinh` (customers).

## 2. Configure the connection

Edit `src/main/resources/db.properties`, **or** set environment variables (they win):
`SHOPMAN_DB_URL`, `SHOPMAN_DB_USERNAME`, `SHOPMAN_DB_PASSWORD`, `SHOPMAN_DB_DRIVER`.

## 3. Build and run

```bash
mvn clean package          # runs the unit tests; DB tests are skipped unless SHOPMAN_TEST_DB_URL is set
# copy target/shopman.war into Tomcat 10.1's webapps/ and start Tomcat
# open http://localhost:8080/shopman/
```

**Tomcat 9 instead?** It uses the old `javax.*` namespace. Replace `jakarta.servlet` with `javax.servlet`
in the Java files, use `javax.servlet-api 4.0.1` and `jstl 1.2` in `pom.xml`, and `web.xml` version 4.0.

## 4. Tests

```bash
# create an EMPTY database first: CREATE DATABASE shopman_test CHARACTER SET utf8mb4;
export SHOPMAN_TEST_DB_URL='jdbc:mysql://localhost:3306/shopman_test?useUnicode=true&characterEncoding=UTF-8'
export SHOPMAN_TEST_DB_USER=root SHOPMAN_TEST_DB_PASSWORD=yourpassword
mvn test
```

The DB tests **wipe and rebuild** the database they are pointed at. Never use your real `shopman` database.

## 5. Where the report's design lives in the code

| Report | Code |
|---|---|
| `Member`, `Customer` (register, validateInfo, saveInfoCustomer) | `model/Member`, `model/Customer` |
| `MemberDAO` (insertMember, existsUsername, existsEmail), `CustomerDAO` | `dao/MemberDAO`, `dao/CustomerDAO` |
| `DatabaseConnection` | `dao/DatabaseConnection` |
| `RevenueStatistics` (getProductRevenueStatistics, calculateTotalRevenue) | `model/RevenueStatistics` |
| `InvoiceDetail.getProductTransaction` | `model/InvoiceDetail` |
| `RevenueStatisticsDAO`, `InvoiceDetailDAO` | `dao/RevenueStatisticsDAO`, `dao/InvoiceDetailDAO` |
| `MemberRegistrationUI`, `StatisticsSelectionUI`, `RevenueStatisticsUI`, `TransactionDetailUI` | servlets in `controller/` + JSPs in `WEB-INF/view/` |

## 6. Decisions you should know about (and may want to change)

1. **What counts as revenue.** The report says "giao dịch bán thành công" without naming a status.
   An invoice counts when its **latest** status is `delivered`
   (`model/InvoiceStatus.COUNTED_AS_REVENUE`). Pending, rejected, cancelled and not-yet-delivered orders are excluded.
2. **Which date is filtered.** `tblInvoice.orderDate` (the only date on the invoice). Both start and end days are inclusive.
3. **Registration order.** The sequence diagram saves the customer before the member, but
   `tblCustomer.id` is a foreign key to `tblMember.id`, so the member row is inserted first. Both inserts run in
   one transaction (a failure in the second rolls back the first).
4. **Customer code** is generated from the member id (`KH` + 2 digits, e.g. `KH05`), so it can never collide.
   Product, invoice codes (`HH01`, `HD01`) are formatted from their ids the same way, as the ERD has no code column.
5. **Small additions to the ERD**, all needed by rules in the report text:
   `UNIQUE(email)` (registration exception 5.2), `tblInvoice.salesEmployeeId/deliveryEmployeeId`,
   `tblGoodsReceipt.managerId`, and indexes for the statistics query.
6. **`Fullname`/`Address` helper classes** from the detailed class diagram are not created; the form and the ERD use plain text
   fields (`name`, `address`).
7. **Passwords** are stored as salted PBKDF2-SHA256 hashes, never as plain text. Forms use CSRF tokens.
8. The report's class names carry a "3" suffix (`Member3`, …); it is dropped here. Rename if your group needs the exact names.
9. **CSS looks missing?** The static screens link their stylesheet relatively (`../assets/css/styles.css`), so they must
   be opened through the server at `http://localhost:8080/<context>/ui/...` (not by double-clicking the .html files),
   and any servlet/JSP that sends the user to one must **redirect**, never `forward` (a forward keeps the old URL
   and the relative path then points outside the app). `index.jsp` already redirects. Session cookies are used
   (no `;jsessionid` in URLs).
