<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<html>
<head><meta charset="UTF-8"><title>Типы лицензий</title></head>
<body>
<%@ include file="/WEB-INF/views/layout/global-navbar.jsp" %>
<%@ include file="/WEB-INF/views/layout/org-navbar.jsp" %>
<h1>Типы лицензий</h1>

<c:if test="${not empty error}">
    <p style="color:red"><c:out value="${error}"/></p>
</c:if>

<table border="1">
    <tr><th>Код</th><th>Название</th></tr>
    <c:forEach var="type" items="${types}">
        <tr>
            <td><c:out value="${type.code}"/></td>
            <td><c:out value="${type.name}"/></td>
        </tr>
    </c:forEach>
</table>

<h2>Добавить тип лицензии</h2>
<form method="post" action="${pageContext.request.contextPath}/org/${orgId}/licenses/types">
    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
    <input type="text" name="code" placeholder="Код типа (например, SOFTWARE)" required>
    <input type="text" name="name" placeholder="Название типа (например, Лицензия на ПО)" required>
    <button type="submit">Добавить</button>
</form>
</body>
</html>