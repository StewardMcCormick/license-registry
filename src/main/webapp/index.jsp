<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="util.CurrentUser" %>
<%@ page import="util.SessionUtil" %>
<%
    CurrentUser currentUser = SessionUtil.getCurrentUser(request);
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/login");
    } else if (currentUser.getOrgId() == null) {
        response.sendRedirect(request.getContextPath() + "/onboarding");
    } else {
        response.sendRedirect(request.getContextPath() + "/licenses");
    }
%>
