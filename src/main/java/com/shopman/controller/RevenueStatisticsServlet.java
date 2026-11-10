package com.shopman.controller;

import com.shopman.model.RevenueStatistics;
import com.shopman.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.logging.Level;
import java.util.logging.Logger;

/** RevenueStatisticsUI: revenue per product for the chosen period, highest first. */
@WebServlet("/manager/statistics/revenue")
public class RevenueStatisticsServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(RevenueStatisticsServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String start = request.getParameter("startDate");
        String end = request.getParameter("endDate");
        request.setAttribute("startDate", start);
        request.setAttribute("endDate", end);

        LocalDate startDate = WebUtil.parseDate(start);
        LocalDate endDate = WebUtil.parseDate(end);
        if (startDate == null || endDate == null) {
            request.setAttribute("error", "Vui lòng chọn ngày bắt đầu và ngày kết thúc");
        } else if (endDate.isBefore(startDate)) {
            request.setAttribute("error", "Ngày kết thúc phải sau hoặc trùng ngày bắt đầu");
        } else {
            try {
                RevenueStatistics statistics = new RevenueStatistics();
                statistics.getProductRevenueStatistics(startDate, endDate);
                request.setAttribute("statistics", statistics);
                request.setAttribute("startDateValue", startDate);
                request.setAttribute("endDateValue", endDate);
                if (statistics.getItems().isEmpty()) { // exception 6 of the use case
                    request.setAttribute("noData", Boolean.TRUE);
                }
            } catch (SQLException e) {
                LOG.log(Level.SEVERE, "Revenue statistics failed", e);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                request.setAttribute("error", "Hệ thống đang gặp lỗi, vui lòng thử lại sau");
            }
        }
        request.getRequestDispatcher("/WEB-INF/view/manager/revenue.jsp").forward(request, response);
    }
}
