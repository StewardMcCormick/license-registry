package filter.common;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import util.auth.CsrfTokenUtil;

import java.io.IOException;

public class CsrfFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String path = request.getRequestURI().substring(request.getContextPath().length());
        if (path.startsWith("/css/") || path.startsWith("/js/")) {
            chain.doFilter(request, response);
            return;
        }

        if ("POST".equalsIgnoreCase(request.getMethod())) {
            HttpSession session = request.getSession(false);
            String submittedToken = request.getParameter("csrfToken");
            if (session == null || !CsrfTokenUtil.isValid(session, submittedToken)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Невалидный csrf-токен, обновите страницу");
                return;
            }
        } else {
            CsrfTokenUtil.getOrCreateToken(request.getSession(true));
        }

        chain.doFilter(request, response);
    }
}
