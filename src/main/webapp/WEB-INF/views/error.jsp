<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/layout/head.jsp" %>
    <title>Ошибка</title>
</head>
<body>
<%@ include file="/WEB-INF/views/layout/global-navbar.jsp" %>
<main class="container">
    <h1>Ошибка</h1>
    <p class="alert error">Статус: <c:out value="${status}"/></p>
    <p><c:out value="${message}"/></p>
    <p><a href="${pageContext.request.contextPath}/">На главную</a></p>
</main>
</body>
</html>
