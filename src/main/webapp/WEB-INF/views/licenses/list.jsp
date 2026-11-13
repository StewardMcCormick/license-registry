<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<html>
<head><meta charset="UTF-8"><title>Лицензии</title></head>
<body>
<%@ include file="/WEB-INF/views/layout/global-navbar.jsp" %>
<%@ include file="/WEB-INF/views/layout/org-navbar.jsp" %>
<h1>Реестр лицензий</h1>

<p><a href="${pageContext.request.contextPath}/org/${orgId}/licenses/new">Добавить лицензию</a></p>

<form method="get" action="${pageContext.request.contextPath}/org/${orgId}/licenses">
    <select name="status">
        <option value="">Любой статус</option>
        <option value="ACTIVE">ACTIVE</option>
        <option value="EXPIRING">EXPIRING</option>
        <option value="EXPIRED">EXPIRED</option>
        <option value="CANCELLED">CANCELLED</option>
    </select>
    <select name="typeId">
        <option value="">Любой тип</option>
        <c:forEach var="type" items="${types}">
            <option value="${type.id}">${type.displayName}</option>
        </c:forEach>
    </select>
    <select name="tagId">
        <option value="">Любой тег</option>
        <c:forEach var="tag" items="${tags}">
            <option value="${tag.id}">${tag.name}</option>
        </c:forEach>
    </select>
    <input type="text" name="q" placeholder="Поиск по названию">
    <button type="submit">Найти</button>
</form>

<table border="1">
    <tr><th>Название</th><th>Статус</th><th>Истекает</th><th></th></tr>
    <c:forEach var="license" items="${licenses}">
        <tr>
            <td>${license.title}</td>
            <td>${license.status}</td>
            <td>${license.expiresAt}</td>
            <td><a href="${pageContext.request.contextPath}/org/${orgId}/licenses/${license.id}">Открыть</a></td>
        </tr>
    </c:forEach>
</table>
</body>
</html>
