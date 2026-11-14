<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<html>
<head><meta charset="UTF-8"><title>Регистрация</title></head>
<body>
<h1>Регистрация</h1>
<c:if test="${not empty error}">
    <p style="color:red"><c:out value="${error}"/></p>
</c:if>
<form method="post" action="${pageContext.request.contextPath}/register">
    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
    <p><input type="email" name="email" placeholder="Email" value="<c:out value='${email}'/>" required></p>
    <p><input type="password" name="password" placeholder="Пароль" required></p>
    <p><input type="text" name="lastName" placeholder="Фамилия" value="<c:out value='${lastName}'/>" required></p>
    <p><input type="text" name="firstName" placeholder="Имя" value="<c:out value='${firstName}'/>" required></p>
    <p><input type="text" name="middleName" placeholder="Отчество" value="<c:out value='${middleName}'/>"></p>
    <button type="submit">Зарегистрироваться</button>
</form>
<p><a href="${pageContext.request.contextPath}/login">Уже есть аккаунт? Войти</a></p>
</body>
</html>
