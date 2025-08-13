package home.anita;

import home.anita.RoutingConfig.ServerConfig;
import home.anita.http.RequestHandler;
import home.anita.server.ServerSelector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoutingServiceTest {

    @Mock
    private HeaderHandler headerHandler;

    @Mock
    private ServerSelector serverSelector;

    @Mock
    private RequestHandler requestHandler;

    @InjectMocks
    private RoutingService routingService;

    private Set<ServerConfig> mockServers;

    @BeforeEach
    void setUp() {
        ServerConfig server1 = new ServerConfig();
        server1.setUrl("http://localhost:9001");

        ServerConfig server2 = new ServerConfig();
        server2.setUrl("http://localhost:9002");

        mockServers = new HashSet<>();
        mockServers.add(server1);
        mockServers.add(server2);
    }

    @Test
    void testServerSelection() {
        ServerConfig selectedServer = mockServers.iterator().next();

        when(headerHandler.processHeaders(any(HttpHeaders.class))).thenReturn(new HttpHeaders());
        when(serverSelector.select(mockServers)).thenReturn(selectedServer);
        when(requestHandler.sendRequest(any())).thenReturn(ResponseEntity.ok("Success"));

        HttpHeaders headers = new HttpHeaders();
        String requestBody = "{\"test\": \"data\"}";
        String path = "/api/echo";

        ResponseEntity<String> response = routingService.routeRequest(requestBody, headers, path, mockServers);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Success", response.getBody());

        // Verify that all components were called
        verify(headerHandler).processHeaders(any(HttpHeaders.class));
        verify(serverSelector).select(mockServers);
        verify(requestHandler).sendRequest(any());
    }

    @Test
    void testNoServersConfigured() {
        when(serverSelector.select(Collections.emptySet())).thenThrow(new IllegalArgumentException("No servers available"));

        HttpHeaders headers = new HttpHeaders();
        String requestBody = "{\"test\": \"data\"}";
        String path = "/api/echo";

        ResponseEntity<String> response = routingService.routeRequest(requestBody, headers, path, Collections.emptySet());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("{\"status\": \"error\", \"message\": \"No available servers\"}", response.getBody());
    }

    @Test
    void testNullServersConfiguration() {
        when(serverSelector.select(null)).thenThrow(new IllegalArgumentException("Server set cannot be null"));

        HttpHeaders headers = new HttpHeaders();
        String requestBody = "{\"test\": \"data\"}";
        String path = "/api/echo";

        ResponseEntity<String> response = routingService.routeRequest(requestBody, headers, path, null);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("{\"status\": \"error\", \"message\": \"No available servers\"}", response.getBody());
    }

    @Test
    void testHeaderForwarding() {
        ServerConfig selectedServer = mockServers.iterator().next();
        when(serverSelector.select(mockServers)).thenReturn(selectedServer);
        when(headerHandler.processHeaders(any(HttpHeaders.class))).thenReturn(new HttpHeaders());
        when(requestHandler.sendRequest(any())).thenReturn(ResponseEntity.ok("Success"));

        HttpHeaders headers = new HttpHeaders();
        headers.add("Custom-Header", "test-value");
        headers.add("Host", "should-be-filtered");
        headers.add("Content-Length", "should-be-filtered");

        String requestBody = "{\"test\": \"data\"}";
        String path = "/api/echo";

        ResponseEntity<String> response = routingService.routeRequest(requestBody, headers, path, mockServers);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Success", response.getBody());

        verify(headerHandler).processHeaders(headers);
        verify(requestHandler).sendRequest(any());
    }
}