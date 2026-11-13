<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<html>
<head><title>Админка</title></head>
<body>
<h1>Дашборд суперадмина</h1>

<p>Пользователей: ${stats.userCount}</p>
<p>Организаций: ${stats.organizationCount}</p>
<p>Лицензий: ${stats.licenseCount}</p>
<p>Истекающих: ${stats.expiringCount}</p>
<p>Просроченных: ${stats.expiredCount}</p>

<h2>Организации</h2>
<table border="1">
    <tr><th>Название</th><th>Админов</th><th>Менеджеров</th><th>Лицензий</th><th>Join-код</th><th></th></tr>
    <c:forEach var="summary" items="${organizations}">
        <tr>
            <td>${summary.organization.name}</td>
            <td>${summary.adminCount}</td>
            <td>${summary.managerCount}</td>
            <td>${summary.licenseCount}</td>
            <td>${summary.organization.joinCode}</td>
            <td><a href="${pageContext.request.contextPath}/org/${summary.organization.id}">Открыть</a></td>
        </tr>
    </c:forEach>
</table>

<h2>Выдать/снять суперадмина</h2>
<form method="post" action="${pageContext.request.contextPath}/admin/grant-superadmin">
    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
    <input type="text" name="userId" placeholder="ID пользователя">
    <button type="submit">Выдать суперадмина</button>
</form>
<form method="post" action="${pageContext.request.contextPath}/admin/revoke-superadmin">
    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
    <input type="text" name="userId" placeholder="ID пользователя">
    <button type="submit">Снять суперадмина</button>
</form>
</body>
</html>
