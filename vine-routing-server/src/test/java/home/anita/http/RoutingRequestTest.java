package home.anita.http;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import static org.junit.jupiter.api.Assertions.*;

class RoutingRequestTest {

    @Test
    void testRoutingRequestCreation() {
        String serverUrl = "http://localhost:9001";
        String path = "/api/echo";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        String body = "{\"test\": \"data\"}";

        RoutingRequest request = new RoutingRequest(serverUrl, path, headers, body);

        assertEquals(serverUrl + path, request.getUrl());
        assertEquals(HttpMethod.POST, request.getMethod());
        assertEquals(headers, request.getHeaders());
        assertEquals(body, request.getBody());
        assertEquals(path, request.getPath());
        assertEquals(serverUrl, request.getTargetServerUrl());
    }

    @Test
    void testRoutingRequestCreateMethod() {
        String serverUrl = "http://localhost:9002";
        String path = "/health";
        HttpHeaders headers = new HttpHeaders();
        String body = "test body";

        RoutingRequest request = RoutingRequest.create(serverUrl, path, headers, body);

        assertEquals(serverUrl + path, request.getUrl());
        assertEquals(HttpMethod.POST, request.getMethod());
        assertEquals(headers, request.getHeaders());
        assertEquals(body, request.getBody());
        assertEquals(path, request.getPath());
        assertEquals(serverUrl, request.getTargetServerUrl());
    }

    @Test
    void testRoutingRequestToString() {
        String serverUrl = "http://localhost:9001";
        String path = "/api/echo";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Custom-Header", "value");
        String body = "{\"message\": \"test\"}";

        RoutingRequest request = new RoutingRequest(serverUrl, path, headers, body);
        String toString = request.toString();

        assertTrue(toString.contains("RoutingRequest"));
        assertTrue(toString.contains(serverUrl));
        assertTrue(toString.contains(path));
        assertTrue(toString.contains("POST"));
        assertTrue(toString.contains("hasHeaders=true"));
        assertTrue(toString.contains("hasBody=true"));
    }

    @Test
    void testRoutingRequestWithEmptyHeaders() {
        String serverUrl = "http://localhost:9001";
        String path = "/api/echo";
        HttpHeaders emptyHeaders = new HttpHeaders();
        String body = "test";

        RoutingRequest request = new RoutingRequest(serverUrl, path, emptyHeaders, body);

        assertEquals(serverUrl + path, request.getUrl());
        assertEquals(emptyHeaders, request.getHeaders());
        assertTrue(request.getHeaders().isEmpty());
    }

    @Test
    void testRoutingRequestWithNullBody() {
        String serverUrl = "http://localhost:9001";
        String path = "/api/test";
        HttpHeaders headers = new HttpHeaders();

        RoutingRequest request = new RoutingRequest(serverUrl, path, headers, null);

        assertEquals(serverUrl + path, request.getUrl());
        assertNull(request.getBody());
    }
}