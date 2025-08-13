package home.anita;

import home.anita.RoutingConfig.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Set;

@Service
public class RoutingService {

    private static final Logger logger = LoggerFactory.getLogger(RoutingService.class);

    private final RoutingConfig routingConfig;
    private final HeaderHandler headerHandler;
    private final ServerSelector serverSelector;

    private final RestTemplate restTemplate = new RestTemplate();

    public RoutingService(RoutingConfig routingConfig, HeaderHandler headerHandler, ServerSelector serverSelector) {
        this.routingConfig = routingConfig;
        this.headerHandler = headerHandler;
        this.serverSelector = serverSelector;
    }

    public ResponseEntity<String> routeRequest(String requestBody, HttpHeaders headers, String path) {
        Set<ServerConfig> servers = routingConfig.getServers();

        if (servers == null || servers.isEmpty()) {
            logger.error("No application servers configured");
            String errorJson = "{\"status\": \"error\", \"message\": \"No application servers available\"}";
            return ResponseEntity.internalServerError().body(errorJson);
        }

        ServerConfig selectedServer = serverSelector.selectServer(servers);
        String targetUrl = selectedServer.getUrl() + path;

        logger.info("Routing request to: {}", targetUrl);

        try {
            HttpHeaders forwardHeaders = headerHandler.processHeaders(headers);
            HttpEntity<String> entity = new HttpEntity<>(requestBody, forwardHeaders);

            ResponseEntity<String> response = restTemplate.exchange(
                    targetUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            logger.info("Response received from {}: status={}", targetUrl, response.getStatusCode());
            return response;

        } catch (HttpClientErrorException e) {
            logger.warn("Client error from {}: status={}, body={}", targetUrl, e.getStatusCode(), e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());

        } catch (HttpServerErrorException e) {
            logger.error("Server error from {}: status={}, body={}", targetUrl, e.getStatusCode(), e.getResponseBodyAsString());
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());

        } catch (Exception e) {
            logger.error("Unexpected error routing to {}: {}", targetUrl, e.getMessage());
            return ResponseEntity.internalServerError().body("Internal routing error");
        }
    }

}