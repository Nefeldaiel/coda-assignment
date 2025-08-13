package home.anita;

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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoutingServiceTest {

    @Mock
    private RoutingConfig routingConfig;

    @Mock
    private HeaderHandler headerHandler;

    @Mock
    private ServerSelector serverSelector;

    @InjectMocks
    private RoutingService routingService;

    private Set<RoutingConfig.ServerConfig> mockServers;

    @BeforeEach
    void setUp() {
        RoutingConfig.ServerConfig server1 = new RoutingConfig.ServerConfig();
        server1.setUrl("http://localhost:9001");
        
        RoutingConfig.ServerConfig server2 = new RoutingConfig.ServerConfig();
        server2.setUrl("http://localhost:9002");
        
        mockServers = new HashSet<>();
        mockServers.add(server1);
        mockServers.add(server2);
    }

    @Test
    void testServerSelection() {
        RoutingConfig.ServerConfig selectedServer = mockServers.iterator().next();
        
        when(routingConfig.getServers()).thenReturn(mockServers);
        when(headerHandler.processHeaders(any(HttpHeaders.class))).thenReturn(new HttpHeaders());
        when(serverSelector.selectServer(mockServers)).thenReturn(selectedServer);
        
        HttpHeaders headers = new HttpHeaders();
        String requestBody = "{\"test\": \"data\"}";
        String path = "/api/echo";
        
        // Since we can't easily test actual HTTP calls, we just verify that the method
        // calls the configuration and attempts routing
        for (int i = 0; i < 5; i++) {
            try {
                routingService.routeRequest(requestBody, headers, path);
            } catch (Exception e) {
                // Expected since we're not mocking RestTemplate
            }
        }
        
        // Verify that all components were called multiple times
        verify(routingConfig, atLeast(5)).getServers();
        verify(headerHandler, atLeast(5)).processHeaders(any(HttpHeaders.class));
        verify(serverSelector, atLeast(5)).selectServer(mockServers);
    }

    @Test
    void testNoServersConfigured() {
        when(routingConfig.getServers()).thenReturn(Collections.emptySet());
        
        HttpHeaders headers = new HttpHeaders();
        String requestBody = "{\"test\": \"data\"}";
        String path = "/api/echo";
        
        ResponseEntity<String> response = routingService.routeRequest(requestBody, headers, path);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("{\"status\": \"error\", \"message\": \"No application servers available\"}", response.getBody());
    }

    @Test
    void testNullServersConfiguration() {
        when(routingConfig.getServers()).thenReturn(null);
        
        HttpHeaders headers = new HttpHeaders();
        String requestBody = "{\"test\": \"data\"}";
        String path = "/api/echo";
        
        ResponseEntity<String> response = routingService.routeRequest(requestBody, headers, path);
        
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("{\"status\": \"error\", \"message\": \"No application servers available\"}", response.getBody());
    }

    @Test
    void testHeaderForwarding() {
        when(routingConfig.getServers()).thenReturn(mockServers);
        when(headerHandler.processHeaders(any(HttpHeaders.class))).thenReturn(new HttpHeaders());
        
        HttpHeaders headers = new HttpHeaders();
        headers.add("Custom-Header", "test-value");
        headers.add("Host", "should-be-filtered");
        headers.add("Content-Length", "should-be-filtered");
        
        String requestBody = "{\"test\": \"data\"}";
        String path = "/api/echo";
        
        try {
            routingService.routeRequest(requestBody, headers, path);
        } catch (Exception e) {
        }
        
        verify(routingConfig).getServers();
        verify(headerHandler).processHeaders(headers);
    }
}