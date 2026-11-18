package util.worker;

import service.license.LicenseService;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LicenseStatusUpdateWorker {

    private static final Logger LOGGER = Logger.getLogger(LicenseStatusUpdateWorker.class.getName());

    private final LicenseService licenseService;
    private final ScheduledExecutorService scheduler;

    public LicenseStatusUpdateWorker(LicenseService licenseService) {
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.licenseService = licenseService;
    }

    public void start() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                licenseService.recalculateStatuses();
            } catch (RuntimeException e) {
                LOGGER.log(Level.SEVERE, "Не удалось пересчитать статусы лицензий", e);
            }
        }, 0, 1, TimeUnit.DAYS);
    }

    public void shutdown() {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }
}