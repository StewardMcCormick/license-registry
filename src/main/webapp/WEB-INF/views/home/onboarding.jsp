<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<html>
<head>
    <%@ include file="/WEB-INF/views/layout/head.jsp" %>
    <title>Онбординг</title>
</head>
<body>
<%@ include file="/WEB-INF/views/layout/global-navbar.jsp" %>
<main class="container">
    <h1>Онбординг</h1>

    <c:if test="${not empty error}">
        <p class="alert error"><c:out value="${error}"/></p>
    </c:if>

    <h2>Создать организацию</h2>
    <form method="post" action="${pageContext.request.contextPath}/org">
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <p><input type="text" name="name" placeholder="Название организации" required></p>
        <button type="submit">Создать</button>
    </form>

    <h2>Присоединиться по join-коду</h2>
    <form method="post" action="${pageContext.request.contextPath}/join-organization">
        <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
        <p><input type="text" name="joinCode" placeholder="Join-код" required></p>
        <button type="submit">Присоединиться</button>
    </form>
</main>
</body>
</html>
