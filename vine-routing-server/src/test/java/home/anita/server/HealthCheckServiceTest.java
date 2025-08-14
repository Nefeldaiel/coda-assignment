package home.anita.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import home.anita.RoutingConfig.ServerConfig;
import home.anita.http.RequestHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthCheckServiceTest {

    @Mock
    private RequestHandler requestHandler;

    @Mock
    private ObjectMapper objectMapper;

    private ServerHealthMap serverHealthMap;
    private HealthCheckService healthCheckService;
    private Set<ServerConfig> mockServers;

    @BeforeEach
    void setUp() {
        // Use real ServerHealthMap for integration testing
        serverHealthMap = new ServerHealthMap();
        healthCheckService = new HealthCheckService(requestHandler, objectMapper, serverHealthMap);

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
    void testHealthCheckWithHealthyServers() throws Exception {
        // Mock RequestHandler for successful health check
        when(requestHandler.sendRequest(any())).thenReturn(
                new ResponseEntity<>("{\"status\": \"UP\"}", HttpStatus.OK)
        );

        // Mock ObjectMapper for JSON parsing
        JsonNode jsonNode = mock(JsonNode.class);
        JsonNode statusNode = mock(JsonNode.class);
        when(objectMapper.readTree("{\"status\": \"UP\"}")).thenReturn(jsonNode);
        when(jsonNode.has("status")).thenReturn(true);
        when(jsonNode.get("status")).thenReturn(statusNode);
        when(statusNode.asText()).thenReturn("UP");

        // Manually add servers to simulate what HealthCheckTrigger would do
        ServerHealth health1 = new ServerHealth("http://localhost:9001");
        ServerHealth health2 = new ServerHealth("http://localhost:9002");
        healthCheckService.addServer("http://localhost:9001", health1);
        healthCheckService.addServer("http://localhost:9002", health2);

        // Check server health
        healthCheckService.checkHealth(health1);
        healthCheckService.checkHealth(health2);

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
    void testHealthCheckWithUnhealthyServers() throws Exception {
        // Mock RequestHandler for unhealthy response
        when(requestHandler.sendRequest(any())).thenReturn(
                new ResponseEntity<>("{\"status\": \"DOWN\"}", HttpStatus.OK)
        );

        // Mock ObjectMapper for JSON parsing
        JsonNode jsonNode = mock(JsonNode.class);
        JsonNode statusNode = mock(JsonNode.class);
        when(objectMapper.readTree("{\"status\": \"DOWN\"}")).thenReturn(jsonNode);
        when(jsonNode.has("status")).thenReturn(true);
        when(jsonNode.get("status")).thenReturn(statusNode);
        when(statusNode.asText()).thenReturn("DOWN");

        // Manually add servers and check health
        ServerHealth health1 = new ServerHealth("http://localhost:9001");
        ServerHealth health2 = new ServerHealth("http://localhost:9002");
        healthCheckService.addServer("http://localhost:9001", health1);
        healthCheckService.addServer("http://localhost:9002", health2);

        healthCheckService.checkHealth(health1);
        healthCheckService.checkHealth(health2);

        Map<String, ServerHealth> allHealth = healthCheckService.getAllServerHealth();
        assertEquals(2, allHealth.size());

        Set<String> unhealthyServers = healthCheckService.getUnhealthyServers();
        assertEquals(2, unhealthyServers.size());
        assertTrue(unhealthyServers.contains("http://localhost:9001"));
        assertTrue(unhealthyServers.contains("http://localhost:9002"));
    }

    @Test
    void testGetAllServerHealthWithNoServers() {
        Map<String, ServerHealth> allHealth = healthCheckService.getAllServerHealth();
        assertEquals(0, allHealth.size());
    }


    @Test
    void testGetServerHealthForNonExistentServer() {
        ServerHealth health = healthCheckService.getServerHealth("http://nonexistent:8080");
        assertNull(health);
    }

    @Test
    void testCheckHealthWithValidResponse() throws Exception {
        // Mock RequestHandler for valid response
        when(requestHandler.sendRequest(any())).thenReturn(
                new ResponseEntity<>("{\"status\": \"UP\"}", HttpStatus.OK)
        );

        // Mock ObjectMapper for JSON parsing
        JsonNode jsonNode = mock(JsonNode.class);
        JsonNode statusNode = mock(JsonNode.class);
        when(objectMapper.readTree("{\"status\": \"UP\"}")).thenReturn(jsonNode);
        when(jsonNode.has("status")).thenReturn(true);
        when(jsonNode.get("status")).thenReturn(statusNode);
        when(statusNode.asText()).thenReturn("UP");

        ServerHealth serverHealth = new ServerHealth("http://localhost:9001");
        healthCheckService.checkHealth(serverHealth);

        assertTrue(serverHealth.isHealthy());
        assertNull(serverHealth.getErrorMessage());
    }

    @Test
    void testCheckHealthWithInvalidJsonResponse() {
        // Mock RequestHandler for invalid JSON
        when(requestHandler.sendRequest(any())).thenReturn(
                new ResponseEntity<>("invalid json", HttpStatus.OK)
        );

        ServerHealth serverHealth = new ServerHealth("http://localhost:9001");
        healthCheckService.checkHealth(serverHealth);

        assertTrue(serverHealth.isUnhealthy());
        assertNotNull(serverHealth.getErrorMessage());
        assertTrue(serverHealth.getErrorMessage().contains("Invalid health response"));
    }

    @Test
    void testCheckHealthWithEmptyResponse() {
        // Mock RequestHandler for empty response
        when(requestHandler.sendRequest(any())).thenReturn(
                new ResponseEntity<>("", HttpStatus.OK)
        );

        ServerHealth serverHealth = new ServerHealth("http://localhost:9001");
        healthCheckService.checkHealth(serverHealth);

        assertTrue(serverHealth.isUnhealthy());
        assertNotNull(serverHealth.getErrorMessage());
        assertTrue(serverHealth.getErrorMessage().contains("Invalid health response"));
    }

    @Test
    void testCheckHealthWithMalformedJson() {
        // Mock RequestHandler for malformed JSON
        when(requestHandler.sendRequest(any())).thenReturn(
                new ResponseEntity<>("{\"invalid\": malformed", HttpStatus.OK)
        );

        ServerHealth serverHealth = new ServerHealth("http://localhost:9001");
        healthCheckService.checkHealth(serverHealth);

        assertTrue(serverHealth.isUnhealthy());
        assertNotNull(serverHealth.getErrorMessage());
        assertTrue(serverHealth.getErrorMessage().contains("Invalid health response"));
    }

    @Test
    void testAddServer() {
        ServerHealth health = new ServerHealth("http://localhost:9001");
        healthCheckService.addServer("http://localhost:9001", health);

        ServerHealth retrievedHealth = healthCheckService.getServerHealth("http://localhost:9001");
        assertNotNull(retrievedHealth);
        assertEquals(health, retrievedHealth);

        Map<String, ServerHealth> allHealth = healthCheckService.getAllServerHealth();
        assertEquals(1, allHealth.size());
        assertTrue(allHealth.containsKey("http://localhost:9001"));
    }
}