<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/layout/head.jsp" %>
    <title>Главная</title>
</head>
<body>
<%@ include file="/WEB-INF/views/layout/global-navbar.jsp" %>
<main class="container">
    <h1>Главная</h1>

    <h2>Пользователь</h2>
    <p><strong>ФИО:</strong> <c:out value="${sessionScope.currentUser.fullName}"/></p>
    <p><strong>Email:</strong> <c:out value="${sessionScope.currentUser.email}"/></p>
    <p><strong>Роль:</strong> <c:out value="${sessionScope.currentUser.globalRole}"/></p>

    <form method="post" action="${pageContext.request.contextPath}/logout">
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <button type="submit">Выйти</button>
    </form>
</main>
</body>
</html>
