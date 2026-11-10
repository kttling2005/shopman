package com.shopman.controller;

import com.shopman.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Map;

/**
 * StatisticsSelectionUI: the manager picks a statistics type and a date range.
 * Only "revenue" (thống kê mặt hàng theo doanh thu) is implemented in this module.
 */
@WebServlet("/manager/statistics")
public class StatisticsSelectionServlet extends HttpServlet {

    /** Statistics that exist only as static layout screens so far: type -> file under /ui/. */
    private static final Map<String, String> PROTOTYPE_PAGES = Map.of(
            "import", "m-stat-import.html",
            "export", "m-stat-export.html",
            "customer", "m-stat-customers.html",
            "supplier", "m-stat-suppliers.html");

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String type = request.getParameter("type");
        String start = request.getParameter("startDate");
        String end = request.getParameter("endDate");

        if (type != null) { // the form was submitted
            String prototype = PROTOTYPE_PAGES.get(type);
            if (prototype != null) { // other statistics are still layout prototypes (see /ui/)
                response.sendRedirect(request.getContextPath() + "/ui/" + prototype);
                return;
            }
            if (!"revenue".equals(type)) {
                request.setAttribute("error", "Loại thống kê không hợp lệ");
            } else {
                LocalDate startDate = WebUtil.parseDate(start);
                LocalDate endDate = WebUtil.parseDate(end);
                if (startDate == null || endDate == null) {
                    request.setAttribute("error", "Vui lòng chọn ngày bắt đầu và ngày kết thúc");
                } else if (endDate.isBefore(startDate)) {
                    request.setAttribute("error", "Ngày kết thúc phải sau hoặc trùng ngày bắt đầu");
                } else {
                    response.sendRedirect(request.getContextPath() + "/manager/statistics/revenue?startDate="
                            + URLEncoder.encode(startDate.toString(), StandardCharsets.UTF_8)
                            + "&endDate=" + URLEncoder.encode(endDate.toString(), StandardCharsets.UTF_8));
                    return;
                }
            }
        }
        request.setAttribute("startDate", start);
        request.setAttribute("endDate", end);
        request.getRequestDispatcher("/WEB-INF/view/manager/statistics-select.jsp").forward(request, response);
    }
}
