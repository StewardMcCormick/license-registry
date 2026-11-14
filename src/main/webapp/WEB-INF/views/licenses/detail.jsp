<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<html>
<head><meta charset="UTF-8"><title>Лицензия</title></head>
<body>
<%@ include file="/WEB-INF/views/layout/global-navbar.jsp" %>
<%@ include file="/WEB-INF/views/layout/org-navbar.jsp" %>
<h1><c:out value="${license.title}"/></h1>
<p>Статус: <c:out value="${license.status}"/></p>
<p>Выдана: <c:out value="${license.issuedAt}"/></p>
<p>Истекает: <c:out value="${license.expiresAt}"/></p>
<p>Заметки: <c:out value="${license.notes}"/></p>

<p>Типы:
    <c:forEach var="type" items="${types}"><c:out value="${type.displayName}"/> </c:forEach>
</p>
<p>Теги:
    <c:forEach var="tag" items="${tags}"><c:out value="${tag.name}"/> </c:forEach>
</p>

<p><a href="${pageContext.request.contextPath}/org/${license.orgId}/licenses/${license.id}/edit">Редактировать</a></p>

<c:if test="${license.status ne 'CANCELLED'}">
    <form method="post" action="${pageContext.request.contextPath}/org/${license.orgId}/licenses/${license.id}/cancel">
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <button type="submit">Отменить лицензию</button>
    </form>
</c:if>

<c:if test="${license.status eq 'CANCELLED'}">
    <form method="post" action="${pageContext.request.contextPath}/org/${license.orgId}/licenses/${license.id}/restore">
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <button type="submit">Восстановить</button>
    </form>
</c:if>

<form method="post" action="${pageContext.request.contextPath}/org/${license.orgId}/licenses/${license.id}/delete">
    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
    <button type="submit">Удалить</button>
</form>

<p><a href="${pageContext.request.contextPath}/org/${license.orgId}/licenses">Назад к списку</a></p>
</body>
</html>