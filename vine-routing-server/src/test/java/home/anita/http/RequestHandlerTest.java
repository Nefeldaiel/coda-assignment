package home.anita.http;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

class RequestHandlerTest {

    private RequestHandler requestHandler;

    @BeforeEach
    void setUp() {
        requestHandler = new RequestHandler();
    }

    @Test
    void testRequestHandlerInitialization() {
        assertNotNull(requestHandler);
    }

    // Note: These tests would require a running server to test actual HTTP calls
    // For integration tests, we would mock the WebClient or use test containers
    // Here we test the request handling logic without actual HTTP calls

    @Test
    void testSendRequestWithInvalidUrl() {
        HttpRequest request = new RoutingRequest("invalid-url", "/test", new HttpHeaders(), "test body");
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            requestHandler.sendRequest(request);
        });
        
        assertEquals("Failed to send HTTP request", exception.getMessage());
    }

    @Test
    void testUnsupportedHttpMethod() {
        // Create a custom request with unsupported method for testing
        HttpRequest request = new HttpRequest("http://localhost:8080/test", HttpMethod.PATCH, new HttpHeaders(), null) {};
        
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            requestHandler.sendRequest(request);
        });
        
        assertEquals("Failed to send HTTP request", exception.getMessage());
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertTrue(exception.getCause().getMessage().contains("Unsupported HTTP method"));
    }
}