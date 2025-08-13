package home.anita;

import home.anita.RoutingConfig.ServerConfig;
import home.anita.server.ServerSelector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Set;

@Service
public class RoutingService {

    private static final Logger logger = LoggerFactory.getLogger(RoutingService.class);

    private final HeaderHandler headerHandler;
    private final ServerSelector serverSelector;
    private final WebClient webClient;

    public RoutingService(HeaderHandler headerHandler, ServerSelector serverSelector) {
        this.headerHandler = headerHandler;
        this.serverSelector = serverSelector;
        // TODO make this a bean
        this.webClient = WebClient.builder().build();
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
        String targetUrl = selectedServer.getUrl() + path;

        logger.info("Routing request to: {}", targetUrl);

        try {
            HttpHeaders forwardHeaders = headerHandler.processHeaders(headers);

            ResponseEntity<String> response = webClient
                    .post()
                    .uri(targetUrl)
                    .headers(httpHeaders -> httpHeaders.addAll(forwardHeaders))
                    .bodyValue(requestBody)
                    .retrieve()
                    .toEntity(String.class)
                    .block();

            logger.info("Response received from {}: status={}", targetUrl, response.getStatusCode());
            return response;

        } catch (WebClientResponseException e) {
            if (e.getStatusCode().is4xxClientError()) {
                logger.warn("Client error from {}: status={}, body={}", targetUrl, e.getStatusCode(), e.getResponseBodyAsString());
            } else {
                logger.error("Server error from {}: status={}, body={}", targetUrl, e.getStatusCode(), e.getResponseBodyAsString());
            }
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());

        } catch (Exception e) {
            logger.error("Unexpected error routing to {}: {}", targetUrl, e.getMessage());
            return ResponseEntity.internalServerError().body("Internal routing error");
        }
    }

}