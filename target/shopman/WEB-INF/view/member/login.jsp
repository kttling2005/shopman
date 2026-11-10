<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!doctype html>
<html lang="vi">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width,initial-scale=1">
  <title>Đăng nhập · ShopMan</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/styles.css">
</head>
<body>
<div class="auth">
  <div class="auth-panel">
    <div>
      <div class="brand"><div class="mark">S</div>ShopMan</div>
      <h1>Mọi điều cần thiết.<br>Gần bạn mỗi ngày.</h1>
      <p>Xuất hóa đơn nhanh, theo dõi đơn rõ ràng, giao hàng đúng hẹn.</p>
    </div>
    <p class="small">© 2026 ShopMan · Đồ án PTTKHTTT</p>
  </div>
  <div class="auth-form">
    <p class="small muted">Chào mừng trở lại</p>
    <h2>Đăng nhập ShopMan</h2>
    <p class="muted mt8" style="margin-bottom:20px">Dùng tài khoản đã đăng kí để tiếp tục.</p>
    <form method="post" action="${pageContext.request.contextPath}/login">
      <input type="hidden" name="csrf" value="<c:out value='${csrf}'/>">
      <input type="hidden" name="next" value="<c:out value='${next}'/>">
      <div class="field">
        <label for="username">Tên đăng nhập <span class="req">*</span></label>
        <input id="username" name="username" class="input" autocomplete="username" value="<c:out value='${username}'/>">
      </div>
      <div class="field">
        <label for="password">Mật khẩu <span class="req">*</span></label>
        <input id="password" name="password" type="password" class="input" autocomplete="current-password">
      </div>
      <c:if test="${not empty error}">
        <div class="alert err"><div><b><c:out value="${error}"/></b></div></div>
      </c:if>
      <div class="mt16"><button class="btn primary block lg" type="submit">Đăng nhập</button></div>
    </form>
    <div class="row between mt16 small">
      <span></span>
      <span class="muted">Chưa có tài khoản? <a class="link" href="${pageContext.request.contextPath}/register">Đăng kí thành viên</a></span>
    </div>
  </div>
</div>
</body>
</html>
