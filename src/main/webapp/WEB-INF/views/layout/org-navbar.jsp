<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:if test="${not empty orgId}">
    <c:set var="orgRole" value=""/>
    <c:forEach var="entry" items="${sessionScope.currentUser.memberRoleMap}">
        <c:if test="${entry.key eq orgId}">
            <c:set var="orgRole" value="${entry.value}"/>
        </c:if>
    </c:forEach>
    <nav>
        <a href="${pageContext.request.contextPath}/org/${orgId}/licenses">Лицензии</a> |
        <a href="${pageContext.request.contextPath}/org/${orgId}/licenses/types">Типы лицензий</a>
        <c:if test="${orgRole eq 'ADMIN'}">
            | <a href="${pageContext.request.contextPath}/org/${orgId}">Сотрудники</a>
        </c:if>
    </nav>
    <hr>
</c:if>
