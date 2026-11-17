<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/layout/head.jsp" %>
    <title>Вход</title>
</head>
<body>
<div class="card">
    <h1>Вход</h1>

    <c:if test="${not empty error}">
        <p class="alert error"><c:out value="${error}"/></p>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/login">
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <p><input type="email" name="email" placeholder="Email" required></p>
        <p><input type="password" name="password" placeholder="Пароль" required></p>
        <button type="submit">Войти</button>
    </form>

    <p class="footer-link">
        <a href="${pageContext.request.contextPath}/register">Нет аккаунта? Зарегистрироваться</a>
    </p>
</div>
</body>
</html>
