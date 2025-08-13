package home.anita.server;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ServerHealthTest {

    @Test
    void testServerHealthCreation() {
        String url = "http://localhost:9001";
        ServerHealth serverHealth = new ServerHealth(url);

        assertEquals(url, serverHealth.getUrl());
        assertEquals(ServerHealth.Status.UNHEALTHY, serverHealth.getStatus());
        assertNull(serverHealth.getLastChecked());
        assertNull(serverHealth.getErrorMessage());
        assertTrue(serverHealth.isUnhealthy());
        assertFalse(serverHealth.isHealthy());
    }

    @Test
    void testSetStatusToHealthy() {
        ServerHealth serverHealth = new ServerHealth("http://localhost:9001");
        LocalDateTime beforeUpdate = LocalDateTime.now();

        serverHealth.setStatus(ServerHealth.Status.HEALTHY);

        assertEquals(ServerHealth.Status.HEALTHY, serverHealth.getStatus());
        assertTrue(serverHealth.isHealthy());
        assertFalse(serverHealth.isUnhealthy());
        assertNotNull(serverHealth.getLastChecked());
        assertTrue(serverHealth.getLastChecked().isAfter(beforeUpdate) || serverHealth.getLastChecked().isEqual(beforeUpdate));
        assertNull(serverHealth.getErrorMessage()); // Error message should be cleared when healthy
    }

    @Test
    void testSetStatusToUnhealthy() {
        ServerHealth serverHealth = new ServerHealth("http://localhost:9001");
        serverHealth.setStatus(ServerHealth.Status.HEALTHY); // Start with healthy
        LocalDateTime beforeUpdate = LocalDateTime.now();

        serverHealth.setStatus(ServerHealth.Status.UNHEALTHY);

        assertEquals(ServerHealth.Status.UNHEALTHY, serverHealth.getStatus());
        assertFalse(serverHealth.isHealthy());
        assertTrue(serverHealth.isUnhealthy());
        assertNotNull(serverHealth.getLastChecked());
        assertTrue(serverHealth.getLastChecked().isAfter(beforeUpdate) || serverHealth.getLastChecked().isEqual(beforeUpdate));
    }

    @Test
    void testSetErrorMessage() {
        ServerHealth serverHealth = new ServerHealth("http://localhost:9001");
        String errorMessage = "Connection timeout";

        serverHealth.setErrorMessage(errorMessage);

        assertEquals(errorMessage, serverHealth.getErrorMessage());
    }

    @Test
    void testErrorMessageClearedWhenHealthy() {
        ServerHealth serverHealth = new ServerHealth("http://localhost:9001");
        serverHealth.setErrorMessage("Some error");
        
        assertNotNull(serverHealth.getErrorMessage());
        
        serverHealth.setStatus(ServerHealth.Status.HEALTHY);
        
        assertNull(serverHealth.getErrorMessage());
    }

    @Test
    void testEqualsAndHashCode() {
        String url1 = "http://localhost:9001";
        String url2 = "http://localhost:9002";
        
        ServerHealth server1a = new ServerHealth(url1);
        ServerHealth server1b = new ServerHealth(url1);
        ServerHealth server2 = new ServerHealth(url2);

        // Test equals
        assertEquals(server1a, server1b);
        assertNotEquals(server1a, server2);
        assertNotEquals(server1a, null);
        assertNotEquals(server1a, "not a ServerHealth object");

        // Test hashCode
        assertEquals(server1a.hashCode(), server1b.hashCode());
        assertNotEquals(server1a.hashCode(), server2.hashCode());
    }

    @Test
    void testToString() {
        ServerHealth serverHealth = new ServerHealth("http://localhost:9001");
        serverHealth.setStatus(ServerHealth.Status.HEALTHY);
        serverHealth.setErrorMessage("test error");

        String toString = serverHealth.toString();

        assertNotNull(toString);
        assertTrue(toString.contains("http://localhost:9001"));
        assertTrue(toString.contains("HEALTHY"));
        assertTrue(toString.contains("test error"));
    }

    @Test
    void testStatusEnum() {
        assertEquals("HEALTHY", ServerHealth.Status.HEALTHY.toString());
        assertEquals("UNHEALTHY", ServerHealth.Status.UNHEALTHY.toString());
        
        // Test enum values
        ServerHealth.Status[] values = ServerHealth.Status.values();
        assertEquals(2, values.length);
        assertEquals(ServerHealth.Status.HEALTHY, values[0]);
        assertEquals(ServerHealth.Status.UNHEALTHY, values[1]);
    }
}