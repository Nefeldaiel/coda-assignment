package home.anita;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class HeaderHandler {

    /**
     * Headers that should not be forwarded to downstream services.
     */
    private static final Set<String> SKIPPED_HEADERS = Set.of(
        "host",
        "content-length"
    );
    
    /**
     * Processes incoming headers and returns filtered headers suitable for forwarding.
     * Skips headers that should not be forwarded to downstream services.
     * 
     * @param incomingHeaders The original headers from the incoming request
     * @return HttpHeaders with filtered headers ready for forwarding
     */
    public HttpHeaders processHeaders(HttpHeaders incomingHeaders) {
        HttpHeaders forwardHeaders = new HttpHeaders();
        
        incomingHeaders.forEach((key, value) -> {
            if (!shouldSkipHeader(key)) {
                forwardHeaders.addAll(key, value);
            }
        });
        
        return forwardHeaders;
    }
    
    /**
     * Checks if a header should be skipped during forwarding.
     * 
     * @param headerName The name of the header to check
     * @return true if the header should be skipped, false otherwise
     */
    private boolean shouldSkipHeader(String headerName) {
        return SKIPPED_HEADERS.contains(headerName.toLowerCase());
    }
    
    /**
     * Gets the set of headers that are skipped during forwarding.
     * 
     * @return Unmodifiable set of skipped header names
     */
    public Set<String> getSkippedHeaders() {
        return SKIPPED_HEADERS;
    }
}