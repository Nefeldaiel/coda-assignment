package home.anita.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Model class that manages the collection of server health statuses.
 * Encapsulates server health storage and provides operations for 
 * querying and manipulating server health data.
 */
@Component
@Slf4j
public class ServerHealthMap {

    private final Map<String, ServerHealth> serverMap = new ConcurrentHashMap<>();

    /**
     * Adds or updates a server health entry in the health map.
     * 
     * @param url The server URL
     * @param health The ServerHealth object to track
     */
    public void addServer(String url, ServerHealth health) {
        serverMap.put(url, health);
        log.debug("Added server to health map: {}", url);
    }

    /**
     * Gets the health status of a specific server.
     * 
     * @param serverUrl The URL of the server
     * @return ServerHealth object or null if server not found
     */
    public ServerHealth getServerHealth(String serverUrl) {
        return serverMap.get(serverUrl);
    }

    /**
     * Gets all server health statuses.
     * 
     * @return Map of server URL to ServerHealth (defensive copy)
     */
    public Map<String, ServerHealth> getAllServerHealth() {
        return new ConcurrentHashMap<>(serverMap);
    }

    /**
     * Gets all healthy servers.
     * 
     * @return Set of healthy server URLs
     */
    public Set<String> getHealthyServers() {
        return serverMap.entrySet().stream()
                .filter(entry -> entry.getValue().isHealthy())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    /**
     * Gets all unhealthy servers.
     * 
     * @return Set of unhealthy server URLs
     */
    public Set<String> getUnhealthyServers() {
        return serverMap.entrySet().stream()
                .filter(entry -> entry.getValue().isUnhealthy())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    /**
     * Gets the total count of servers being tracked.
     * 
     * @return Total number of servers
     */
    public int getTotalServerCount() {
        return serverMap.size();
    }

    /**
     * Gets the count of healthy servers.
     * 
     * @return Number of healthy servers
     */
    public int getHealthyServerCount() {
        return (int) serverMap.values().stream()
                .filter(ServerHealth::isHealthy)
                .count();
    }

    /**
     * Gets the count of unhealthy servers.
     * 
     * @return Number of unhealthy servers
     */
    public int getUnhealthyServerCount() {
        return (int) serverMap.values().stream()
                .filter(ServerHealth::isUnhealthy)
                .count();
    }

    /**
     * Checks if the server map is empty.
     * 
     * @return true if no servers are being tracked
     */
    public boolean isEmpty() {
        return serverMap.isEmpty();
    }

    /**
     * Logs a summary of the health check results.
     */
    public void logHealthCheckSummary() {
        var totalCount = getTotalServerCount();
        var healthyCount = getHealthyServerCount();
        var unhealthyCount = getUnhealthyServerCount();

        log.info("Health check completed - Total: {}, Healthy: {}, Unhealthy: {}",
                totalCount, healthyCount, unhealthyCount);

        serverMap.forEach((url, health) -> {
            if (health.isHealthy()) {
                log.info("✓ {} - HEALTHY", url);
            } else {
                log.warn("✗ {} - UNHEALTHY ({})", url, health.getErrorMessage());
            }
        });
    }

    /**
     * Executes the given action for each server health entry.
     * 
     * @param action The action to execute for each ServerHealth
     */
    public void forEachServer(java.util.function.Consumer<ServerHealth> action) {
        serverMap.values().forEach(action);
    }

    /**
     * Removes all servers from the health map.
     * Useful for testing or resetting state.
     */
    public void clear() {
        serverMap.clear();
        log.debug("Cleared all servers from health map");
    }
}