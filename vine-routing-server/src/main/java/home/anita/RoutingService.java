package home.anita;

import home.anita.RoutingConfig.ServerConfig;
import home.anita.http.RequestHandler;
import home.anita.http.RoutingRequest;
import home.anita.server.ServerSelector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Set;

@Service
@Slf4j
public class RoutingService {

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
            log.error("No available servers for routing: {}", e.getMessage());
            String errorJson = "{\"status\": \"error\", \"message\": \"No available servers\"}";
            return ResponseEntity.internalServerError().body(errorJson);
        }

        log.info("Routing request to: {}{}", selectedServer.getUrl(), path);

        try {
            var forwardHeaders = headerHandler.processHeaders(headers);
            var routingRequest = RoutingRequest.create(
                    selectedServer.getUrl(),
                    path,
                    forwardHeaders,
                    requestBody
            );

            var response = requestHandler.sendRequest(routingRequest);

            log.info("Response received from {}: status={}",
                    routingRequest.getUrl(), response != null ? response.getStatusCode() : "null");
            return response;

        } catch (WebClientResponseException e) {
            var targetUrl = selectedServer.getUrl() + path;
            if (e.getStatusCode().is4xxClientError()) {
                log.warn("Client error from {}: status={}, body={}", targetUrl, e.getStatusCode(), e.getResponseBodyAsString());
            } else {
                log.error("Server error from {}: status={}, body={}", targetUrl, e.getStatusCode(), e.getResponseBodyAsString());
            }
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());

        } catch (Exception e) {
            var targetUrl = selectedServer.getUrl() + path;
            log.error("Unexpected error routing to {}: {}", targetUrl, e.getMessage());
            return ResponseEntity.internalServerError().body("Internal routing error");
        }
    }

}