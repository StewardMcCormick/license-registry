<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<html>
<head><meta charset="UTF-8"><title>Новая лицензия</title></head>
<body>
<%@ include file="/WEB-INF/views/layout/global-navbar.jsp" %>
<%@ include file="/WEB-INF/views/layout/org-navbar.jsp" %>
<h1>Новая лицензия</h1>
<c:if test="${not empty error}">
    <p style="color:red"><c:out value="${error}"/></p>
</c:if>

<form method="post" action="${pageContext.request.contextPath}/org/${orgId}/licenses/new">
    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
    <p><input type="text" name="title" placeholder="Название" value="<c:out value='${formTitle}'/>" required></p>
    <p>Дата выдачи: <input type="date" name="issuedAt" value="<c:out value='${formIssuedAt}'/>" required></p>
    <p>Дата истечения: <input type="date" name="expiresAt" value="<c:out value='${formExpiresAt}'/>" required></p>
    <p>Типы:
        <c:forEach var="type" items="${types}">
            <label><input type="checkbox" name="typeIds" value="${type.id}"<c:if test="${not empty formTypeIds and formTypeIds.contains(type.id)}"> checked</c:if>> <c:out value="${type.displayName}"/></label>
        </c:forEach>
    </p>
    <p>Теги (через запятую): <input type="text" name="tags" value="<c:out value='${formTags}'/>"></p>
    <p>Заметки: <textarea name="notes"><c:out value="${formNotes}"/></textarea></p>
    <button type="submit">Сохранить</button>
</form>
</body>
</html>
