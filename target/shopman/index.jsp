<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- Landing page. Always REDIRECT (never forward): the static screens use relative links such as
     ../assets/css/styles.css, which only resolve correctly when the browser's address is /ui/... --%>
<c:choose>
  <c:when test="${sessionScope.role == 'manager'}">
    <c:redirect url="/manager/statistics"/>
  </c:when>
  <c:otherwise>
    <c:redirect url="/ui/home.jsp"/>
  </c:otherwise>
</c:choose>
