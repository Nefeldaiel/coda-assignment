package home.anita.server;

import home.anita.RoutingConfig.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Server selector that only selects from healthy servers.
 * Falls back to all servers if no healthy servers are available.
 */
@Component
public class HealthAwareServerSelector implements ServerSelector {

    private static final Logger logger = LoggerFactory.getLogger(HealthAwareServerSelector.class);
    
    private final RandomServerSelector randomServerSelector;
    private final HealthCheckService healthCheckService;
    
    public HealthAwareServerSelector(RandomServerSelector randomServerSelector, 
                                   HealthCheckService healthCheckService) {
        this.randomServerSelector = randomServerSelector;
        this.healthCheckService = healthCheckService;
    }

    @Override
    public ServerConfig select(Set<ServerConfig> servers) {
        if (servers == null || servers.isEmpty()) {
            throw new IllegalArgumentException("Server set cannot be null or empty");
        }

        // Get healthy servers
        Set<String> healthyServerUrls = healthCheckService.getHealthyServers();
        
        // Filter servers to only include healthy ones
        Set<ServerConfig> healthyServers = servers.stream()
            .filter(server -> healthyServerUrls.contains(server.getUrl()))
            .collect(Collectors.toSet());

        if (!healthyServers.isEmpty()) {
            // Select from healthy servers
            logger.debug("Selecting from {} healthy servers out of {} total servers", 
                healthyServers.size(), servers.size());
            return randomServerSelector.select(healthyServers);
        } else {
            // No healthy servers available, fall back to all servers
            logger.warn("No healthy servers available, falling back to all servers");
            return randomServerSelector.select(servers);
        }
    }
}