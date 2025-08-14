package home.anita.http;

import lombok.Getter;
import org.springframework.http.HttpHeaders;

import static org.springframework.http.HttpMethod.POST;

/**
 * HTTP request model for routing requests to application servers.
 * Used by RoutingService to forward requests through RequestHandler.
 */
@Getter
public class RoutingRequest extends HttpRequest {

    private final String path;
    private final String targetServerUrl;

    /**
     * Creates a routing request for forwarding to an application server.
     *
     * @param targetServerUrl The base URL of the target application server
     * @param path            The path to append to the server URL
     * @param headers         The HTTP headers to forward
     * @param body            The request body to forward
     */
    public RoutingRequest(String targetServerUrl, String path, HttpHeaders headers, String body) {
        super(targetServerUrl + path, POST, headers, body);
        this.path = path;
        this.targetServerUrl = targetServerUrl;
    }

    /**
     * Creates a RoutingRequest from the provided parameters.
     *
     * @param targetServerUrl The base URL of the target server
     * @param path            The request path
     * @param headers         The HTTP headers to forward (will be processed by HeaderHandler)
     * @param requestBody     The request body to forward
     * @return A new RoutingRequest instance
     */
    public static RoutingRequest create(String targetServerUrl, String path, HttpHeaders headers, String requestBody) {
        return new RoutingRequest(targetServerUrl, path, headers, requestBody);
    }

    @Override
    public String toString() {
        return String.format("RoutingRequest{targetServer='%s', path='%s', method=%s, hasHeaders=%s, hasBody=%s}",
                targetServerUrl,
                path,
                getMethod(),
                getHeaders() != null && !getHeaders().isEmpty(),
                getBody() != null && !getBody().isEmpty());
    }
}