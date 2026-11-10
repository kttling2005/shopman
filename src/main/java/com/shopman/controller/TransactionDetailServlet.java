package com.shopman.controller;

import com.shopman.dao.DatabaseConnection;
import com.shopman.dao.ProductDAO;
import com.shopman.model.InvoiceDetail;
import com.shopman.model.Product;
import com.shopman.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/** TransactionDetailUI: every sale of one product in the period, with quantity and revenue totals. */
@WebServlet("/manager/statistics/revenue/transactions")
public class TransactionDetailServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(TransactionDetailServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int productId = WebUtil.parseInt(request.getParameter("productId"), -1);
        LocalDate startDate = WebUtil.parseDate(request.getParameter("startDate"));
        LocalDate endDate = WebUtil.parseDate(request.getParameter("endDate"));
        if (productId < 1 || startDate == null || endDate == null || endDate.isBefore(startDate)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Tham số không hợp lệ");
            return;
        }
        try {
            Product product;
            try (Connection connection = DatabaseConnection.getConnection()) {
                product = new ProductDAO(connection).findById(productId);
            }
            if (product == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy mặt hàng");
                return;
            }
            List<InvoiceDetail> transactions = new InvoiceDetail().getProductTransaction(productId, startDate, endDate);
            int totalQuantity = transactions.stream().mapToInt(InvoiceDetail::getQuantity).sum();
            BigDecimal totalRevenue = transactions.stream()
                    .map(InvoiceDetail::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);

            request.setAttribute("product", product);
            request.setAttribute("transactions", transactions);
            request.setAttribute("totalQuantity", totalQuantity);
            request.setAttribute("totalRevenue", totalRevenue);
            request.setAttribute("startDate", startDate.toString());
            request.setAttribute("endDate", endDate.toString());
            request.getRequestDispatcher("/WEB-INF/view/manager/transactions.jsp").forward(request, response);
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Transaction list failed", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Hệ thống đang gặp lỗi, vui lòng thử lại sau");
        }
    }
}
