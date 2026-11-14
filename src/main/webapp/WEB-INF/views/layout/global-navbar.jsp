<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:if test="${not empty sessionScope.currentUser}">
    <nav>
        <a href="${pageContext.request.contextPath}/">Главная</a> |
        <a href="${pageContext.request.contextPath}/org">Мои организации</a> |
        <a href="${pageContext.request.contextPath}/onboarding">Онбординг</a>
        <c:if test="${sessionScope.currentUser.superadmin}">
            | <a href="${pageContext.request.contextPath}/admin">Панель администратора</a>
        </c:if>
    </nav>
    <hr>
</c:if>
