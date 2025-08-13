package home.anita;

import home.anita.RoutingConfig.ServerConfig;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ServerSelectorTest {

    /**
     * Test implementation of ServerSelector for testing purposes.
     */
    private static class TestServerSelector implements ServerSelector {
        @Override
        public ServerConfig selectServer(Set<ServerConfig> servers) {
            if (servers == null || servers.isEmpty()) {
                throw new IllegalArgumentException("Server set cannot be null or empty");
            }
            return servers.iterator().next(); // Just return the first server
        }
    }

    @Test
    void testServerSelectorInterfaceContract() {
        ServerSelector selector = new TestServerSelector();
        
        ServerConfig server1 = new ServerConfig();
        server1.setUrl("http://localhost:9001");
        
        Set<ServerConfig> servers = new HashSet<>();
        servers.add(server1);
        
        ServerConfig selected = selector.selectServer(servers);
        
        assertNotNull(selected);
        assertEquals(server1, selected);
    }

    @Test
    void testServerSelectorWithNullSet() {
        ServerSelector selector = new TestServerSelector();
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> selector.selectServer(null)
        );
        
        assertEquals("Server set cannot be null or empty", exception.getMessage());
    }

    @Test
    void testServerSelectorWithEmptySet() {
        ServerSelector selector = new TestServerSelector();
        
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> selector.selectServer(Collections.emptySet())
        );
        
        assertEquals("Server set cannot be null or empty", exception.getMessage());
    }
}