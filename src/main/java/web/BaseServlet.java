package web;

import config.ServiceRegistry;
import jakarta.servlet.ServletContext;
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

    protected boolean belongsToCurrentOrg(CurrentUser user, UUID orgId) {
        for (UUID id : user.getMemberRoleMap().keySet()) {
            if (id.equals(orgId)) {
                return true;
            }
        }

        return false;
    }
}
