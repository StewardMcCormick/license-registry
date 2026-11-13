package web;

import config.ServiceRegistry;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.CurrentUser;
import util.SessionUtil;

import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class BaseServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(BaseServlet.class.getName());

    protected ServiceRegistry services() {
        return (ServiceRegistry) getServletContext().getAttribute(ServiceRegistry.CONTEXT_ATTRIBUTE);
    }

    protected CurrentUser currentUser(HttpServletRequest request) {
        return SessionUtil.getCurrentUser(request);
    }

    protected boolean belongsToCurrentOrg(CurrentUser user, UUID orgId) {
        return user.isSuperadmin() || orgId.equals(user.getOrgId());
    }

    protected void render(HttpServletRequest request, HttpServletResponse response, String viewPath)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/" + viewPath).forward(request, response);
    }

    protected void redirect(HttpServletRequest request, HttpServletResponse response, String path)
            throws IOException {
        response.sendRedirect(request.getContextPath() + path);
    }

    protected void renderError(HttpServletRequest request, HttpServletResponse response, int status, String message)
            throws ServletException, IOException {
        LOGGER.log(Level.WARNING, "Ошибка " + status + " на " + request.getMethod() + " "
                + request.getRequestURI() + ": " + message);
        response.setStatus(status);
        request.setAttribute("status", status);
        request.setAttribute("message", message);
        render(request, response, "error.jsp");
    }
}
