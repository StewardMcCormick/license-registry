<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/layout/head.jsp" %>
    <title>Админка</title>
</head>
<body>
<%@ include file="/WEB-INF/views/layout/global-navbar.jsp" %>
<main class="container">
    <h1>Дашборд суперадмина</h1>

    <div class="stats">
        <p>Пользователей: <strong><c:out value="${stats.userCount}"/></strong></p>
        <p>Организаций: <strong><c:out value="${stats.organizationCount}"/></strong></p>
        <p>Лицензий: <strong><c:out value="${stats.licenseCount}"/></strong></p>
        <p>Истекающих: <strong><c:out value="${stats.expiringCount}"/></strong></p>
        <p>Просроченных: <strong><c:out value="${stats.expiredCount}"/></strong></p>
    </div>

    <h2>Организации</h2>
    <table>
        <tr><th>Название</th><th>Админов</th><th>Менеджеров</th><th>Лицензий</th><th>Join-код</th><th>Действия</th></tr>
        <c:forEach var="summary" items="${organizations}">
            <tr>
                <td><c:out value="${summary.organization.name}"/></td>
                <td><c:out value="${summary.adminCount}"/></td>
                <td><c:out value="${summary.managerCount}"/></td>
                <td><c:out value="${summary.licenseCount}"/></td>
                <td><c:out value="${summary.organization.joinCode}"/></td>
                <td>
                    <a href="${pageContext.request.contextPath}/org/${summary.organization.id}">Открыть</a>
                    <a href="${pageContext.request.contextPath}/org/${summary.organization.id}/licenses">Лицензии</a>
                    <form class="inline" method="post" action="${pageContext.request.contextPath}/admin/reissue-code">
                        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                        <input type="hidden" name="orgId" value="${summary.organization.id}">
                        <button type="submit" class="link">Перевыпустить код</button>
                    </form>
                </td>
            </tr>
        </c:forEach>
    </table>

    <h2>Выдать суперадмина</h2>
    <form method="post" action="${pageContext.request.contextPath}/admin/grant-superadmin">
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <input type="text" name="userId" placeholder="ID пользователя">
        <button type="submit">Выдать</button>
    </form>

    <h2>Снять суперадмина</h2>
    <form method="post" action="${pageContext.request.contextPath}/admin/revoke-superadmin">
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <input type="text" name="userId" placeholder="ID пользователя">
        <button type="submit">Снять</button>
    </form>
</main>
</body>
</html>
