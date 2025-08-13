package home.anita;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
    "routing.servers[0].url=http://test1:9001",
    "routing.servers[1].url=http://test2:9002"
})
class RoutingConfigTest {

    @Autowired
    private RoutingConfig routingConfig;

    @Test
    void testConfigurationLoading() {
        assertNotNull(routingConfig.getServers());
        assertEquals(2, routingConfig.getServers().size());
        
        // Check that both URLs are present in the Set
        boolean hasServer1 = routingConfig.getServers().stream()
            .anyMatch(server -> "http://test1:9001".equals(server.getUrl()));
        boolean hasServer2 = routingConfig.getServers().stream()
            .anyMatch(server -> "http://test2:9002".equals(server.getUrl()));
        
        assertTrue(hasServer1, "Should contain server1");
        assertTrue(hasServer2, "Should contain server2");
    }

    @Test
    void testServerConfigSettersAndGetters() {
        RoutingConfig.ServerConfig server = new RoutingConfig.ServerConfig();
        server.setUrl("http://example:8080");
        
        assertEquals("http://example:8080", server.getUrl());
    }
}