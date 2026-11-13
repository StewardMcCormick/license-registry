<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<html>
<head>
    <meta charset="UTF-8">
    <title>Мои организации</title>
</head>
<body>
<%@ include file="/WEB-INF/views/layout/global-navbar.jsp" %>
<h1>Мои организации</h1>

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
</body>
</html>
