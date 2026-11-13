<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<html>
<head>
    <title>Главная</title>
</head>
<body>
<h1>Главная</h1>

<h2>Пользователь</h2>
<p><strong>ФИО:</strong> ${currentUser.fullName}</p>
<p><strong>Email:</strong> ${currentUser.email}</p>

<form method="post" action="${pageContext.request.contextPath}/logout">
    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
    <button type="submit">Выйти</button>
</form>

<h2>Мои организации</h2>
<c:choose>
    <c:when test="${empty organizations}">
        <p>Вы пока не состоите ни в одной организации.</p>
    </c:when>
    <c:otherwise>
        <table border="1">
            <tr>
                <th>Название</th>
                <th>Роль</th>
                <th></th>
            </tr>
            <c:forEach var="org" items="${organizations}">
                <tr>
                    <td>${org.name}</td>
                    <td>${rolesByOrgId[org.id]}</td>
                    <td><a href="${pageContext.request.contextPath}/org/${org.id}/licenses">Открыть</a></td>
                </tr>
            </c:forEach>
        </table>
    </c:otherwise>
</c:choose>

<h2>Онбординг</h2>
<c:if test="${not empty error}">
    <p style="color:red">${error}</p>
</c:if>

<h3>Создать организацию</h3>
<form method="post" action="${pageContext.request.contextPath}/create-organization">
    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
    <p><input type="text" name="name" placeholder="Название организации" required></p>
    <button type="submit">Создать</button>
</form>

<h3>Присоединиться по join-коду</h3>
<form method="post" action="${pageContext.request.contextPath}/join-organization">
    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
    <p><input type="text" name="joinCode" placeholder="Join-код" required></p>
    <button type="submit">Присоединиться</button>
</form>
</body>
</html>
