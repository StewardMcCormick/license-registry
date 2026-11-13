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
        UUID orgId = PathUtil.segmentAsUuid(PathUtil.segments(request.getPathInfo()), 0);

        if (currentUser == null || orgId == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
// TODO: нужно ли
//        if (currentUser.isSuperadmin()) {
//            chain.doFilter(request, response);
//            return;
//        }

        ServiceRegistry services = (ServiceRegistry) request.getServletContext()
                .getAttribute(ServiceRegistry.CONTEXT_ATTRIBUTE);
        MemberRole role = services.getOrganizationService()
                .findMemberRole(orgId, currentUser.getUserId())
                .orElse(null);

        if (role != MemberRole.ADMIN) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Доступно только администратору организации");
            return;
        }

        chain.doFilter(request, response);
    }
}
