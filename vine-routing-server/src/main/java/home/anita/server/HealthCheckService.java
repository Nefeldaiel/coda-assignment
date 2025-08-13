package home.anita.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import home.anita.RoutingConfig;
import home.anita.RoutingConfig.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static home.anita.server.ServerHealth.Status.HEALTHY;
import static home.anita.server.ServerHealth.Status.UNHEALTHY;

/**
 * Service responsible for monitoring the health status of server nodes.
 */
@Service
public class HealthCheckService {
    
    private static final Logger logger = LoggerFactory.getLogger(HealthCheckService.class);
    private static final String HEALTH_ENDPOINT = "/health";
    private static final String EXPECTED_STATUS = "UP";
    
    private final RoutingConfig routingConfig;
    private final HealthCheckConfig healthCheckConfig;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    
    // Thread-safe map to store server health status
    private final Map<String, ServerHealth> serverHealthMap = new ConcurrentHashMap<>();
    
    public HealthCheckService(RoutingConfig routingConfig, HealthCheckConfig healthCheckConfig) {
        this.routingConfig = routingConfig;
        this.healthCheckConfig = healthCheckConfig;
        this.webClient = WebClient.builder().build();
        this.objectMapper = new ObjectMapper();
        
        logger.info("HealthCheckService initialized with config: {}", healthCheckConfig);
    }
    
