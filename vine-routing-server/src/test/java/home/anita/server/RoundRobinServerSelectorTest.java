package home.anita.server;

import home.anita.RoutingConfig.ServerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoundRobinServerSelectorTest {

    private RoundRobinServerSelector serverSelector;

    @Mock
    private RandomServerSelector randomServerSelector;

    @Mock
    private HealthCheckService healthCheckService;


    private Set<ServerConfig> servers;

    @BeforeEach
    void setUp() {
        serverSelector = new RoundRobinServerSelector(randomServerSelector, healthCheckService);

        ServerConfig server1 = new ServerConfig();
        server1.setUrl("http://localhost:9001");

        ServerConfig server2 = new ServerConfig();
        server2.setUrl("http://localhost:9002");

        ServerConfig server3 = new ServerConfig();
        server3.setUrl("http://localhost:9003");

        servers = new HashSet<>();
        servers.add(server1);
        servers.add(server2);
        servers.add(server3);
    }

    @Test
    void testRoundRobinSelectionWithAllHealthyServers() {
        // Mock all servers as healthy
        Set<String> healthyUrls = Set.of(
                "http://localhost:9001",
                "http://localhost:9002",
                "http://localhost:9003"
        );
        when(healthCheckService.getHealthyServers()).thenReturn(healthyUrls);

        // Reset counter to ensure predictable order
        serverSelector.resetCounter();

        // Test round-robin behavior - should cycle through servers
        Set<String> selectedUrls = new HashSet<>();
        for (int i = 0; i < 6; i++) { // 2 full cycles
            ServerConfig selected = serverSelector.select(servers);
            selectedUrls.add(selected.getUrl());
        }

        // Should have visited all 3 servers
        assertEquals(3, selectedUrls.size());
        assertTrue(selectedUrls.contains("http://localhost:9001"));
        assertTrue(selectedUrls.contains("http://localhost:9002"));
        assertTrue(selectedUrls.contains("http://localhost:9003"));
    }

    @Test
    void testRoundRobinOrderConsistency() {
        // Mock all servers as healthy
        Set<String> healthyUrls = Set.of(
                "http://localhost:9001",
                "http://localhost:9002",
                "http://localhost:9003"
        );
        when(healthCheckService.getHealthyServers()).thenReturn(healthyUrls);

        serverSelector.resetCounter();

        // First selection cycle
        String[] firstCycle = new String[3];
        for (int i = 0; i < 3; i++) {
            firstCycle[i] = serverSelector.select(servers).getUrl();
        }

        // Reset and do another cycle
        serverSelector.resetCounter();
        String[] secondCycle = new String[3];
        for (int i = 0; i < 3; i++) {
            secondCycle[i] = serverSelector.select(servers).getUrl();
        }

        // Order should be consistent
        assertArrayEquals(firstCycle, secondCycle);
    }

    @Test
    void testRoundRobinWithPartiallyHealthyServers() {
        // Only servers 1 and 3 are healthy
        Set<String> healthyUrls = Set.of(
                "http://localhost:9001",
                "http://localhost:9003"
        );
        when(healthCheckService.getHealthyServers()).thenReturn(healthyUrls);

        serverSelector.resetCounter();

        // Should only select from healthy servers (1 and 3)
        Set<String> selectedUrls = new HashSet<>();
        for (int i = 0; i < 10; i++) {
            ServerConfig selected = serverSelector.select(servers);
            selectedUrls.add(selected.getUrl());
        }

        assertEquals(2, selectedUrls.size());
        assertTrue(selectedUrls.contains("http://localhost:9001"));
        assertTrue(selectedUrls.contains("http://localhost:9003"));
        assertFalse(selectedUrls.contains("http://localhost:9002")); // Unhealthy server
    }

    @Test
    void testFallbackToAllServersWhenNoneHealthy() {
        // No healthy servers
        when(healthCheckService.getHealthyServers()).thenReturn(Collections.emptySet());

        serverSelector.resetCounter();

        // Should use all servers as fallback
        Set<String> selectedUrls = new HashSet<>();
        for (int i = 0; i < 9; i++) { // 3 full cycles
            ServerConfig selected = serverSelector.select(servers);
            selectedUrls.add(selected.getUrl());
        }

        // Should have used all 3 servers
        assertEquals(3, selectedUrls.size());
        assertTrue(selectedUrls.contains("http://localhost:9001"));
        assertTrue(selectedUrls.contains("http://localhost:9002"));
        assertTrue(selectedUrls.contains("http://localhost:9003"));
    }

    @Test
    void testEvenDistribution() {
        // Mock all servers as healthy
        Set<String> healthyUrls = Set.of(
                "http://localhost:9001",
                "http://localhost:9002",
                "http://localhost:9003"
        );
        when(healthCheckService.getHealthyServers()).thenReturn(healthyUrls);

        serverSelector.resetCounter();

        // Count selections over multiple cycles
        int[] counts = new int[3];
        String[] urls = {"http://localhost:9001", "http://localhost:9002", "http://localhost:9003"};

        int iterations = 300; // 100 full cycles
        for (int i = 0; i < iterations; i++) {
            ServerConfig selected = serverSelector.select(servers);
            String selectedUrl = selected.getUrl();

            for (int j = 0; j < urls.length; j++) {
                if (urls[j].equals(selectedUrl)) {
                    counts[j]++;
                    break;
                }
            }
        }

        // Each server should be selected exactly 100 times (perfect round-robin)
        for (int count : counts) {
            assertEquals(100, count, "Each server should be selected exactly " + (iterations / 3) + " times");
        }
    }

    @Test
    void testSingleServerSelection() {
        ServerConfig singleServer = new ServerConfig();
        singleServer.setUrl("http://localhost:8080");
        Set<ServerConfig> singleServerSet = Set.of(singleServer);

        when(healthCheckService.getHealthyServers()).thenReturn(Set.of("http://localhost:8080"));

        // Should always return the same server
        for (int i = 0; i < 10; i++) {
            ServerConfig selected = serverSelector.select(singleServerSet);
            assertEquals(singleServer, selected);
            assertEquals("http://localhost:8080", selected.getUrl());
        }
    }

    @Test
    void testSelectWithNullSet() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> serverSelector.select(null)
        );

        assertEquals("Server set cannot be null or empty", exception.getMessage());
    }

    @Test
    void testSelectWithEmptySet() {
        Set<ServerConfig> emptySet = Collections.emptySet();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> serverSelector.select(emptySet)
        );

        assertEquals("Server set cannot be null or empty", exception.getMessage());
    }

    @Test
    void testCounterIncrement() {
        Set<String> healthyUrls = Set.of("http://localhost:9001");
        when(healthCheckService.getHealthyServers()).thenReturn(healthyUrls);

        serverSelector.resetCounter();
        assertEquals(0, serverSelector.getCurrentCounter());

        serverSelector.select(servers);
        assertEquals(1, serverSelector.getCurrentCounter());

        serverSelector.select(servers);
        assertEquals(2, serverSelector.getCurrentCounter());
    }

    @Test
    void testCounterReset() {
        Set<String> healthyUrls = Set.of("http://localhost:9001");
        when(healthCheckService.getHealthyServers()).thenReturn(healthyUrls);

        // Make some selections
        serverSelector.select(servers);
        serverSelector.select(servers);
        assertTrue(serverSelector.getCurrentCounter() > 0);

        // Reset counter
        serverSelector.resetCounter();
        assertEquals(0, serverSelector.getCurrentCounter());
    }

    @Test
    void testCounterOverflow() {
        Set<String> healthyUrls = Set.of(
                "http://localhost:9001",
                "http://localhost:9002"
        );
        when(healthCheckService.getHealthyServers()).thenReturn(healthyUrls);

        // Test with a large number of selections to ensure counter wrapping works
        Set<String> selectedUrls = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            ServerConfig selected = serverSelector.select(servers);
            selectedUrls.add(selected.getUrl());
        }

        // Should still only select from the 2 healthy servers
        assertEquals(2, selectedUrls.size());
        assertTrue(selectedUrls.contains("http://localhost:9001"));
        assertTrue(selectedUrls.contains("http://localhost:9002"));
    }

    @Test
    void testHealthCheckServiceIntegration() {
        // Verify that health check service is called
        Set<String> healthyUrls = Set.of("http://localhost:9001");
        when(healthCheckService.getHealthyServers()).thenReturn(healthyUrls);

        serverSelector.select(servers);

        verify(healthCheckService, times(1)).getHealthyServers();
    }
}