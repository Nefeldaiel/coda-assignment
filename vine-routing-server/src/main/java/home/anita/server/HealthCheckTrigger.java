package home.anita.server;

import home.anita.RoutingConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Component responsible for triggering health checks at application startup
 * and on scheduled intervals. Separated from HealthCheckService to isolate
 * the triggering logic from the core health checking business logic.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class HealthCheckTrigger {

    private final HealthCheckService healthCheckService;
    private final RoutingConfig routingConfig;

    /**
     * Performs health check on application startup.
     * This method is automatically called when the Spring application is ready.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void performStartupHealthCheck() {
        log.info("Starting health check for all configured servers...");

        var servers = routingConfig.getServers();
        if (servers == null || servers.isEmpty()) {
            log.warn("No servers configured for health checking");
            return;
        }

        for (var server : servers) {
            var url = server.getUrl();
            var serverHealth = new ServerHealth(url);
            healthCheckService.addServer(url, serverHealth);
            healthCheckService.checkHealth(serverHealth);
        }

        healthCheckService.logHealthCheckSummary();
    }

    /**
     * Scheduled health check that runs at configurable intervals.
     * Uses the interval defined in health-check.interval property.
     * Only runs if there are servers configured for health checking.
     */
    @Scheduled(fixedDelayString = "#{@healthCheckConfig.interval}")
    public void scheduledHealthCheck() {
        var allServer = healthCheckService.getAllServerHealth();

        if (allServer.isEmpty()) {
            log.debug("No servers configured for scheduled health check");
            return;
        }

        log.debug("Running scheduled health check for {} servers", allServer.size());

        allServer.values().forEach(healthCheckService::checkHealth);

        // Log summary with current status
        var healthyCount = healthCheckService.getHealthyServers().size();
        var unhealthyCount = healthCheckService.getUnhealthyServers().size();
        var totalCount = allServer.size();

        if (unhealthyCount > 0) {
            log.warn("Scheduled health check completed - Total: {}, Healthy: {}, Unhealthy: {}",
                    totalCount, healthyCount, unhealthyCount);

            // Log unhealthy servers for immediate attention
            healthCheckService.getUnhealthyServers().forEach(url -> {
                ServerHealth health = healthCheckService.getServerHealth(url);
                if (health != null) {
                    log.warn("✗ {} - UNHEALTHY ({})", url, health.getErrorMessage());
                }
            });
        } else {
            log.info("Scheduled health check completed - All {} servers are HEALTHY", totalCount);
        }
    }
}