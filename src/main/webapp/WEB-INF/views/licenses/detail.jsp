<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<html>
<head><meta charset="UTF-8"><title>Лицензия</title></head>
<body>
<%@ include file="/WEB-INF/views/layout/global-navbar.jsp" %>
<%@ include file="/WEB-INF/views/layout/org-navbar.jsp" %>
<h1>${license.title}</h1>
<p>Статус: ${license.status}</p>
<p>Выдана: ${license.issuedAt}</p>
<p>Истекает: ${license.expiresAt}</p>
<p>Заметки: ${license.notes}</p>

<p>Типы:
    <c:forEach var="type" items="${types}">${type.displayName} </c:forEach>
</p>
<p>Теги:
    <c:forEach var="tag" items="${tags}">${tag.name} </c:forEach>
</p>

<p><a href="${pageContext.request.contextPath}/org/${license.orgId}/licenses/${license.id}/edit">Редактировать</a></p>

<form method="post" action="${pageContext.request.contextPath}/org/${license.orgId}/licenses/${license.id}/cancel">
    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
    <button type="submit">Отменить лицензию</button>
</form>

<form method="post" action="${pageContext.request.contextPath}/org/${license.orgId}/licenses/${license.id}/restore">
    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
    <button type="submit">Восстановить</button>
</form>

<form method="post" action="${pageContext.request.contextPath}/org/${license.orgId}/licenses/${license.id}/delete">
    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
    <button type="submit">Удалить</button>
</form>

<p><a href="${pageContext.request.contextPath}/org/${license.orgId}/licenses">Назад к списку</a></p>
</body>
</html>
