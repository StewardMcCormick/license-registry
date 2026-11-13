package filter;

import config.ServiceRegistry;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.MemberRole;
import service.OrganizationService;
import util.CurrentUser;
import util.PathUtil;
import util.SessionUtil;

import java.io.IOException;
import java.util.UUID;

public class OrgRoleFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        CurrentUser currentUser = SessionUtil.getCurrentUser(request);
        if (currentUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String[] segments = PathUtil.segments(request.getPathInfo());
        UUID orgId = PathUtil.segmentAsUuid(segments, 0);
        if (orgId == null) {
            chain.doFilter(request, response);
            return;
        }

        String section = PathUtil.segment(segments, 1);
        boolean adminOnly = section == null || section.isBlank()
                || "reissue-code".equals(section)
                || "promote".equals(section)
                || "lower".equals(section);

        if (adminOnly && !isAdminOf(request, currentUser.getUserId(), orgId)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Доступно только администратору организации");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isAdminOf(HttpServletRequest request, UUID userId, UUID orgId) {
        ServiceRegistry serviceRegistry = (ServiceRegistry) request.getServletContext()
                .getAttribute(ServiceRegistry.CONTEXT_ATTRIBUTE);
        OrganizationService organizationService = serviceRegistry.getOrganizationService();
        return organizationService.findMemberRole(orgId, userId)
                .orElse(null) == MemberRole.ADMIN;
    }
}
