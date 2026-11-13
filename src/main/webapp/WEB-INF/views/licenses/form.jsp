<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<html>
<head><title>Лицензия</title></head>
<body>
<h1><c:choose><c:when test="${not empty license}">Редактирование лицензии</c:when><c:otherwise>Новая лицензия</c:otherwise></c:choose></h1>
<c:if test="${not empty error}">
    <p style="color:red">${error}</p>
</c:if>

<form method="post" action="${pageContext.request.contextPath}${not empty license ? '/licenses/'.concat(license.id).concat('/edit') : '/licenses/new'}">
    <input type="hidden" name="csrfToken" value="${sessionScope.csrfToken}">
    <p><input type="text" name="title" placeholder="Название" value="${license.title}" required></p>
    <p>Дата выдачи: <input type="date" name="issuedAt" value="${license.issuedAt}" required></p>
    <p>Дата истечения: <input type="date" name="expiresAt" value="${license.expiresAt}" required></p>
    <p>Типы:
        <c:forEach var="type" items="${types}">
            <label><input type="checkbox" name="typeIds" value="${type.id}">${type.displayName}</label>
        </c:forEach>
    </p>
    <p>Теги (через запятую): <input type="text" name="tags"></p>
    <p>Заметки: <textarea name="notes">${license.notes}</textarea></p>
    <button type="submit">Сохранить</button>
</form>
</body>
</html>
