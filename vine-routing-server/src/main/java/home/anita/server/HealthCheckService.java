package home.anita.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import home.anita.http.HealthCheckRequest;
import home.anita.http.RequestHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Map;
import java.util.Set;

import static home.anita.server.ServerHealth.Status.HEALTHY;
import static home.anita.server.ServerHealth.Status.UNHEALTHY;

/**
 * Service responsible for monitoring the health status of server nodes.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class HealthCheckService {

    private static final String EXPECTED_STATUS_AS_HEALTHY = "UP";

    private final RequestHandler requestHandler;
    private final ObjectMapper objectMapper;
    private final ServerHealthMap serverHealthMap;


    /**
     * Checks the health of a single server by calling its /health endpoint.
     *
     * @param health The server health object to update
     */
    public void checkHealth(ServerHealth health) {
        var url = health.getUrl();

        try {
            log.debug("Checking health for server: {}", url);

            var request = HealthCheckRequest.create(url);
            var response = requestHandler.sendRequest(request);

            if (response != null && response.getStatusCode() == HttpStatus.OK) {
                var body = response.getBody();
                if (isValidHealthResponse(body)) {
                    health.setStatus(HEALTHY);
                    log.info("Server {} is HEALTHY", url);
                } else {
                    health.setStatus(UNHEALTHY);
                    health.setErrorMessage("Invalid health response: " + body);
                    log.warn("Server {} is UNHEALTHY - Invalid response: {}", url, body);
                }
            } else {
                health.setStatus(UNHEALTHY);
                health.setErrorMessage("HTTP " + (response != null ? response.getStatusCode() : "null response"));
                log.warn("Server {} is UNHEALTHY - HTTP {}", url, (response != null ? response.getStatusCode() : "null response"));
            }

        } catch (WebClientResponseException e) {
            health.setStatus(UNHEALTHY);
            health.setErrorMessage("HTTP " + e.getStatusCode() + ": " + e.getResponseBodyAsString());
            log.error("Server {} is UNHEALTHY - HTTP {}: {}", url, e.getStatusCode(), e.getResponseBodyAsString());
        } catch (Exception e) {
            health.setStatus(UNHEALTHY);
            health.setErrorMessage(e.getMessage());
            log.error("Server {} is UNHEALTHY - Error: {}", url, e.getMessage());
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
            var jsonNode = objectMapper.readTree(responseBody);
            if (jsonNode.has("status")) {
                var status = jsonNode.get("status").asText();
                return EXPECTED_STATUS_AS_HEALTHY.equals(status);
            }
        } catch (Exception e) {
            log.debug("Failed to parse health response as JSON: {}", responseBody);
        }

        return false;
    }


    public ServerHealth getServerHealth(String serverUrl) {
        return serverHealthMap.getServerHealth(serverUrl);
    }

    public Map<String, ServerHealth> getAllServerHealth() {
        return serverHealthMap.getAllServerHealth();
    }

    public Set<String> getHealthyServers() {
        return serverHealthMap.getHealthyServers();
    }

    public Set<String> getUnhealthyServers() {
        return serverHealthMap.getUnhealthyServers();
    }

    public void logHealthCheckSummary() {
        serverHealthMap.logHealthCheckSummary();
    }

    public void addServer(String url, ServerHealth health) {
        serverHealthMap.addServer(url, health);
    }
}