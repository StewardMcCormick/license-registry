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
import java.util.Set;
import java.util.UUID;

public class OrgRoleFilter implements Filter {

    private static final Set<String> ADMIN_ONLY_SECTIONS = Set.of("reissue-code", "promote", "lower");

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

        if (currentUser.isSuperadmin()) {
            chain.doFilter(request, response);
            return;
        }

        String[] segments = PathUtil.segments(request.getPathInfo());
        UUID orgId = PathUtil.segmentAsUuid(segments, 0);
        if (orgId == null) {
            chain.doFilter(request, response);
            return;
        }

        String section = PathUtil.segment(segments, 1);
        if (isAdminOnlySection(section) && !isAdminOf(request, currentUser.getUserId(), orgId)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Доступно только администратору организации");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isAdminOnlySection(String section) {
        return section == null || section.isBlank() || ADMIN_ONLY_SECTIONS.contains(section);
    }

    private boolean isAdminOf(HttpServletRequest request, UUID userId, UUID orgId) {
        ServiceRegistry serviceRegistry = (ServiceRegistry) request.getServletContext()
                .getAttribute(ServiceRegistry.CONTEXT_ATTRIBUTE);
        OrganizationService organizationService = serviceRegistry.getOrganizationService();
        return organizationService.findMemberRole(orgId, userId)
                .orElse(null) == MemberRole.ADMIN;
    }
}
