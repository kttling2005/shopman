<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="Báo cáo thống kê"/>
<%@ include file="_start.jspf" %>

<div class="crumbs">Quản lý / Báo cáo</div>
<div class="page-head"><div><h1>Báo cáo thống kê</h1><p>Chọn loại thống kê và khoảng thời gian cần xem.</p></div></div>

<c:if test="${not empty error}">
  <div class="alert err" style="margin-bottom:16px"><div><b><c:out value="${error}"/></b></div></div>
</c:if>

<form method="get" action="${pageContext.request.contextPath}/manager/statistics">
  <div class="split">
    <div>
      <label class="card" style="display:block;margin-bottom:10px;padding:14px 16px;cursor:pointer">
        <div class="row between">
          <div><b>Thống kê mặt hàng theo doanh thu</b><div class="small muted">Doanh thu từng mặt hàng, xem giao dịch chi tiết</div></div>
          <input type="radio" name="type" value="revenue" checked>
        </div>
      </label>
      <label class="card" style="display:block;margin-bottom:10px;padding:14px 16px;cursor:pointer">
        <div class="row between">
          <div><b>Thống kê hàng nhập</b><div class="small muted">Theo phiếu nhập và nhà cung cấp · <span class="badge">Bản mẫu</span></div></div>
          <input type="radio" name="type" value="import">
        </div>
      </label>
      <label class="card" style="display:block;margin-bottom:10px;padding:14px 16px;cursor:pointer">
        <div class="row between">
          <div><b>Thống kê hàng xuất</b><div class="small muted">Hàng đã bán theo thời gian · <span class="badge">Bản mẫu</span></div></div>
          <input type="radio" name="type" value="export">
        </div>
      </label>
      <label class="card" style="display:block;margin-bottom:10px;padding:14px 16px;cursor:pointer">
        <div class="row between">
          <div><b>Thống kê khách hàng</b><div class="small muted">Theo số lượng và giá trị đơn · <span class="badge">Bản mẫu</span></div></div>
          <input type="radio" name="type" value="customer">
        </div>
      </label>
      <label class="card" style="display:block;margin-bottom:10px;padding:14px 16px;cursor:pointer">
        <div class="row between">
          <div><b>Thống kê nhà cung cấp</b><div class="small muted">Theo số lượng hàng cung cấp · <span class="badge">Bản mẫu</span></div></div>
          <input type="radio" name="type" value="supplier">
        </div>
      </label>
    </div>

    <div class="card">
      <h3 style="margin-bottom:14px">Khoảng thời gian</h3>
      <div class="field">
        <label for="startDate">Ngày bắt đầu</label>
        <input id="startDate" name="startDate" type="date" class="input" value="<c:out value='${startDate}'/>">
      </div>
      <div class="field">
        <label for="endDate">Ngày kết thúc</label>
        <input id="endDate" name="endDate" type="date" class="input" value="<c:out value='${endDate}'/>">
      </div>
      <button class="btn primary block" type="submit">Xem thống kê</button>
      <p class="hint mt12">Khoảng thời gian áp dụng cho thống kê theo doanh thu. Các loại còn lại là bản mẫu giao diện.</p>
    </div>
  </div>
</form>

<%@ include file="_end.jspf" %>
