<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>Главная</title>
</head>
<body>
<%@ include file="/WEB-INF/views/layout/global-navbar.jsp" %>
<h1>Главная</h1>

<h2>Пользователь</h2>
<p><strong>ФИО:</strong> ${sessionScope.currentUser.fullName}</p>
<p><strong>Email:</strong> ${sessionScope.currentUser.email}</p>
<p><strong>Роль:</strong> ${sessionScope.currentUser.globalRole}</p>

<form method="post" action="${pageContext.request.contextPath}/logout" style="display:inline">
    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
    <button type="submit">Выйти</button>
</form>
</body>
</html>
