<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<html>
<head><meta charset="UTF-8"><title>Админка</title></head>
<body>
<%@ include file="/WEB-INF/views/layout/global-navbar.jsp" %>
<h1>Дашборд суперадмина</h1>

<p>Пользователей: <c:out value="${stats.userCount}"/></p>
<p>Организаций: <c:out value="${stats.organizationCount}"/></p>
<p>Лицензий: <c:out value="${stats.licenseCount}"/></p>
<p>Истекающих: <c:out value="${stats.expiringCount}"/></p>
<p>Просроченных: <c:out value="${stats.expiredCount}"/></p>

<h2>Организации</h2>
<table border="1">
    <tr><th>Название</th><th>Админов</th><th>Менеджеров</th><th>Лицензий</th><th>Join-код</th><th>Действия</th></tr>
    <c:forEach var="summary" items="${organizations}">
        <tr>
            <td><c:out value="${summary.organization.name}"/></td>
            <td><c:out value="${summary.adminCount}"/></td>
            <td><c:out value="${summary.managerCount}"/></td>
            <td><c:out value="${summary.licenseCount}"/></td>
            <td><c:out value="${summary.organization.joinCode}"/></td>
            <td>
                <a href="${pageContext.request.contextPath}/org/${summary.organization.id}">Открыть</a> |
                <a href="${pageContext.request.contextPath}/org/${summary.organization.id}/licenses">Лицензии</a>
                <form method="post" action="${pageContext.request.contextPath}/admin/reissue-code" style="display:inline">
                    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                    <input type="hidden" name="orgId" value="${summary.organization.id}">
                    <button type="submit">Перевыпустить код</button>
                </form>
            </td>
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