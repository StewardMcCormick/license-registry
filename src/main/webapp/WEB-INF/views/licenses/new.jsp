<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/layout/head.jsp" %>
    <title>Новая лицензия</title>
</head>
<body>
<%@ include file="/WEB-INF/views/layout/global-navbar.jsp" %>
<%@ include file="/WEB-INF/views/layout/org-navbar.jsp" %>
<main class="container">
    <h1>Новая лицензия</h1>

    <c:if test="${not empty error}">
        <p class="alert error"><c:out value="${error}"/></p>
    </c:if>

    <form method="post" action="${pageContext.request.contextPath}/org/${orgId}/licenses/new">
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <p>Название: <input type="text" name="title" value="<c:out value='${formTitle}'/>" required></p>
        <p>Дата выдачи: <input type="date" name="issuedAt" value="<c:out value='${formIssuedAt}'/>" required></p>
        <p>Дата истечения: <input type="date" name="expiresAt" value="<c:out value='${formExpiresAt}'/>" required></p>
        <fieldset>
            <legend>Типы</legend>
            <c:forEach var="type" items="${types}">
                <label>
                    <input type="checkbox" name="typeIds" value="${type.id}"<c:if test="${not empty formTypeIds and formTypeIds.contains(type.id)}"> checked</c:if>>
                    <c:out value="${type.displayName}"/>
                </label>
            </c:forEach>
        </fieldset>
        <p>Теги (через запятую): <input type="text" name="tags" value="<c:out value='${formTags}'/>"></p>
        <p>Заметки:</p>
        <p><textarea name="notes"><c:out value="${formNotes}"/></textarea></p>
        <button type="submit">Сохранить</button>
    </form>
</main>
</body>
</html>
