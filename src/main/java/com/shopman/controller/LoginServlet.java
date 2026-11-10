package com.shopman.controller;

import com.shopman.dao.DatabaseConnection;
import com.shopman.dao.MemberDAO;
import com.shopman.model.Member;
import com.shopman.util.PasswordHasher;
import com.shopman.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Minimal login so the manager can reach the statistics pages (the full Login use case is a separate module).
 * Stores memberId, username, name and role in the session.
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(LoginServlet.class.getName());
    private static final String VIEW = "/WEB-INF/view/member/login.jsp";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        show(request, response, null);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (!WebUtil.csrfValid(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Phiên làm việc không hợp lệ, vui lòng tải lại trang");
            return;
        }
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        if (username == null || username.isBlank() || password == null || password.isEmpty()) {
            show(request, response, "Vui lòng nhập tên đăng nhập và mật khẩu");
            return;
        }
        try (Connection connection = DatabaseConnection.getConnection()) {
            MemberDAO dao = new MemberDAO(connection);
            Member member = dao.findByUsername(username.trim());
            if (member == null || !PasswordHasher.verify(password, member.getPassword())) {
                show(request, response, "Tên đăng nhập hoặc mật khẩu không đúng");
                return;
            }
            String role = dao.findRole(member.getId());
            if (role == null) {
                show(request, response, "Tài khoản chưa được cấp quyền sử dụng");
                return;
            }
            request.getSession(true);
            request.changeSessionId(); // new session id after login (session fixation)
            HttpSession session = request.getSession();
            session.setAttribute("memberId", member.getId());
            session.setAttribute("username", member.getUsername());
            session.setAttribute("displayName", member.getName());
            session.setAttribute("role", role);
            response.sendRedirect(request.getContextPath() + landingPath(role, request.getParameter("next")));
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Login failed", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            show(request, response, "Hệ thống đang gặp lỗi, vui lòng thử lại sau");
        }
    }

    /** Only same-site paths under /manager/ are accepted as "next", never an external URL. */
    private static String landingPath(String role, String next) {
        if (MemberDAO.ROLE_MANAGER.equals(role)) {
            if (next != null && next.startsWith("/manager/") && !next.contains("//") && !next.contains("\\")) {
                return next;
            }
            return "/manager/statistics";
        }
        return "/";
    }

    private void show(HttpServletRequest request, HttpServletResponse response, String error)
            throws ServletException, IOException {
        request.setAttribute("error", error);
        request.setAttribute("csrf", WebUtil.csrfToken(request));
        request.setAttribute("next", request.getParameter("next"));
        request.setAttribute("username", request.getParameter("username"));
        request.getRequestDispatcher(VIEW).forward(request, response);
    }
}
