package filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import util.CsrfTokenUtil;

import java.io.IOException;

public class CsrfFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpSession session = request.getSession(true);

        CsrfTokenUtil.getOrCreateToken(session);

        if ("POST".equalsIgnoreCase(request.getMethod())) {
            String submittedToken = request.getParameter("csrfToken");
            if (!CsrfTokenUtil.isValid(session, submittedToken)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Невалидный csrf-токен, обновите страницу");
                return;
            }
        }

        chain.doFilter(request, response);
    }
}
