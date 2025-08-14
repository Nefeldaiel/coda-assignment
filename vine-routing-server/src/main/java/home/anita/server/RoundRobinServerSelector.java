package home.anita.server;

import home.anita.RoutingConfig.ServerConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Server selector that selects servers in a round-robin manner.
 * Distributes requests evenly across all available servers.
 * Health-aware implementation that only selects from healthy servers.
 */
@Component
@Slf4j
public class RoundRobinServerSelector extends HealthAwareServerSelector {

    private final AtomicInteger counter = new AtomicInteger(0);

    public RoundRobinServerSelector(RandomServerSelector randomServerSelector,
                                    HealthCheckService healthCheckService) {
        super(randomServerSelector, healthCheckService);
    }

    /**
     * Selects a server using round-robin algorithm from healthy servers.
     * Falls back to all servers if no healthy servers are available.
     *
     * @param servers The set of available servers to select from
     * @return The selected server configuration
     * @throws IllegalArgumentException if the server set is null or empty
     */
    @Override
    public ServerConfig select(Set<ServerConfig> servers) {
        var healthyServers = getHealthyServers(servers);

        Set<ServerConfig> serversToUse;
        if (!healthyServers.isEmpty()) {
            serversToUse = healthyServers;
            log.debug("Using {} healthy servers out of {} total servers for round-robin selection",
                    healthyServers.size(), servers.size());
        } else {
            serversToUse = servers;
            log.warn("No healthy servers available, using all {} servers for round-robin selection",
                    servers.size());
        }

        // Convert to list for indexed access (needed for round-robin)
        var serverList = new ArrayList<>(serversToUse);

        // Get next server using round-robin
        var index = Math.abs(counter.getAndIncrement()) % serverList.size();
        var selectedServer = serverList.get(index);

        log.debug("Round-robin selected server {}/{}: {}",
                index + 1, serverList.size(), selectedServer.getUrl());

        return selectedServer;
    }

    /**
     * Resets the round-robin counter.
     * Useful for testing or when server list changes significantly.
     */
    public void resetCounter() {
        counter.set(0);
        log.debug("Round-robin counter reset");
    }

    /**
     * Gets the current counter value for monitoring purposes.
     */
    public int getCurrentCounter() {
        return counter.get();
    }
}