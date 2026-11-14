package web.common;

import config.ServiceRegistry;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import util.auth.CurrentUser;
import util.auth.SessionUtil;

public abstract class BaseServlet extends HttpServlet {

    private ServiceRegistry services;

    @Override
    public void init() throws ServletException {
        super.init();

        services = (ServiceRegistry) getServletContext().getAttribute(ServiceRegistry.CONTEXT_ATTRIBUTE);
    }

    public ServiceRegistry services() {
        return services;
    }

    protected CurrentUser currentUser(HttpServletRequest request) {
        return SessionUtil.getCurrentUser(request);
    }
}
