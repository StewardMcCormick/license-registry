<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/layout/head.jsp" %>
    <title>Типы лицензий</title>
</head>
<body>
<%@ include file="/WEB-INF/views/layout/global-navbar.jsp" %>
<%@ include file="/WEB-INF/views/layout/org-navbar.jsp" %>
<main class="container">
    <h1>Типы лицензий</h1>

    <c:if test="${not empty error}">
        <p class="alert error"><c:out value="${error}"/></p>
    </c:if>

    <table>
        <tr><th>Код</th><th>Название</th></tr>
        <c:forEach var="type" items="${types}">
            <tr>
                <td><c:out value="${type.code}"/></td>
                <td><c:out value="${type.name}"/></td>
            </tr>
        </c:forEach>
    </table>

    <c:if test="${orgRole eq 'ADMIN' or sessionScope.currentUser.superadmin}">
        <h2>Добавить тип лицензии</h2>
        <form method="post" action="${pageContext.request.contextPath}/org/${orgId}/licenses/types">
            <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
            <p><input type="text" name="code" placeholder="Код типа (например, SOFTWARE)" required></p>
            <p><input type="text" name="name" placeholder="Название типа (например, Лицензия на ПО)" required></p>
            <button type="submit">Добавить</button>
        </form>
    </c:if>
</main>
</body>
</html>
