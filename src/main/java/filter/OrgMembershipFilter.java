package filter;

import config.ServiceRegistry;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import service.OrganizationService;
import util.CurrentUser;
import util.PathUtil;
import util.SessionUtil;

import java.io.IOException;
import java.util.UUID;

public class OrgMembershipFilter implements Filter {

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

        UUID orgId = PathUtil.segmentAsUuid(PathUtil.segments(request.getPathInfo()), 0);
        if (!isOrgMember(request, currentUser.getUserId(), orgId)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Доступно только участнику организации");
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean isOrgMember(HttpServletRequest request, UUID userId, UUID orgId) {
        if (orgId == null) {
            return true;
        }

        ServiceRegistry serviceRegistry = (ServiceRegistry) request.getServletContext()
                .getAttribute(ServiceRegistry.CONTEXT_ATTRIBUTE);
        OrganizationService organizationService = serviceRegistry.getOrganizationService();

        return organizationService.findMemberRole(orgId, userId).isPresent();
    }
}
