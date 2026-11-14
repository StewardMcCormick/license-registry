<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<html>
<head><meta charset="UTF-8"><title>Ошибка</title></head>
<body>
<%@ include file="/WEB-INF/views/layout/global-navbar.jsp" %>
<h1>Ошибка</h1>
<p>Статус: <c:out value="${status}"/></p>
<p><c:out value="${message}"/></p>
<a href="${pageContext.request.contextPath}/">На главную</a>
</body>
</html>
