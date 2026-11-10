<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Giao dịch mặt hàng"/>
<%@ include file="_start.jspf" %>

<c:url var="backUrl" value="/manager/statistics/revenue">
  <c:param name="startDate" value="${startDate}"/>
  <c:param name="endDate" value="${endDate}"/>
</c:url>

<div class="crumbs">
  <a href="${pageContext.request.contextPath}/manager/statistics">Báo cáo</a> /
  <a href="${backUrl}">Doanh thu mặt hàng</a> / <c:out value="${product.code}"/>
</div>
<div class="page-head">
  <div>
    <h1>Giao dịch mặt hàng <c:out value="${product.code}"/></h1>
    <p>Các lần bán ra từ <c:out value="${startDate}"/> đến <c:out value="${endDate}"/>, theo thứ tự thời gian.</p>
  </div>
  <a class="btn" href="${backUrl}">Quay lại</a>
</div>

<section class="card">
  <b><c:out value="${product.name}"/></b>
  <div class="small muted">
    <c:out value="${product.code}"/> · Giá bán hiện tại <fmt:formatNumber value="${product.unitPrice}" pattern="#,##0"/> đ
  </div>
</section>

<div class="grid g3 mt16">
  <div class="card stat accent"><div class="label">Doanh thu</div>
    <div class="value"><fmt:formatNumber value="${totalRevenue}" pattern="#,##0"/> đ</div><div class="sub">Trong kỳ</div></div>
  <div class="card stat"><div class="label">Số lần giao dịch</div>
    <div class="value">${transactions.size()}</div><div class="sub">Dòng hóa đơn</div></div>
  <div class="card stat"><div class="label">Số lượng bán</div>
    <div class="value"><fmt:formatNumber value="${totalQuantity}" pattern="#,##0"/></div><div class="sub">Sản phẩm</div></div>
</div>

<section class="card flush mt16">
  <table>
    <thead>
      <tr><th>TT</th><th>Mã hóa đơn</th><th>Ngày giao dịch</th><th>Mã khách hàng</th>
          <th class="num">Số lượng</th><th class="num">Đơn giá</th><th class="num">Thành tiền</th></tr>
    </thead>
    <tbody>
    <c:forEach var="t" items="${transactions}" varStatus="st">
      <tr>
        <td>${st.count}</td>
        <td><c:out value="${t.invoice.code}"/></td>
        <td><c:out value="${t.invoice.orderDateText}"/></td>
        <td><c:out value="${t.invoice.customer.customerId}"/></td>
        <td class="num">${t.quantity}</td>
        <td class="num"><fmt:formatNumber value="${t.unitPrice}" pattern="#,##0"/></td>
        <td class="num"><fmt:formatNumber value="${t.subtotal}" pattern="#,##0"/></td>
      </tr>
    </c:forEach>
    <tr class="total">
      <td colspan="4">Tổng</td>
      <td class="num">${totalQuantity}</td><td></td>
      <td class="num"><fmt:formatNumber value="${totalRevenue}" pattern="#,##0"/></td>
    </tr>
    </tbody>
  </table>
</section>

<%@ include file="_end.jspf" %>
