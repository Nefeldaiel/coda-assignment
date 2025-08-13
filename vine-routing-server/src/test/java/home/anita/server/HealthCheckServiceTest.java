package home.anita.server;

import home.anita.RoutingConfig;
import home.anita.RoutingConfig.ServerConfig;
import home.anita.http.RequestHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HealthCheckServiceTest {

    @Mock
    private RoutingConfig routingConfig;

    @Mock
    private HealthCheckConfig healthCheckConfig;

    @Mock
    private RequestHandler requestHandler;

    @Mock
    private ApplicationReadyEvent applicationReadyEvent;

    private HealthCheckService healthCheckService;
    private Set<ServerConfig> mockServers;

    @BeforeEach
    void setUp() {
        healthCheckService = new HealthCheckService(routingConfig, requestHandler);

        // Set up mock servers
        ServerConfig server1 = new ServerConfig();
        server1.setUrl("http://localhost:9001");

        ServerConfig server2 = new ServerConfig();
        server2.setUrl("http://localhost:9002");

        mockServers = new HashSet<>();
        mockServers.add(server1);
        mockServers.add(server2);
    }

    @Test
    void testPerformStartupHealthCheckWithHealthyServers() {
        when(routingConfig.getServers()).thenReturn(mockServers);

        // Mock RequestHandler for successful health check
        when(requestHandler.sendRequest(any())).thenReturn(
                new ResponseEntity<>("{\"status\": \"UP\"}", HttpStatus.OK)
        );

        healthCheckService.performStartupHealthCheck();

        Map<String, ServerHealth> allHealth = healthCheckService.getAllServerHealth();
        assertEquals(2, allHealth.size());

        ServerHealth server1Health = healthCheckService.getServerHealth("http://localhost:9001");
        ServerHealth server2Health = healthCheckService.getServerHealth("http://localhost:9002");

        assertNotNull(server1Health);
        assertNotNull(server2Health);
        assertTrue(server1Health.isHealthy());
        assertTrue(server2Health.isHealthy());

        Set<String> healthyServers = healthCheckService.getHealthyServers();
        assertEquals(2, healthyServers.size());
        assertTrue(healthyServers.contains("http://localhost:9001"));
        assertTrue(healthyServers.contains("http://localhost:9002"));
    }

    @Test
    void testPerformStartupHealthCheckWithUnhealthyServers() {
        when(routingConfig.getServers()).thenReturn(mockServers);

        // Mock RequestHandler for unhealthy response
        when(requestHandler.sendRequest(any())).thenReturn(
                new ResponseEntity<>("{\"status\": \"DOWN\"}", HttpStatus.OK)
        );

        healthCheckService.performStartupHealthCheck();

        Map<String, ServerHealth> allHealth = healthCheckService.getAllServerHealth();
        assertEquals(2, allHealth.size());

        Set<String> unhealthyServers = healthCheckService.getUnhealthyServers();
        assertEquals(2, unhealthyServers.size());
        assertTrue(unhealthyServers.contains("http://localhost:9001"));
        assertTrue(unhealthyServers.contains("http://localhost:9002"));
    }

    @Test
    void testPerformStartupHealthCheckWithNoServers() {
        when(routingConfig.getServers()).thenReturn(new HashSet<>());

        healthCheckService.performStartupHealthCheck();

        Map<String, ServerHealth> allHealth = healthCheckService.getAllServerHealth();
        assertEquals(0, allHealth.size());
    }

    @Test
    void testPerformStartupHealthCheckWithNullServers() {
        when(routingConfig.getServers()).thenReturn(null);

        healthCheckService.performStartupHealthCheck();

        Map<String, ServerHealth> allHealth = healthCheckService.getAllServerHealth();
        assertEquals(0, allHealth.size());
    }

    @Test
    void testGetServerHealthForNonExistentServer() {
        ServerHealth health = healthCheckService.getServerHealth("http://nonexistent:8080");
        assertNull(health);
    }

    @Test
    void testCheckServerHealthWithValidResponse() {
        // Mock RequestHandler for valid response
        when(requestHandler.sendRequest(any())).thenReturn(
                new ResponseEntity<>("{\"status\": \"UP\"}", HttpStatus.OK)
        );

        ServerHealth serverHealth = new ServerHealth("http://localhost:9001");
        healthCheckService.checkServerHealth(serverHealth);

        assertTrue(serverHealth.isHealthy());
        assertNull(serverHealth.getErrorMessage());
    }

    @Test
    void testCheckServerHealthWithInvalidJsonResponse() {
        // Mock RequestHandler for invalid JSON
        when(requestHandler.sendRequest(any())).thenReturn(
                new ResponseEntity<>("invalid json", HttpStatus.OK)
        );

        ServerHealth serverHealth = new ServerHealth("http://localhost:9001");
        healthCheckService.checkServerHealth(serverHealth);

        assertTrue(serverHealth.isUnhealthy());
        assertNotNull(serverHealth.getErrorMessage());
        assertTrue(serverHealth.getErrorMessage().contains("Invalid health response"));
    }

    @Test
    void testCheckServerHealthWithEmptyResponse() {
        // Mock RequestHandler for empty response
        when(requestHandler.sendRequest(any())).thenReturn(
                new ResponseEntity<>("", HttpStatus.OK)
        );

        ServerHealth serverHealth = new ServerHealth("http://localhost:9001");
        healthCheckService.checkServerHealth(serverHealth);

        assertTrue(serverHealth.isUnhealthy());
        assertNotNull(serverHealth.getErrorMessage());
        assertTrue(serverHealth.getErrorMessage().contains("Invalid health response"));
    }

    @Test
    void testCheckServerHealthWithMalformedJson() {
        // Mock RequestHandler for malformed JSON
        when(requestHandler.sendRequest(any())).thenReturn(
                new ResponseEntity<>("{\"invalid\": malformed", HttpStatus.OK)
        );

        ServerHealth serverHealth = new ServerHealth("http://localhost:9001");
        healthCheckService.checkServerHealth(serverHealth);

        assertTrue(serverHealth.isUnhealthy());
        assertNotNull(serverHealth.getErrorMessage());
        assertTrue(serverHealth.getErrorMessage().contains("Invalid health response"));
    }

    @Test
    void testPerformManualHealthCheck() {
        when(routingConfig.getServers()).thenReturn(mockServers);

        // Mock RequestHandler for successful health check
        when(requestHandler.sendRequest(any())).thenReturn(
                new ResponseEntity<>("{\"status\": \"UP\"}", HttpStatus.OK)
        );

        // Initialize servers first
        healthCheckService.performStartupHealthCheck();

        // Perform manual check
        healthCheckService.performHealthCheck();

        Map<String, ServerHealth> allHealth = healthCheckService.getAllServerHealth();
        assertEquals(2, allHealth.size());

        Set<String> healthyServers = healthCheckService.getHealthyServers();
        assertEquals(2, healthyServers.size());
    }

    @Test
    void testScheduledHealthCheckWithHealthyServers() {
        when(routingConfig.getServers()).thenReturn(mockServers);

        // Mock RequestHandler for successful health check
        when(requestHandler.sendRequest(any())).thenReturn(
                new ResponseEntity<>("{\"status\": \"UP\"}", HttpStatus.OK)
        );

        // Initialize servers first
        healthCheckService.performStartupHealthCheck();

        // Run scheduled health check
        healthCheckService.scheduledHealthCheck();

        Map<String, ServerHealth> allHealth = healthCheckService.getAllServerHealth();
        assertEquals(2, allHealth.size());

        Set<String> healthyServers = healthCheckService.getHealthyServers();
        assertEquals(2, healthyServers.size());

        Set<String> unhealthyServers = healthCheckService.getUnhealthyServers();
        assertEquals(0, unhealthyServers.size());
    }

    @Test
    void testScheduledHealthCheckWithUnhealthyServers() {
        when(routingConfig.getServers()).thenReturn(mockServers);

        // Mock RequestHandler for unhealthy response
        when(requestHandler.sendRequest(any())).thenReturn(
                new ResponseEntity<>("{\"status\": \"DOWN\"}", HttpStatus.OK)
        );

        // Initialize servers first
        healthCheckService.performStartupHealthCheck();

        // Run scheduled health check
        healthCheckService.scheduledHealthCheck();

        Set<String> unhealthyServers = healthCheckService.getUnhealthyServers();
        assertEquals(2, unhealthyServers.size());

        Set<String> healthyServers = healthCheckService.getHealthyServers();
        assertEquals(0, healthyServers.size());
    }

    @Test
    void testScheduledHealthCheckWithNoServers() {
        // Test that scheduled health check handles empty server map gracefully
        healthCheckService.scheduledHealthCheck();

        Map<String, ServerHealth> allHealth = healthCheckService.getAllServerHealth();
        assertEquals(0, allHealth.size());
    }
}