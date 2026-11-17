<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:if test="${not empty orgId}">
    <div class="subbar">
        <nav>
            <a href="${pageContext.request.contextPath}/org/${orgId}/licenses">Лицензии</a>
            <a href="${pageContext.request.contextPath}/org/${orgId}/licenses/types">Типы лицензий</a>
            <c:if test="${orgRole eq 'ADMIN' or sessionScope.currentUser.isSuperadmin}">
                <a href="${pageContext.request.contextPath}/org/${orgId}">Сотрудники</a>
            </c:if>
        </nav>
    </div>
</c:if>
