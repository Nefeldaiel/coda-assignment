package home.anita.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ServerHealthMapTest {

    private ServerHealthMap serverHealthMap;
    private ServerHealth health1;
    private ServerHealth health2;
    private ServerHealth health3;

    @BeforeEach
    void setUp() {
        serverHealthMap = new ServerHealthMap();
        
        health1 = new ServerHealth("http://localhost:9001");
        health2 = new ServerHealth("http://localhost:9002"); 
        health3 = new ServerHealth("http://localhost:9003");
    }

    @Test
    void testAddServer() {
        serverHealthMap.addServer("http://localhost:9001", health1);
        
        ServerHealth retrieved = serverHealthMap.getServerHealth("http://localhost:9001");
        assertNotNull(retrieved);
        assertEquals(health1, retrieved);
        assertEquals(1, serverHealthMap.getTotalServerCount());
    }

    @Test
    void testGetServerHealthForNonExistentServer() {
        ServerHealth health = serverHealthMap.getServerHealth("http://nonexistent:8080");
        assertNull(health);
    }

    @Test
    void testGetAllServerHealth() {
        serverHealthMap.addServer("http://localhost:9001", health1);
        serverHealthMap.addServer("http://localhost:9002", health2);
        
        Map<String, ServerHealth> allHealth = serverHealthMap.getAllServerHealth();
        assertEquals(2, allHealth.size());
        assertTrue(allHealth.containsKey("http://localhost:9001"));
        assertTrue(allHealth.containsKey("http://localhost:9002"));
        
        // Verify it's a defensive copy
        allHealth.clear();
        assertEquals(2, serverHealthMap.getTotalServerCount());
    }

    @Test
    void testGetHealthyServers() {
        // Set up mixed health statuses
        health1.setStatus(ServerHealth.Status.HEALTHY);
        health2.setStatus(ServerHealth.Status.UNHEALTHY);
        health3.setStatus(ServerHealth.Status.HEALTHY);
        
        serverHealthMap.addServer("http://localhost:9001", health1);
        serverHealthMap.addServer("http://localhost:9002", health2);
        serverHealthMap.addServer("http://localhost:9003", health3);
        
        Set<String> healthyServers = serverHealthMap.getHealthyServers();
        assertEquals(2, healthyServers.size());
        assertTrue(healthyServers.contains("http://localhost:9001"));
        assertTrue(healthyServers.contains("http://localhost:9003"));
        assertFalse(healthyServers.contains("http://localhost:9002"));
    }

    @Test
    void testGetUnhealthyServers() {
        // Set up mixed health statuses  
        health1.setStatus(ServerHealth.Status.HEALTHY);
        health2.setStatus(ServerHealth.Status.UNHEALTHY);
        health3.setStatus(ServerHealth.Status.UNHEALTHY);
        
        serverHealthMap.addServer("http://localhost:9001", health1);
        serverHealthMap.addServer("http://localhost:9002", health2);
        serverHealthMap.addServer("http://localhost:9003", health3);
        
        Set<String> unhealthyServers = serverHealthMap.getUnhealthyServers();
        assertEquals(2, unhealthyServers.size());
        assertTrue(unhealthyServers.contains("http://localhost:9002"));
        assertTrue(unhealthyServers.contains("http://localhost:9003"));
        assertFalse(unhealthyServers.contains("http://localhost:9001"));
    }

    @Test
    void testGetCounts() {
        assertEquals(0, serverHealthMap.getTotalServerCount());
        assertEquals(0, serverHealthMap.getHealthyServerCount());
        assertEquals(0, serverHealthMap.getUnhealthyServerCount());
        
        // Add servers with different statuses
        health1.setStatus(ServerHealth.Status.HEALTHY);
        health2.setStatus(ServerHealth.Status.UNHEALTHY);
        health3.setStatus(ServerHealth.Status.HEALTHY);
        
        serverHealthMap.addServer("http://localhost:9001", health1);
        serverHealthMap.addServer("http://localhost:9002", health2);
        serverHealthMap.addServer("http://localhost:9003", health3);
        
        assertEquals(3, serverHealthMap.getTotalServerCount());
        assertEquals(2, serverHealthMap.getHealthyServerCount());
        assertEquals(1, serverHealthMap.getUnhealthyServerCount());
    }

    @Test
    void testIsEmpty() {
        assertTrue(serverHealthMap.isEmpty());
        
        serverHealthMap.addServer("http://localhost:9001", health1);
        assertFalse(serverHealthMap.isEmpty());
        
        serverHealthMap.clear();
        assertTrue(serverHealthMap.isEmpty());
    }

    @Test
    void testForEachServer() {
        serverHealthMap.addServer("http://localhost:9001", health1);
        serverHealthMap.addServer("http://localhost:9002", health2);
        
        int[] count = {0};
        serverHealthMap.forEachServer(health -> count[0]++);
        
        assertEquals(2, count[0]);
    }

    @Test
    void testClear() {
        serverHealthMap.addServer("http://localhost:9001", health1);
        serverHealthMap.addServer("http://localhost:9002", health2);
        
        assertFalse(serverHealthMap.isEmpty());
        assertEquals(2, serverHealthMap.getTotalServerCount());
        
        serverHealthMap.clear();
        
        assertTrue(serverHealthMap.isEmpty());
        assertEquals(0, serverHealthMap.getTotalServerCount());
        assertNull(serverHealthMap.getServerHealth("http://localhost:9001"));
    }

    @Test 
    void testLogHealthCheckSummary() {
        // This test mainly ensures the method doesn't throw exceptions
        // The actual logging behavior would need integration testing
        health1.setStatus(ServerHealth.Status.HEALTHY);
        health2.setStatus(ServerHealth.Status.UNHEALTHY);
        health2.setErrorMessage("Connection refused");
        
        serverHealthMap.addServer("http://localhost:9001", health1);
        serverHealthMap.addServer("http://localhost:9002", health2);
        
        // Should not throw any exceptions
        assertDoesNotThrow(() -> serverHealthMap.logHealthCheckSummary());
    }
}