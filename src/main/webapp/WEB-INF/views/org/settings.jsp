<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<html>
<head><meta charset="UTF-8"><title>Организация</title></head>
<body>
<%@ include file="/WEB-INF/views/layout/global-navbar.jsp" %>
<%@ include file="/WEB-INF/views/layout/org-navbar.jsp" %>
<h1>${organization.name}</h1>
<p>Join-код: <b>${organization.joinCode}</b></p>

<form method="post" action="${pageContext.request.contextPath}/org/${organization.id}/reissue-code">
    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
    <button type="submit">Перевыпустить код</button>
</form>

<h2>Участники</h2>
<table border="1">
    <tr><th>ID</th><th>Фамилия</th><th>Имя</th><th>Отчество</th><th>Роль</th><th>Действие</th></tr>
    <c:forEach var="member" items="${members}">
        <tr>
            <td>${member.userId}</td>
            <td>${member.user.firstName}</td>
            <td>${member.user.lastName}</td>
            <td>${member.user.middleName}</td>
            <td>${member.memberRole}</td>
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
