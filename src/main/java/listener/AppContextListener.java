package listener;

import config.ServiceRegistry;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServiceRegistry services = new ServiceRegistry();
        event.getServletContext().setAttribute(ServiceRegistry.CONTEXT_ATTRIBUTE, services);

        services.getLicenseStatusUpdateWorker().start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        ServiceRegistry services = (ServiceRegistry) event.getServletContext()
                .getAttribute(ServiceRegistry.CONTEXT_ATTRIBUTE);
        if (services != null) {
            services.getLicenseStatusUpdateWorker().shutdown();
        }
    }
}