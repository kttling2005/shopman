package com.shopman.controller;

import com.shopman.model.Member;
import com.shopman.model.Member.RegistrationResult;
import com.shopman.util.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * MemberRegistrationUI of the report. GET shows the form, POST is submitRegistration().
 * After a successful POST we redirect (post/redirect/get) so a page refresh cannot register twice.
 */
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(RegisterServlet.class.getName());
    private static final String VIEW = "/WEB-INF/view/member/register.jsp";
    private static final String FLASH_SUCCESS = "registerSuccess";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        if (session.getAttribute(FLASH_SUCCESS) != null) {
            session.removeAttribute(FLASH_SUCCESS);
            request.setAttribute("registered", Boolean.TRUE);
        }
        showForm(request, response, new LinkedHashMap<>());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (!WebUtil.csrfValid(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Phiên làm việc không hợp lệ, vui lòng tải lại trang");
            return;
        }

        Member member = new Member();
        member.setName(request.getParameter("name"));
        member.setAddress(request.getParameter("address"));
        member.setEmail(request.getParameter("email"));
        member.setPhone(request.getParameter("phone"));
        member.setUsername(request.getParameter("username"));
        member.setPassword(request.getParameter("password"));
        member.setNote(request.getParameter("note"));

        Map<String, String> errors = new LinkedHashMap<>();
        String dob = request.getParameter("dateOfBirth");
        if (dob != null && !dob.isBlank()) {
            try {
                member.setDateOfBirth(LocalDate.parse(dob.trim()));
            } catch (DateTimeParseException e) {
                errors.put("dateOfBirth", "Ngày sinh không hợp lệ");
            }
        }
        // validateInfo() reports a missing date; do not overwrite the more specific "invalid" message
        Map<String, String> validation = member.validateInfo(request.getParameter("confirmPassword"));
        validation.forEach(errors::putIfAbsent);

        if (!errors.isEmpty()) {
            keepForm(request, member);
            showForm(request, response, errors);
            return;
        }

        try {
            RegistrationResult result = member.register();
            switch (result) {
                case SUCCESS:
                    request.getSession(true).setAttribute(FLASH_SUCCESS, Boolean.TRUE);
                    response.sendRedirect(request.getContextPath() + "/register");
                    return;
                case USERNAME_EXISTS:
                    errors.put("username", "Tên đăng nhập đã được sử dụng");
                    break;
                case EMAIL_EXISTS:
                    errors.put("email", "Email đã được sử dụng");
                    break;
                default:
                    errors.put("form", "Đăng ký không thành công, vui lòng thử lại");
                    break;
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Registration failed", e);
            errors.put("form", "Hệ thống đang gặp lỗi, vui lòng thử lại sau");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        keepForm(request, member);
        showForm(request, response, errors);
    }

    /** Puts the typed values back into the form. Passwords are never echoed. */
    private void keepForm(HttpServletRequest request, Member member) {
        request.setAttribute("name", member.getName());
        request.setAttribute("dateOfBirth", request.getParameter("dateOfBirth"));
        request.setAttribute("phone", member.getPhone());
        request.setAttribute("email", member.getEmail());
        request.setAttribute("address", member.getAddress());
        request.setAttribute("username", member.getUsername());
        request.setAttribute("note", member.getNote());
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response, Map<String, String> errors)
            throws ServletException, IOException {
        request.setAttribute("errors", errors);
        request.setAttribute("csrf", WebUtil.csrfToken(request));
        request.getRequestDispatcher(VIEW).forward(request, response);
    }
}
