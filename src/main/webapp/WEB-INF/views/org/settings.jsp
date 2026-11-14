<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<html>
<head><meta charset="UTF-8"><title>Организация</title></head>
<body>
<%@ include file="/WEB-INF/views/layout/global-navbar.jsp" %>
<%@ include file="/WEB-INF/views/layout/org-navbar.jsp" %>
<h1><c:out value="${organization.name}"/></h1>
<p>Join-код: <b><c:out value="${organization.joinCode}"/></b></p>

<form method="post" action="${pageContext.request.contextPath}/org/${organization.id}/reissue-code">
    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
    <button type="submit">Перевыпустить код</button>
</form>

<h2>Участники</h2>
<table border="1">
    <tr><th>ID</th><th>Фамилия</th><th>Имя</th><th>Отчество</th><th>Роль</th><th>Действие</th></tr>
    <c:forEach var="member" items="${members}">
        <tr>
            <td><c:out value="${member.userId}"/></td>
            <td><c:out value="${member.user.lastName}"/></td>
            <td><c:out value="${member.user.firstName}"/></td>
            <td><c:out value="${member.user.middleName}"/></td>
            <td><c:out value="${member.memberRole}"/></td>
            <td>
                <c:choose>
                    <c:when test="${member.memberRole != 'ADMIN'}">
                        <form method="post" action="${pageContext.request.contextPath}/org/${organization.id}/promote">
                            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                            <input type="hidden" name="userId" value="${member.userId}">
                            <button type="submit">Сделать админом</button>
                        </form>
                    </c:when>
                    <c:otherwise>
                        <form method="post" action="${pageContext.request.contextPath}/org/${organization.id}/lower">
                            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
                            <input type="hidden" name="userId" value="${member.userId}">
                            <button type="submit">Сделать менеджером</button>
                        </form>
                    </c:otherwise>
                </c:choose>
            </td>
        </tr>
    </c:forEach>
</table>
</body>
</html>