package home.anita.http;

import lombok.Getter;

import static org.springframework.http.HttpMethod.GET;

/**
 * HTTP request model for health check requests to application servers.
 * Used by HealthCheckService to check server health through RequestHandler.
 */
@Getter
public class HealthCheckRequest extends HttpRequest {

    private static final String HEALTH_ENDPOINT = "/health";

    private final String serverUrl;

    /**
     * Creates a health check request for the specified server.
     *
     * @param serverUrl The base URL of the server to check
     */
    public HealthCheckRequest(String serverUrl) {
        super(serverUrl + HEALTH_ENDPOINT, GET);
        this.serverUrl = serverUrl;
    }

    /**
     * Gets the health endpoint path.
     *
     * @return The health endpoint path ("/health")
     */
    public static String getHealthEndpoint() {
        return HEALTH_ENDPOINT;
    }

    /**
     * Creates a HealthCheckRequest for the specified server.
     *
     * @param serverUrl The base URL of the server to check
     * @return A new HealthCheckRequest instance
     */
    public static HealthCheckRequest create(String serverUrl) {
        return new HealthCheckRequest(serverUrl);
    }

    @Override
    public String toString() {
        return String.format("HealthCheckRequest{serverUrl='%s', fullUrl='%s', method=%s}",
                serverUrl,
                getUrl(),
                getMethod());
    }
}