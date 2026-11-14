<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<html>
<head><meta charset="UTF-8"><title>Вход</title></head>
<body>
<h1>Вход</h1>
<c:if test="${not empty error}">
    <p style="color:red"><c:out value="${error}"/></p>
</c:if>
<form method="post" action="${pageContext.request.contextPath}/login">
    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
    <p><input type="email" name="email" placeholder="Email" required></p>
    <p><input type="password" name="password" placeholder="Пароль" required></p>
    <button type="submit">Войти</button>
</form>
<p><a href="${pageContext.request.contextPath}/register">Нет аккаунта? Зарегистрироваться</a></p>
</body>
</html>
