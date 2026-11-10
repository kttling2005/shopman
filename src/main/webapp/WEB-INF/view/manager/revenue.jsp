<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<c:set var="pageTitle" value="Thống kê mặt hàng theo doanh thu"/>
<%@ include file="_start.jspf" %>

<div class="crumbs"><a href="${pageContext.request.contextPath}/manager/statistics">Báo cáo</a> / Doanh thu mặt hàng</div>
<div class="page-head">
  <div><h1>Thống kê mặt hàng theo doanh thu</h1><p>Xếp từ doanh thu cao đến thấp. Nhấn vào một mặt hàng để xem các lần giao dịch.</p></div>
  <a class="btn" href="${pageContext.request.contextPath}/manager/statistics">Quay lại</a>
</div>

<section class="card">
  <form method="get" action="${pageContext.request.contextPath}/manager/statistics/revenue">
    <div class="form-grid" style="grid-template-columns:1fr 1fr auto;align-items:end">
      <div class="field">
        <label for="startDate">Ngày bắt đầu</label>
        <input id="startDate" name="startDate" type="date" class="input" value="<c:out value='${startDate}'/>" required>
      </div>
      <div class="field">
        <label for="endDate">Ngày kết thúc</label>
        <input id="endDate" name="endDate" type="date" class="input" value="<c:out value='${endDate}'/>" required>
      </div>
      <div class="field"><button class="btn primary" type="submit">Xem thống kê</button></div>
    </div>
  </form>
</section>

<c:if test="${not empty error}">
  <div class="alert err" style="margin-top:16px"><div><b><c:out value="${error}"/></b></div></div>
</c:if>

<c:if test="${empty error and empty statistics}">
  <p class="muted mt16">Chọn khoảng thời gian rồi nhấn “Xem thống kê”.</p>
</c:if>

<c:if test="${not empty statistics}">
  <c:choose>
    <c:when test="${noData}">
      <div class="alert info" style="margin-top:16px"><div><b>Không có dữ liệu bán hàng nào trong khoảng thời gian đã chọn.</b></div></div>
    </c:when>
    <c:otherwise>
      <div class="grid g3 mt16">
        <div class="card stat accent"><div class="label">Tổng doanh thu</div>
          <div class="value"><fmt:formatNumber value="${statistics.totalRevenue}" pattern="#,##0"/> đ</div>
          <div class="sub"><c:out value="${startDate}"/> → <c:out value="${endDate}"/></div></div>
        <div class="card stat"><div class="label">Số lượng đã bán</div>
          <div class="value"><fmt:formatNumber value="${statistics.totalQuantity}" pattern="#,##0"/></div><div class="sub">Sản phẩm</div></div>
        <div class="card stat"><div class="label">Mặt hàng có doanh thu</div>
          <div class="value">${statistics.items.size()}</div><div class="sub">Mặt hàng</div></div>
      </div>

      <section class="card flush mt16">
        <table>
          <thead><tr><th>TT</th><th>Mã hàng</th><th>Tên hàng</th><th class="num">Số lượng bán</th><th class="num">Doanh thu (đ)</th></tr></thead>
          <tbody>
          <c:forEach var="row" items="${statistics.items}" varStatus="st">
            <c:url var="detailUrl" value="/manager/statistics/revenue/transactions">
              <c:param name="productId" value="${row.product.id}"/>
              <c:param name="startDate" value="${startDate}"/>
              <c:param name="endDate" value="${endDate}"/>
            </c:url>
            <tr>
              <td>${st.count}</td>
              <td><a class="link" href="${detailUrl}"><c:out value="${row.product.code}"/></a></td>
              <td><a href="${detailUrl}"><c:out value="${row.product.name}"/></a></td>
              <td class="num"><fmt:formatNumber value="${row.quantitySold}" pattern="#,##0"/></td>
              <td class="num"><fmt:formatNumber value="${row.revenue}" pattern="#,##0"/></td>
            </tr>
          </c:forEach>
          <tr class="total">
            <td></td><td></td><td>Tổng</td>
            <td class="num"><fmt:formatNumber value="${statistics.totalQuantity}" pattern="#,##0"/></td>
            <td class="num"><fmt:formatNumber value="${statistics.totalRevenue}" pattern="#,##0"/></td>
          </tr>
          </tbody>
        </table>
      </section>
    </c:otherwise>
  </c:choose>
</c:if>

<%@ include file="_end.jspf" %>
