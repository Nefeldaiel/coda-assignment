package home.anita;

import home.anita.RoutingConfig.ServerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RandomServerSelectorTest {

    private RandomServerSelector serverSelector;
    private Set<ServerConfig> servers;

    @BeforeEach
    void setUp() {
        serverSelector = new RandomServerSelector();
        
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
    void testSelectServerWithMultipleServers() {
        ServerConfig selected = serverSelector.selectServer(servers);
        
        assertNotNull(selected);
        assertTrue(servers.contains(selected), "Selected server should be from the original set");
        assertNotNull(selected.getUrl(), "Selected server should have a URL");
    }

    @Test
    void testSelectServerWithSingleServer() {
        ServerConfig singleServer = new ServerConfig();
        singleServer.setUrl("http://localhost:8080");
        Set<ServerConfig> singleServerSet = Set.of(singleServer);
        
        ServerConfig selected = serverSelector.selectServer(singleServerSet);
        
        assertNotNull(selected);
        assertEquals(singleServer, selected);
        assertEquals("http://localhost:8080", selected.getUrl());
    }

    @Test
    void testSelectServerRandomness() {
        // Run the selection multiple times to verify randomness
        Set<String> selectedUrls = new HashSet<>();
        
        for (int i = 0; i < 100; i++) {
            ServerConfig selected = serverSelector.selectServer(servers);
            selectedUrls.add(selected.getUrl());
        }
        
        // With 100 iterations and 3 servers, we should see some variety
        // This is probabilistic, but very likely to pass
        assertTrue(selectedUrls.size() > 1, "Should select different servers over multiple iterations");
    }

    @Test
    void testSelectServerWithNullSet() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> serverSelector.selectServer(null)
        );
        
        assertEquals("Server set cannot be null or empty", exception.getMessage());
    }

    @Test
    void testSelectServerWithEmptySet() {
        Set<ServerConfig> emptySet = Collections.emptySet();
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> serverSelector.selectServer(emptySet)
        );
        
        assertEquals("Server set cannot be null or empty", exception.getMessage());
    }

    @Test
    void testSelectServerReturnsValidServer() {
        for (int i = 0; i < 50; i++) {
            ServerConfig selected = serverSelector.selectServer(servers);
            
            assertNotNull(selected, "Selected server should not be null");
            assertNotNull(selected.getUrl(), "Selected server URL should not be null");
            assertFalse(selected.getUrl().isEmpty(), "Selected server URL should not be empty");
            assertTrue(servers.contains(selected), "Selected server should be from the original set");
        }
    }

    @Test
    void testSelectServerDistribution() {
        // Test that selection appears reasonably distributed
        int iterations = 300;
        int[] counts = new int[3];
        String[] urls = {"http://localhost:9001", "http://localhost:9002", "http://localhost:9003"};
        
        for (int i = 0; i < iterations; i++) {
            ServerConfig selected = serverSelector.selectServer(servers);
            String selectedUrl = selected.getUrl();
            
            for (int j = 0; j < urls.length; j++) {
                if (urls[j].equals(selectedUrl)) {
                    counts[j]++;
                    break;
                }
            }
        }
        
        // Each server should be selected at least once in 300 iterations
        for (int count : counts) {
            assertTrue(count > 0, "Each server should be selected at least once");
        }
        
        // No server should be selected more than 90% of the time (very unlikely with random selection)
        for (int count : counts) {
            assertTrue(count < iterations * 0.9, "No server should dominate the selection");
        }
    }

    @Test
    void testSelectServerWithDifferentServerTypes() {
        ServerConfig server1 = new ServerConfig();
        server1.setUrl("http://server1.example.com:8080");
        
        ServerConfig server2 = new ServerConfig();
        server2.setUrl("https://server2.example.com:443");
        
        Set<ServerConfig> mixedServers = Set.of(server1, server2);
        
        ServerConfig selected = serverSelector.selectServer(mixedServers);
        
        assertNotNull(selected);
        assertTrue(mixedServers.contains(selected));
        assertTrue(selected.getUrl().startsWith("http://") || selected.getUrl().startsWith("https://"));
    }
}