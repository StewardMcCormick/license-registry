<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<html>
<head><meta charset="UTF-8"><title>Лицензия</title></head>
<body>
<%@ include file="/WEB-INF/views/layout/global-navbar.jsp" %>
<%@ include file="/WEB-INF/views/layout/org-navbar.jsp" %>
<h1><c:choose><c:when test="${not empty license}">Редактирование лицензии</c:when><c:otherwise>Новая лицензия</c:otherwise></c:choose></h1>
<c:if test="${not empty error}">
    <p style="color:red"><c:out value="${error}"/></p>
</c:if>

<c:choose>
    <c:when test="${not empty license}">
        <c:set var="formAction" value="${pageContext.request.contextPath}/org/${license.orgId}/licenses/${license.id}/edit"/>
    </c:when>
    <c:otherwise>
        <c:set var="formAction" value="${pageContext.request.contextPath}/org/${orgId}/licenses/new"/>
    </c:otherwise>
</c:choose>

<form method="post" action="${formAction}">
    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
    <p><input type="text" name="title" placeholder="Название" value="<c:out value='${license.title}'/>" required></p>
    <p>Дата выдачи: <input type="date" name="issuedAt" value="<c:out value='${license.issuedAt}'/>" required></p>
    <p>Дата истечения: <input type="date" name="expiresAt" value="<c:out value='${license.expiresAt}'/>" required></p>
    <p>Типы:
        <c:forEach var="type" items="${types}">
            <c:set var="checked" value="false"/>
            <c:forEach var="selectedType" items="${selectedTypes}">
                <c:if test="${selectedType.id eq type.id}">
                    <c:set var="checked" value="true"/>
                </c:if>
            </c:forEach>
            <label><input type="checkbox" name="typeIds" value="${type.id}"<c:if test="${checked}"> checked</c:if>> <c:out value="${type.displayName}"/></label>
        </c:forEach>
    </p>
    <p>Теги (через запятую): <input type="text" name="tags" value="<c:out value='${tagsValue}'/>"></p>
    <p>Заметки: <textarea name="notes"><c:out value="${license.notes}"/></textarea></p>
    <button type="submit">Сохранить</button>
</form>
</body>
</html>