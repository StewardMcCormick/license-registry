<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<html>
<head><title>Начало работы</title></head>
<body>
<h1>Создайте организацию или присоединитесь к существующей</h1>
<c:if test="${not empty error}">
    <p style="color:red">${error}</p>
</c:if>

<h2>Создать организацию</h2>
<form method="post" action="${pageContext.request.contextPath}/onboarding/create-organization">
    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
    <p><input type="text" name="name" placeholder="Название организации" required></p>
    <button type="submit">Создать</button>
</form>

<h2>Присоединиться по join-коду</h2>
<form method="post" action="${pageContext.request.contextPath}/onboarding/join-organization">
    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
    <p><input type="text" name="joinCode" placeholder="Join-код" required></p>
    <button type="submit">Присоединиться</button>
</form>
</body>
</html>
