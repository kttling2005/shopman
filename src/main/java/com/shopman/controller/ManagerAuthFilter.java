package com.shopman.controller;

import com.shopman.dao.MemberDAO;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** Everything under /manager/ needs a logged-in manager (precondition of the statistics use case). */
@WebFilter("/manager/*")
public class ManagerAuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        HttpSession session = request.getSession(false);
        Object role = session == null ? null : session.getAttribute("role");
        if (role == null) {
            String path = request.getRequestURI().substring(request.getContextPath().length());
            String next = request.getQueryString() == null ? path : path + "?" + request.getQueryString();
            response.sendRedirect(request.getContextPath() + "/login?next="
                    + URLEncoder.encode(next, StandardCharsets.UTF_8));
            return;
        }
        if (!MemberDAO.ROLE_MANAGER.equals(role)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Chỉ nhân viên quản lí được xem trang này");
            return;
        }
        response.setHeader("Cache-Control", "no-store");
        chain.doFilter(req, res);
    }
}
