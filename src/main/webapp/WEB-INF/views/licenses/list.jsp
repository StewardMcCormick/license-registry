<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/layout/head.jsp" %>
    <title>Лицензии</title>
</head>
<body>
<%@ include file="/WEB-INF/views/layout/global-navbar.jsp" %>
<%@ include file="/WEB-INF/views/layout/org-navbar.jsp" %>
<main class="container">
    <h1>Реестр лицензий</h1>

    <p><a href="${pageContext.request.contextPath}/org/${orgId}/licenses/new">Добавить лицензию</a></p>

    <form method="get" action="${pageContext.request.contextPath}/org/${orgId}/licenses">
        <select name="status">
            <option value="">Любой статус</option>
            <option value="ACTIVE"<c:if test="${param.status eq 'ACTIVE'}"> selected</c:if>>Действует</option>
            <option value="EXPIRING"<c:if test="${param.status eq 'EXPIRING'}"> selected</c:if>>Истекает</option>
            <option value="EXPIRED"<c:if test="${param.status eq 'EXPIRED'}"> selected</c:if>>Просрочена</option>
            <option value="CANCELLED"<c:if test="${param.status eq 'CANCELLED'}"> selected</c:if>>Отменена</option>
        </select>
        <select name="typeId">
            <option value="">Любой тип</option>
            <c:forEach var="type" items="${types}">
                <option value="${type.id}"><c:out value="${type.displayName}"/></option>
            </c:forEach>
        </select>
        <input type="text" name="q" placeholder="Поиск по названию">

        <c:if test="${not empty tags}">
            <fieldset>
                <legend>Теги</legend>
                <c:forEach var="tag" items="${tags}">
                    <label>
                        <input type="checkbox" name="tagIds" value="${tag.id}"<c:if test="${not empty selectedTagIds and selectedTagIds.contains(tag.id)}"> checked</c:if>>
                        <c:out value="${tag.name}"/>
                    </label>
                </c:forEach>
            </fieldset>
        </c:if>

        <button type="submit">Найти</button>
    </form>

    <table>
        <tr><th>Название</th><th>Статус</th><th>Истекает</th><th>Ссылка</th></tr>
        <c:forEach var="license" items="${licenses}">
            <tr>
                <td><c:out value="${license.title}"/></td>
                <td><c:out value="${license.status}"/></td>
                <td><c:out value="${license.expiresAt}"/></td>
                <td><a href="${pageContext.request.contextPath}/org/${orgId}/licenses/${license.id}">Открыть</a></td>
            </tr>
        </c:forEach>
    </table>
</main>
</body>
</html>
