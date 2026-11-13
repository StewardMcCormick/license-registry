package filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ErrorLoggingFilter implements Filter {

    private static final Logger LOGGER = Logger.getLogger(ErrorLoggingFilter.class.getName());

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        try {
            chain.doFilter(request, response);
        } catch (IOException | ServletException | RuntimeException e) {
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            LOGGER.log(Level.SEVERE, "Необработанная ошибка при обработке " + httpRequest.getMethod()
                    + " " + httpRequest.getRequestURI(), e);
            throw e;
        }
    }
}