    /**
     * Performs health check on application startup.
     * This method is automatically called when the Spring application is ready.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void performStartupHealthCheck() {
        logger.info("Starting health check for all configured servers...");
        
        Set<ServerConfig> servers = routingConfig.getServers();
        if (servers == null || servers.isEmpty()) {
            logger.warn("No servers configured for health checking");
            return;
        }
        
        // Initialize and check all servers
        for (ServerConfig server : servers) {
            String serverUrl = server.getUrl();
            ServerHealth serverHealth = new ServerHealth(serverUrl);
            serverHealthMap.put(serverUrl, serverHealth);
            
            checkServerHealth(serverHealth);
        }
        
        // Log summary of health check results
        logHealthCheckSummary();
    }
    
    /**
     * Checks the health of a single server by calling its /health endpoint.
     * 
     * @param serverHealth The server health object to update
     */
    public void checkServerHealth(ServerHealth serverHealth) {
        String serverUrl = serverHealth.getUrl();
        String healthUrl = serverUrl + HEALTH_ENDPOINT;
        
        try {
            logger.debug("Checking health for server: {}", healthUrl);
            
            ResponseEntity<String> response = webClient
                    .get()
                    .uri(healthUrl)
                    .retrieve()
                    .toEntity(String.class)
                    .block();
            
            if (response != null && response.getStatusCode() == HttpStatus.OK) {
                String responseBody = response.getBody();
                if (isValidHealthResponse(responseBody)) {
                    serverHealth.setStatus(HEALTHY);
                    logger.info("Server {} is HEALTHY", serverUrl);
                } else {
                    serverHealth.setStatus(UNHEALTHY);
                    serverHealth.setErrorMessage("Invalid health response: " + responseBody);
                    logger.warn("Server {} is UNHEALTHY - Invalid response: {}", serverUrl, responseBody);
                }
            } else {
                serverHealth.setStatus(UNHEALTHY);
                serverHealth.setErrorMessage("HTTP " + (response != null ? response.getStatusCode() : "null response"));
                logger.warn("Server {} is UNHEALTHY - HTTP {}", serverUrl, (response != null ? response.getStatusCode() : "null response"));
            }
            
        } catch (WebClientResponseException e) {
            serverHealth.setStatus(UNHEALTHY);
            serverHealth.setErrorMessage("HTTP " + e.getStatusCode() + ": " + e.getResponseBodyAsString());
            logger.error("Server {} is UNHEALTHY - HTTP {}: {}", serverUrl, e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            serverHealth.setStatus(UNHEALTHY);
            serverHealth.setErrorMessage(e.getMessage());
            logger.error("Server {} is UNHEALTHY - Error: {}", serverUrl, e.getMessage());
        }
    }
    
    /**
     * Validates that the health response contains the expected status.
     * Expected format: {"status": "UP"}
     * 
     * @param responseBody The response body from the health endpoint
     * @return true if the response indicates the server is healthy
     */
    private boolean isValidHealthResponse(String responseBody) {
        if (responseBody == null || responseBody.trim().isEmpty()) {
            return false;
        }
        
        try {
            JsonNode jsonNode = objectMapper.readTree(responseBody);
            if (jsonNode.has("status")) {
                String status = jsonNode.get("status").asText();
                return EXPECTED_STATUS.equals(status);
            }
        } catch (Exception e) {
            logger.debug("Failed to parse health response as JSON: {}", responseBody);
        }
        
        return false;
    }
    
    /**
     * Gets the health status of a specific server.
     * 
     * @param serverUrl The URL of the server
     * @return ServerHealth object or null if server not found
     */
    public ServerHealth getServerHealth(String serverUrl) {
        return serverHealthMap.get(serverUrl);
    }
    
    /**
     * Gets all server health statuses.
     * 
     * @return Map of server URL to ServerHealth
     */
    public Map<String, ServerHealth> getAllServerHealth() {
        return new ConcurrentHashMap<>(serverHealthMap);
    }
    
    /**
     * Gets all healthy servers.
     * 
     * @return Set of healthy server URLs
     */
    public Set<String> getHealthyServers() {
        return serverHealthMap.entrySet().stream()
            .filter(entry -> entry.getValue().isHealthy())
            .map(Map.Entry::getKey)
            .collect(java.util.stream.Collectors.toSet());
    }
    
    /**
     * Gets all unhealthy servers.
     * 
     * @return Set of unhealthy server URLs
     */
    public Set<String> getUnhealthyServers() {
        return serverHealthMap.entrySet().stream()
            .filter(entry -> entry.getValue().isUnhealthy())
            .map(Map.Entry::getKey)
            .collect(java.util.stream.Collectors.toSet());
    }
    
    /**
     * Logs a summary of the health check results.
     */
    private void logHealthCheckSummary() {
        int totalServers = serverHealthMap.size();
        int healthyCount = getHealthyServers().size();
        int unhealthyCount = getUnhealthyServers().size();
        
        logger.info("Health check completed - Total: {}, Healthy: {}, Unhealthy: {}", 
            totalServers, healthyCount, unhealthyCount);
        
        // Log details for each server
        serverHealthMap.forEach((url, health) -> {
            if (health.isHealthy()) {
                logger.info("✓ {} - HEALTHY", url);
            } else {
                logger.warn("✗ {} - UNHEALTHY ({})", url, health.getErrorMessage());
            }
        });
    }
    
    /**
     * Manually triggers a health check for all servers.
     * This can be used for scheduled health checks or on-demand checks.
     */
    public void performHealthCheck() {
        logger.info("Performing manual health check for all servers...");
        serverHealthMap.values().forEach(this::checkServerHealth);
        logHealthCheckSummary();
    }
    
    /**
     * Scheduled health check that runs at configurable intervals.
     * Uses the interval defined in health-check.interval property.
     * Only runs if there are servers configured for health checking.
     */
    @Scheduled(fixedDelayString = "#{@healthCheckConfig.interval}")
    public void scheduledHealthCheck() {
        if (serverHealthMap.isEmpty()) {
            logger.debug("No servers configured for scheduled health check");
            return;
        }
        
        logger.debug("Running scheduled health check for {} servers", serverHealthMap.size());
        
        // Perform health check on all configured servers
        serverHealthMap.values().forEach(this::checkServerHealth);
        
        // Log summary with current status
        int healthyCount = getHealthyServers().size();
        int unhealthyCount = getUnhealthyServers().size();
        int totalCount = serverHealthMap.size();
        
        if (unhealthyCount > 0) {
            logger.warn("Scheduled health check completed - Total: {}, Healthy: {}, Unhealthy: {}", 
                totalCount, healthyCount, unhealthyCount);
            
            // Log unhealthy servers for immediate attention
            getUnhealthyServers().forEach(url -> {
                ServerHealth health = serverHealthMap.get(url);
                logger.warn("✗ {} - UNHEALTHY ({})", url, health.getErrorMessage());
            });
        } else {
            logger.info("Scheduled health check completed - All {} servers are HEALTHY", totalCount);
        }
    }
}