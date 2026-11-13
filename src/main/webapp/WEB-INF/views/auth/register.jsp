<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<html>
<head><meta charset="UTF-8"><title>Регистрация</title></head>
<body>
<h1>Регистрация</h1>
<c:if test="${not empty error}">
    <p style="color:red">${error}</p>
</c:if>
<form method="post" action="${pageContext.request.contextPath}/register">
    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
    <p><input type="email" name="email" placeholder="Email" required></p>
    <p><input type="password" name="password" placeholder="Пароль" required></p>
    <p><input type="text" name="lastName" placeholder="Фамилия" required></p>
    <p><input type="text" name="firstName" placeholder="Имя" required></p>
    <p><input type="text" name="middleName" placeholder="Отчество"></p>
    <button type="submit">Зарегистрироваться</button>
</form>
<p><a href="${pageContext.request.contextPath}/login">Уже есть аккаунт? Войти</a></p>
</body>
</html>
