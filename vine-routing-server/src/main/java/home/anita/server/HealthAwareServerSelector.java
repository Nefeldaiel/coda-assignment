package home.anita.server;

import home.anita.RoutingConfig.ServerConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Server selector that only selects from healthy servers.
 * Falls back to all servers if no healthy servers are available.
 */
@Component
@Slf4j
public class HealthAwareServerSelector implements ServerSelector {

    private final RandomServerSelector randomServerSelector;
    private final HealthCheckService healthCheckService;

    public HealthAwareServerSelector(RandomServerSelector randomServerSelector,
                                     HealthCheckService healthCheckService) {
        this.randomServerSelector = randomServerSelector;
        this.healthCheckService = healthCheckService;
    }

    @Override
    public ServerConfig select(Set<ServerConfig> servers) {
        var healthyServers = getHealthyServers(servers);

        if (!healthyServers.isEmpty()) {
            // Select from healthy servers
            log.debug("Selecting from {} healthy servers out of {} total servers",
                    healthyServers.size(), servers.size());
            return randomServerSelector.select(healthyServers);
        } else {
            // No healthy servers available, fall back to all servers
            log.warn("No healthy servers available, falling back to all servers");
            return randomServerSelector.select(servers);
        }
    }

    Set<ServerConfig> getHealthyServers(Set<ServerConfig> servers) {
        if (servers == null || servers.isEmpty()) {
            throw new IllegalArgumentException("Server set cannot be null or empty");
        }

        Set<String> healthyServerUrls = healthCheckService.getHealthyServers();
        return servers.stream()
                .filter(server -> healthyServerUrls.contains(server.getUrl()))
                .collect(Collectors.toSet());
    }
}