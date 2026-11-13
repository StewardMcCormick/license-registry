<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<html>
<head><title>Типы лицензий</title></head>
<body>
<h1>Реестр лицензий</h1>

<p>
    <a href="${pageContext.request.contextPath}/licenses">Лицензии</a> |
    <strong>Типы лицензий</strong>
</p>

<c:if test="${not empty error}">
    <p style="color:red">${error}</p>
</c:if>

<table border="1">
    <tr><th>Код</th><th>Название</th></tr>
    <c:forEach var="type" items="${types}">
        <tr>
            <td>${type.code}</td>
            <td>${type.name}</td>
        </tr>
    </c:forEach>
</table>

<h2>Добавить тип лицензии</h2>
<form method="post" action="${pageContext.request.contextPath}/licenses/types">
    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
    <input type="text" name="code" placeholder="Код типа (например, SOFTWARE)" required>
    <input type="text" name="name" placeholder="Название типа (например, Лицензия на ПО)" required>
    <button type="submit">Добавить</button>
</form>
</body>
</html>
