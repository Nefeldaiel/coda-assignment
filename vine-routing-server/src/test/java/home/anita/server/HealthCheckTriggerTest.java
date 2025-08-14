package home.anita.server;

import home.anita.RoutingConfig;
import home.anita.RoutingConfig.ServerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.event.ApplicationReadyEvent;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthCheckTriggerTest {

    @Mock
    private HealthCheckService healthCheckService;

    @Mock
    private RoutingConfig routingConfig;

    @Mock
    private ApplicationReadyEvent applicationReadyEvent;

    private HealthCheckTrigger healthCheckTrigger;
    private Set<ServerConfig> mockServers;

    @BeforeEach
    void setUp() {
        healthCheckTrigger = new HealthCheckTrigger(healthCheckService, routingConfig);

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
    void testPerformStartupHealthCheckWithServers() {
        when(routingConfig.getServers()).thenReturn(mockServers);

        healthCheckTrigger.performStartupHealthCheck();

        // Verify that addServerHealth was called for each server
        verify(healthCheckService, times(2)).addServer(anyString(), any(ServerHealth.class));
        verify(healthCheckService).addServer(eq("http://localhost:9001"), any(ServerHealth.class));
        verify(healthCheckService).addServer(eq("http://localhost:9002"), any(ServerHealth.class));

        // Verify that checkServerHealth was called for each server
        verify(healthCheckService, times(2)).checkHealth(any(ServerHealth.class));

        // Verify that logHealthCheckSummary was called
        verify(healthCheckService).logHealthCheckSummary();
    }

    @Test
    void testPerformStartupHealthCheckWithNoServers() {
        when(routingConfig.getServers()).thenReturn(new HashSet<>());

        healthCheckTrigger.performStartupHealthCheck();

        // Verify that no server health operations were performed
        verify(healthCheckService, never()).addServer(anyString(), any(ServerHealth.class));
        verify(healthCheckService, never()).checkHealth(any(ServerHealth.class));
        verify(healthCheckService, never()).logHealthCheckSummary();
    }

    @Test
    void testPerformStartupHealthCheckWithNullServers() {
        when(routingConfig.getServers()).thenReturn(null);

        healthCheckTrigger.performStartupHealthCheck();

        // Verify that no server health operations were performed
        verify(healthCheckService, never()).addServer(anyString(), any(ServerHealth.class));
        verify(healthCheckService, never()).checkHealth(any(ServerHealth.class));
        verify(healthCheckService, never()).logHealthCheckSummary();
    }

    @Test
    void testScheduledHealthCheckWithServers() {
        // Mock the health check service to return server health map
        ServerHealth health1 = new ServerHealth("http://localhost:9001");
        ServerHealth health2 = new ServerHealth("http://localhost:9002");
        Map<String, ServerHealth> healthMap = Map.of(
            "http://localhost:9001", health1,
            "http://localhost:9002", health2
        );
        
        when(healthCheckService.getAllServerHealth()).thenReturn(healthMap);
        when(healthCheckService.getHealthyServers()).thenReturn(Set.of("http://localhost:9001", "http://localhost:9002"));
        when(healthCheckService.getUnhealthyServers()).thenReturn(Set.of());

        healthCheckTrigger.scheduledHealthCheck();

        // Verify that checkServerHealth was called for each server
        verify(healthCheckService, times(2)).checkHealth(any(ServerHealth.class));
    }

    @Test
    void testScheduledHealthCheckWithUnhealthyServers() {
        // Mock the health check service to return server health map with unhealthy servers
        ServerHealth health1 = new ServerHealth("http://localhost:9001");
        ServerHealth health2 = new ServerHealth("http://localhost:9002");
        Map<String, ServerHealth> healthMap = Map.of(
            "http://localhost:9001", health1,
            "http://localhost:9002", health2
        );
        
        when(healthCheckService.getAllServerHealth()).thenReturn(healthMap);
        when(healthCheckService.getHealthyServers()).thenReturn(Set.of());
        when(healthCheckService.getUnhealthyServers()).thenReturn(Set.of("http://localhost:9001", "http://localhost:9002"));
        when(healthCheckService.getServerHealth("http://localhost:9001")).thenReturn(health1);
        when(healthCheckService.getServerHealth("http://localhost:9002")).thenReturn(health2);

        healthCheckTrigger.scheduledHealthCheck();

        // Verify that checkServerHealth was called for each server
        verify(healthCheckService, times(2)).checkHealth(any(ServerHealth.class));
        
        // Verify that getServerHealth was called for unhealthy servers logging
        verify(healthCheckService).getServerHealth("http://localhost:9001");
        verify(healthCheckService).getServerHealth("http://localhost:9002");
    }

    @Test
    void testScheduledHealthCheckWithNoServers() {
        when(healthCheckService.getAllServerHealth()).thenReturn(Map.of());

        healthCheckTrigger.scheduledHealthCheck();

        // Verify that no health checks were performed
        verify(healthCheckService, never()).checkHealth(any(ServerHealth.class));
    }
}