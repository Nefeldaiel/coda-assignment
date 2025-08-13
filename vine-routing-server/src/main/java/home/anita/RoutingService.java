package home.anita;

import home.anita.RoutingConfig.ServerConfig;
import home.anita.http.RequestHandler;
import home.anita.http.RoutingRequest;
import home.anita.server.ServerSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Set;

@Service
public class RoutingService {

    private static final Logger logger = LoggerFactory.getLogger(RoutingService.class);

    private final HeaderHandler headerHandler;
    private final ServerSelector serverSelector;
    private final RequestHandler requestHandler;

    public RoutingService(HeaderHandler headerHandler, ServerSelector serverSelector, RequestHandler requestHandler) {
        this.headerHandler = headerHandler;
        this.serverSelector = serverSelector;
        this.requestHandler = requestHandler;
    }

    public ResponseEntity<String> routeRequest(String requestBody, HttpHeaders headers, String path, Set<ServerConfig> servers) {
        ServerConfig selectedServer;
        try {
            selectedServer = serverSelector.select(servers);
        } catch (IllegalArgumentException e) {
            logger.error("No available servers for routing: {}", e.getMessage());
            String errorJson = "{\"status\": \"error\", \"message\": \"No available servers\"}";
            return ResponseEntity.internalServerError().body(errorJson);
        }

        logger.info("Routing request to: {}{}", selectedServer.getUrl(), path);

        try {
            var forwardHeaders = headerHandler.processHeaders(headers);
            var routingRequest = RoutingRequest.create(
                selectedServer.getUrl(), 
                path, 
                forwardHeaders, 
                requestBody
            );

            var response = requestHandler.sendRequest(routingRequest);

            logger.info("Response received from {}: status={}", 
                routingRequest.getUrl(), response != null ? response.getStatusCode() : "null");
            return response;

        } catch (WebClientResponseException e) {
            var targetUrl = selectedServer.getUrl() + path;
            if (e.getStatusCode().is4xxClientError()) {
                logger.warn("Client error from {}: status={}, body={}", targetUrl, e.getStatusCode(), e.getResponseBodyAsString());
            } else {
                logger.error("Server error from {}: status={}, body={}", targetUrl, e.getStatusCode(), e.getResponseBodyAsString());
            }
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());

        } catch (Exception e) {
            var targetUrl = selectedServer.getUrl() + path;
            logger.error("Unexpected error routing to {}: {}", targetUrl, e.getMessage());
            return ResponseEntity.internalServerError().body("Internal routing error");
        }
    }

}