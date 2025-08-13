package home.anita.server;

import home.anita.RoutingConfig.ServerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthAwareServerSelectorTest {

    @Mock
    private RandomServerSelector randomServerSelector;

    @Mock
    private HealthCheckService healthCheckService;

    private HealthAwareServerSelector healthAwareSelector;
    private Set<ServerConfig> allServers;
    private ServerConfig server1;
    private ServerConfig server2;
    private ServerConfig server3;

    @BeforeEach
    void setUp() {
        healthAwareSelector = new HealthAwareServerSelector(randomServerSelector, healthCheckService);

        server1 = new ServerConfig();
        server1.setUrl("http://localhost:9001");
        
        server2 = new ServerConfig();
        server2.setUrl("http://localhost:9002");
        
        server3 = new ServerConfig();
        server3.setUrl("http://localhost:9003");

        allServers = new HashSet<>();
        allServers.add(server1);
        allServers.add(server2);
        allServers.add(server3);
    }

    @Test
    void testSelectFromHealthyServersOnly() {
        // Setup: server1 and server2 are healthy, server3 is not
        Set<String> healthyUrls = Set.of("http://localhost:9001", "http://localhost:9002");
        when(healthCheckService.getHealthyServers()).thenReturn(healthyUrls);
        when(randomServerSelector.select(any())).thenReturn(server1);

        ServerConfig selected = healthAwareSelector.select(allServers);

        assertEquals(server1, selected);
        
        // Verify that randomServerSelector was called with only healthy servers
        verify(randomServerSelector).select(argThat(servers ->
            servers.size() == 2 && 
            servers.contains(server1) && 
            servers.contains(server2) &&
            !servers.contains(server3)
        ));
    }

    @Test
    void testSelectWhenAllServersHealthy() {
        // Setup: all servers are healthy
        Set<String> healthyUrls = Set.of("http://localhost:9001", "http://localhost:9002", "http://localhost:9003");
        when(healthCheckService.getHealthyServers()).thenReturn(healthyUrls);
        when(randomServerSelector.select(any())).thenReturn(server2);

        ServerConfig selected = healthAwareSelector.select(allServers);

        assertEquals(server2, selected);
        
        // Verify that randomServerSelector was called with all servers
        verify(randomServerSelector).select(allServers);
    }

    @Test
    void testSelectWhenNoServersHealthy() {
        // Setup: no servers are healthy
        Set<String> healthyUrls = Set.of();
        when(healthCheckService.getHealthyServers()).thenReturn(healthyUrls);
        when(randomServerSelector.select(any())).thenReturn(server3);

        ServerConfig selected = healthAwareSelector.select(allServers);

        assertEquals(server3, selected);
        
        // Verify that randomServerSelector was called with all servers as fallback
        verify(randomServerSelector).select(allServers);
    }

    @Test
    void testSelectServerWithSingleHealthy() {
        // Setup: only server2 is healthy
        Set<String> healthyUrls = Set.of("http://localhost:9002");
        when(healthCheckService.getHealthyServers()).thenReturn(healthyUrls);
        when(randomServerSelector.select(any())).thenReturn(server2);

        ServerConfig selected = healthAwareSelector.select(allServers);

        assertEquals(server2, selected);
        
        // Verify that randomServerSelector was called with only the healthy server
        verify(randomServerSelector).select(argThat(servers ->
            servers.size() == 1 && servers.contains(server2)
        ));
    }

    @Test
    void testSelectServerWithNullSet() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> healthAwareSelector.select(null)
        );
        
        assertEquals("Server set cannot be null or empty", exception.getMessage());
        verifyNoInteractions(healthCheckService);
        verifyNoInteractions(randomServerSelector);
    }

    @Test
    void testSelectServerWithEmptySet() {
        Set<ServerConfig> emptySet = Set.of();
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> healthAwareSelector.select(emptySet)
        );
        
        assertEquals("Server set cannot be null or empty", exception.getMessage());
        verifyNoInteractions(healthCheckService);
        verifyNoInteractions(randomServerSelector);
    }

    @Test
    void testSelectServerWhenHealthyServersNotInSet() {
        // Setup: healthy servers include URLs not in the server set
        Set<String> healthyUrls = Set.of("http://localhost:8080", "http://localhost:8081");
        when(healthCheckService.getHealthyServers()).thenReturn(healthyUrls);
        when(randomServerSelector.select(any())).thenReturn(server1);

        ServerConfig selected = healthAwareSelector.select(allServers);

        assertEquals(server1, selected);
        
        // Should fall back to all servers since no intersection with healthy servers
        verify(randomServerSelector).select(allServers);
    }

    @Test
    void testSelectWithPartialIntersection() {
        // Setup: some healthy servers are in the server set, some are not
        Set<String> healthyUrls = Set.of("http://localhost:9001", "http://localhost:8080");
        when(healthCheckService.getHealthyServers()).thenReturn(healthyUrls);
        when(randomServerSelector.select(any())).thenReturn(server1);

        ServerConfig selected = healthAwareSelector.select(allServers);

        assertEquals(server1, selected);
        
        // Should select from the intersection (only server1)
        verify(randomServerSelector).select(argThat(servers ->
            servers.size() == 1 && servers.contains(server1)
        ));
    }

    @Test
    void testMultipleCallsWithChangingHealthStatus() {
        // First call: server1 is healthy
        Set<String> healthyUrls1 = Set.of("http://localhost:9001");
        when(healthCheckService.getHealthyServers()).thenReturn(healthyUrls1);
        when(randomServerSelector.select(any())).thenReturn(server1);

        ServerConfig selected1 = healthAwareSelector.select(allServers);
        assertEquals(server1, selected1);

        // Second call: server2 is healthy (server1 became unhealthy)
        Set<String> healthyUrls2 = Set.of("http://localhost:9002");
        when(healthCheckService.getHealthyServers()).thenReturn(healthyUrls2);
        when(randomServerSelector.select(any())).thenReturn(server2);

        ServerConfig selected2 = healthAwareSelector.select(allServers);
        assertEquals(server2, selected2);

        // Verify both calls were made with appropriate server sets
        verify(randomServerSelector).select(argThat(servers ->
            servers.size() == 1 && servers.contains(server1)
        ));
        verify(randomServerSelector).select(argThat(servers ->
            servers.size() == 1 && servers.contains(server2)
        ));
    }
}