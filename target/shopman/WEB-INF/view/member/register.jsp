<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
  <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
    <!doctype html>
    <html lang="vi">

    <head>
      <meta charset="utf-8">
      <meta name="viewport" content="width=device-width,initial-scale=1">
      <title>Đăng kí thành viên · ShopMan</title>
      <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/styles.css">
    </head>

    <body>
      <div class="auth reg">
        <div class="auth-panel">
          <div>
            <div class="brand">
              <div class="mark">S</div>ShopMan
            </div>
            <span class="tag" style="margin-top:22px;display:inline-block">Thành viên mới</span>
            <h1>Một tài khoản,<br>trọn tiện ích.</h1>
            <p>Tạo tài khoản miễn phí để đặt hàng và theo dõi trạng thái hóa đơn.</p>
            <div class="points">
              <div><i>✓</i>Đặt mua hàng trực tuyến</div>
              <div><i>✓</i>Theo dõi trạng thái hóa đơn</div>
              <div><i>✓</i>Lưu thông tin giao hàng</div>
            </div>
          </div>
          <p class="small">Đã có tài khoản? <a href="${pageContext.request.contextPath}/login"
              style="text-decoration:underline">Đăng nhập</a></p>
        </div>

        <div class="auth-form" style="max-width:640px">
          <c:choose>
            <c:when test="${registered}">
              <%-- Step 4-6 of the main scenario: success message with an OK button that returns to the login screen
                --%>
                <div class="empty" style="padding:40px 0">
                  <div class="ico">✓</div>
                  <h2>Đăng ký thành công!</h2>
                  <p class="muted mt8">Bạn có thể đăng nhập để sử dụng các chức năng mua hàng.</p>
                  <div class="mt24"><a class="btn primary lg" href="${pageContext.request.contextPath}/login">OK</a>
                  </div>
                </div>
            </c:when>
            <c:otherwise>
              <h2>Đăng kí thành viên</h2>
              <p class="muted mt8" style="margin-bottom:20px">Nhập thông tin cá nhân để tạo tài khoản.</p>

              <c:if test="${not empty errors.form}">
                <div class="alert err" style="margin-bottom:14px">
                  <div><b>
                      <c:out value="${errors.form}" />
                    </b></div>
                </div>
              </c:if>

              <form method="post" action="${pageContext.request.contextPath}/register" novalidate>
                <input type="hidden" name="csrf" value="<c:out value='${csrf}'/>">
                <div class="form-grid">
                  <div class="field">
                    <label for="name">Họ và tên <span class="req">*</span></label>
                    <input id="name" name="name" class="input ${not empty errors.name ? 'err' : ''}" maxlength="100"
                      value="<c:out value='${name}'/>">
                    <c:if test="${not empty errors.name}"><span class="errmsg">
                        <c:out value="${errors.name}" />
                      </span></c:if>
                  </div>
                  <div class="field">
                    <label for="dateOfBirth">Ngày sinh <span class="req">*</span></label>
                    <input id="dateOfBirth" name="dateOfBirth" type="date"
                      class="input ${not empty errors.dateOfBirth ? 'err' : ''}"
                      value="<c:out value='${dateOfBirth}'/>">
                    <c:if test="${not empty errors.dateOfBirth}"><span class="errmsg">
                        <c:out value="${errors.dateOfBirth}" />
                      </span></c:if>
                  </div>
                  <div class="field">
                    <label for="phone">Số điện thoại <span class="req">*</span></label>
                    <input id="phone" name="phone" class="input ${not empty errors.phone ? 'err' : ''}" maxlength="15"
                      value="<c:out value='${phone}'/>">
                    <c:if test="${not empty errors.phone}"><span class="errmsg">
                        <c:out value="${errors.phone}" />
                      </span></c:if>
                  </div>
                  <div class="field">
                    <label for="email">Email <span class="req">*</span></label>
                    <input id="email" name="email" type="email" class="input ${not empty errors.email ? 'err' : ''}"
                      maxlength="100" value="<c:out value='${email}'/>">
                    <c:if test="${not empty errors.email}"><span class="errmsg">
                        <c:out value="${errors.email}" />
                      </span></c:if>
                  </div>
                  <div class="field full">
                    <label for="address">Địa chỉ <span class="req">*</span></label>
                    <input id="address" name="address" class="input ${not empty errors.address ? 'err' : ''}"
                      maxlength="255" value="<c:out value='${address}'/>">
                    <c:if test="${not empty errors.address}"><span class="errmsg">
                        <c:out value="${errors.address}" />
                      </span></c:if>
                  </div>
                  <div class="field">
                    <label for="username">Tên đăng nhập <span class="req">*</span></label>
                    <input id="username" name="username" autocomplete="username"
                      class="input ${not empty errors.username ? 'err' : ''}" maxlength="50"
                      value="<c:out value='${username}'/>">
                    <c:if test="${not empty errors.username}"><span class="errmsg">
                        <c:out value="${errors.username}" />
                      </span></c:if>
                  </div>
                  <div class="field">
                    <label for="note">Ghi chú</label>
                    <input id="note" name="note" class="input ${not empty errors.note ? 'err' : ''}" maxlength="500"
                      placeholder="Không bắt buộc" value="<c:out value='${note}'/>">
                    <c:if test="${not empty errors.note}"><span class="errmsg">
                        <c:out value="${errors.note}" />
                      </span></c:if>
                  </div>
                  <div class="field">
                    <label for="password">Mật khẩu <span class="req">*</span></label>
                    <input id="password" name="password" type="password" autocomplete="new-password"
                      class="input ${not empty errors.password ? 'err' : ''}" maxlength="72">
                    <c:if test="${not empty errors.password}"><span class="errmsg">
                        <c:out value="${errors.password}" />
                      </span></c:if>
                  </div>
                  <div class="field">
                    <label for="confirmPassword">Xác nhận mật khẩu <span class="req">*</span></label>
                    <input id="confirmPassword" name="confirmPassword" type="password" autocomplete="new-password"
                      class="input ${not empty errors.confirmPassword ? 'err' : ''}" maxlength="72">
                    <c:if test="${not empty errors.confirmPassword}"><span class="errmsg">
                        <c:out value="${errors.confirmPassword}" />
                      </span></c:if>
                  </div>
                </div>
                <div class="row between mt8">
                  <a class="btn" href="${pageContext.request.contextPath}/login">Hủy đăng kí</a>
                  <button class="btn primary lg" type="submit" id="registerBtn" disabled>
                    Đăng kí
                  </button>
                </div>
              </form>
            </c:otherwise>
          </c:choose>
        </div>
      </div>

      <script>
        document.addEventListener("DOMContentLoaded", function () {
          const form = document.querySelector('form[action$="/register"]');
          const registerBtn = document.getElementById("registerBtn");

          // Các trường bắt buộc
          const requiredFields = [
            "name",
            "dateOfBirth",
            "phone",
            "email",
            "address",
            "username",
            "password",
            "confirmPassword"
          ];

          function checkForm() {
            const isFilled = requiredFields.every(fieldName => {
              const field = form.elements[fieldName];
              return field && field.value.trim() !== "";
            });

            registerBtn.disabled = !isFilled;
          }

          // Kiểm tra khi người dùng nhập hoặc thay đổi dữ liệu
          requiredFields.forEach(fieldName => {
            const field = form.elements[fieldName];

            field.addEventListener("input", checkForm);
            field.addEventListener("change", checkForm);
          });

          // Kiểm tra trạng thái ban đầu
          checkForm();
        });
      </script>
    </body>

    </html>