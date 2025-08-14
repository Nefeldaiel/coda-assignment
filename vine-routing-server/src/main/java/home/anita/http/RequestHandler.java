package home.anita.http;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

/**
 * Centralized HTTP request handler using WebClient.
 * All HTTP requests should go through this handler for consistency and maintainability.
 */
@Component
@Slf4j
public class RequestHandler {

    private final WebClient webClient;

    public RequestHandler() {
        this.webClient = WebClient.builder().build();
    }

    /**
     * Sends an HTTP request using the provided request model.
     *
     * @param request The request model containing all necessary information for the HTTP call
     * @return ResponseEntity containing the response from the target server
     * @throws WebClientResponseException if the request fails with HTTP error status
     * @throws RuntimeException           for other network or processing errors
     */
    public ResponseEntity<String> sendRequest(HttpRequest request) {
        log.debug("Sending {} request to: {}", request.getMethod(), request.getUrl());

        try {
            ResponseSpec responseSpec;

            if (POST.equals(request.getMethod())) {
                var bodySpec = webClient.post()
                        .uri(request.getUrl())
                        .headers(httpHeaders -> addHeaders(httpHeaders, request.getHeaders()));

                if (request.getBody() != null && !request.getBody().isEmpty()) {
                    responseSpec = bodySpec.bodyValue(request.getBody()).retrieve();
                } else {
                    responseSpec = bodySpec.retrieve();
                }
            } else if (GET.equals(request.getMethod())) {
                responseSpec = webClient.get()
                        .uri(request.getUrl())
                        .headers(httpHeaders -> addHeaders(httpHeaders, request.getHeaders()))
                        .retrieve();
            } else {
                throw new IllegalArgumentException("Unsupported HTTP method: " + request.getMethod());
            }

            // Execute request and get response
            var response = responseSpec
                    .toEntity(String.class)
                    .block();

            log.debug("Request to {} completed with status: {}",
                    request.getUrl(), response != null ? response.getStatusCode() : "null");

            return response;

        } catch (WebClientResponseException e) {
            log.warn("HTTP error for request to {}: status={}, body={}",
                    request.getUrl(), e.getStatusCode(), e.getResponseBodyAsString());
            throw e;

        } catch (Exception e) {
            log.error("Unexpected error sending request to {}: {}",
                    request.getUrl(), e.getMessage());
            throw new RuntimeException("Failed to send HTTP request", e);
        }
    }

    /**
     * Helper method to add headers to the WebClient request.
     *
     * @param httpHeaders    The WebClient HttpHeaders object
     * @param requestHeaders The headers to add from the HttpRequest
     */
    private void addHeaders(HttpHeaders httpHeaders, HttpHeaders requestHeaders) {
        if (requestHeaders != null && !requestHeaders.isEmpty()) {
            httpHeaders.addAll(requestHeaders);
        }
    }
}