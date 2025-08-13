package home.anita;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class HeaderHandlerTest {

    private HeaderHandler headerHandler;

    @BeforeEach
    void setUp() {
        headerHandler = new HeaderHandler();
    }

    @Test
    void testProcessHeadersWithAllowedHeaders() {
        HttpHeaders incomingHeaders = new HttpHeaders();
        incomingHeaders.add("Content-Type", "application/json");
        incomingHeaders.add("Authorization", "Bearer token123");
        incomingHeaders.add("Custom-Header", "custom-value");

        HttpHeaders processedHeaders = headerHandler.processHeaders(incomingHeaders);

        assertEquals(3, processedHeaders.size());
        assertEquals("application/json", processedHeaders.getFirst("Content-Type"));
        assertEquals("Bearer token123", processedHeaders.getFirst("Authorization"));
        assertEquals("custom-value", processedHeaders.getFirst("Custom-Header"));
    }

    @Test
    void testProcessHeadersSkipsHostHeader() {
        HttpHeaders incomingHeaders = new HttpHeaders();
        incomingHeaders.add("Content-Type", "application/json");
        incomingHeaders.add("Host", "example.com");
        incomingHeaders.add("Authorization", "Bearer token123");

        HttpHeaders processedHeaders = headerHandler.processHeaders(incomingHeaders);

        assertEquals(2, processedHeaders.size());
        assertEquals("application/json", processedHeaders.getFirst("Content-Type"));
        assertEquals("Bearer token123", processedHeaders.getFirst("Authorization"));
        assertNull(processedHeaders.getFirst("Host"));
    }

    @Test
    void testProcessHeadersSkipsContentLengthHeader() {
        HttpHeaders incomingHeaders = new HttpHeaders();
        incomingHeaders.add("Content-Type", "application/json");
        incomingHeaders.add("Content-Length", "123");
        incomingHeaders.add("Authorization", "Bearer token123");

        HttpHeaders processedHeaders = headerHandler.processHeaders(incomingHeaders);

        assertEquals(2, processedHeaders.size());
        assertEquals("application/json", processedHeaders.getFirst("Content-Type"));
        assertEquals("Bearer token123", processedHeaders.getFirst("Authorization"));
        assertNull(processedHeaders.getFirst("Content-Length"));
    }

    @Test
    void testProcessHeadersSkipsMultipleSkippedHeaders() {
        HttpHeaders incomingHeaders = new HttpHeaders();
        incomingHeaders.add("Content-Type", "application/json");
        incomingHeaders.add("Host", "example.com");
        incomingHeaders.add("Content-Length", "123");
        incomingHeaders.add("Authorization", "Bearer token123");

        HttpHeaders processedHeaders = headerHandler.processHeaders(incomingHeaders);

        assertEquals(2, processedHeaders.size());
        assertEquals("application/json", processedHeaders.getFirst("Content-Type"));
        assertEquals("Bearer token123", processedHeaders.getFirst("Authorization"));
        assertNull(processedHeaders.getFirst("Host"));
        assertNull(processedHeaders.getFirst("Content-Length"));
    }

    @Test
    void testProcessHeadersCaseInsensitive() {
        HttpHeaders incomingHeaders = new HttpHeaders();
        incomingHeaders.add("Content-Type", "application/json");
        incomingHeaders.add("HOST", "example.com"); // Uppercase
        incomingHeaders.add("content-length", "123"); // Lowercase
        incomingHeaders.add("Authorization", "Bearer token123");

        HttpHeaders processedHeaders = headerHandler.processHeaders(incomingHeaders);

        assertEquals(2, processedHeaders.size());
        assertEquals("application/json", processedHeaders.getFirst("Content-Type"));
        assertEquals("Bearer token123", processedHeaders.getFirst("Authorization"));
        assertNull(processedHeaders.getFirst("HOST"));
        assertNull(processedHeaders.getFirst("content-length"));
    }

    @Test
    void testProcessHeadersWithEmptyHeaders() {
        HttpHeaders incomingHeaders = new HttpHeaders();

        HttpHeaders processedHeaders = headerHandler.processHeaders(incomingHeaders);

        assertEquals(0, processedHeaders.size());
    }

    @Test
    void testProcessHeadersWithMultipleValues() {
        HttpHeaders incomingHeaders = new HttpHeaders();
        incomingHeaders.add("Accept", "application/json");
        incomingHeaders.add("Accept", "text/html");
        incomingHeaders.add("Host", "example.com");

        HttpHeaders processedHeaders = headerHandler.processHeaders(incomingHeaders);

        assertEquals(1, processedHeaders.size());
        List<String> acceptValues = processedHeaders.get("Accept");
        assertEquals(2, acceptValues.size());
        assertTrue(acceptValues.contains("application/json"));
        assertTrue(acceptValues.contains("text/html"));
        assertNull(processedHeaders.getFirst("Host"));
    }

    @Test
    void testGetSkippedHeaders() {
        Set<String> skippedHeaders = headerHandler.getSkippedHeaders();

        assertEquals(2, skippedHeaders.size());
        assertTrue(skippedHeaders.contains("host"));
        assertTrue(skippedHeaders.contains("content-length"));
    }

    @Test
    void testSkippedHeadersIsUnmodifiable() {
        Set<String> skippedHeaders = headerHandler.getSkippedHeaders();

        assertThrows(UnsupportedOperationException.class, () -> {
            skippedHeaders.add("new-header");
        });
    }
}