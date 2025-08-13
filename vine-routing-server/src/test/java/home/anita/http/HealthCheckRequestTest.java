package home.anita.http;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import static org.junit.jupiter.api.Assertions.*;

class HealthCheckRequestTest {

    @Test
    void testHealthCheckRequestCreation() {
        String serverUrl = "http://localhost:9001";
        
        HealthCheckRequest request = new HealthCheckRequest(serverUrl);
        
        assertEquals(serverUrl + "/health", request.getUrl());
        assertEquals(HttpMethod.GET, request.getMethod());
        assertEquals(serverUrl, request.getServerUrl());
        assertNotNull(request.getHeaders());
        assertNull(request.getBody());
    }

    @Test
    void testHealthCheckRequestCreateMethod() {
        String serverUrl = "http://localhost:9002";
        
        HealthCheckRequest request = HealthCheckRequest.create(serverUrl);
        
        assertEquals(serverUrl + "/health", request.getUrl());
        assertEquals(HttpMethod.GET, request.getMethod());
        assertEquals(serverUrl, request.getServerUrl());
    }

    @Test
    void testGetHealthEndpoint() {
        assertEquals("/health", HealthCheckRequest.getHealthEndpoint());
    }

    @Test
    void testHealthCheckRequestToString() {
        String serverUrl = "http://localhost:9003";
        
        HealthCheckRequest request = new HealthCheckRequest(serverUrl);
        String toString = request.toString();
        
        assertTrue(toString.contains("HealthCheckRequest"));
        assertTrue(toString.contains(serverUrl));
        assertTrue(toString.contains("/health"));
        assertTrue(toString.contains("GET"));
    }

    @Test
    void testHealthCheckRequestWithDifferentPorts() {
        String[] serverUrls = {
            "http://localhost:8080",
            "http://localhost:9001", 
            "http://example.com:8090",
            "https://secure.example.com"
        };
        
        for (String serverUrl : serverUrls) {
            HealthCheckRequest request = HealthCheckRequest.create(serverUrl);
            
            assertEquals(serverUrl + "/health", request.getUrl());
            assertEquals(serverUrl, request.getServerUrl());
            assertEquals(HttpMethod.GET, request.getMethod());
        }
    }

    @Test
    void testHealthCheckRequestHeaders() {
        String serverUrl = "http://localhost:9001";
        
        HealthCheckRequest request = new HealthCheckRequest(serverUrl);
        
        assertNotNull(request.getHeaders());
        assertTrue(request.getHeaders().isEmpty());
    }
}