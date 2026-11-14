package listener;

import config.ServiceRegistry;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebListener
public class AppContextListener implements ServletContextListener {

    private static final Logger LOGGER = Logger.getLogger(AppContextListener.class.getName());

    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServiceRegistry services = new ServiceRegistry();
        event.getServletContext().setAttribute(ServiceRegistry.CONTEXT_ATTRIBUTE, services);

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                services.getLicenseService().recalculateStatuses();
            } catch (RuntimeException e) {
                LOGGER.log(Level.SEVERE, "Не удалось пересчитать статусы лицензий", e);
            }
        }, 0, 1, TimeUnit.DAYS);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }
}
