package filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.CurrentUser;
import util.SessionUtil;

import java.io.IOException;

public class OrgMembershipFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        CurrentUser currentUser = SessionUtil.getCurrentUser(request);
        if (currentUser == null || currentUser.getOrgId() == null) {
            response.sendRedirect(request.getContextPath() + "/onboarding");
            return;
        }

        chain.doFilter(request, response);
    }
}
