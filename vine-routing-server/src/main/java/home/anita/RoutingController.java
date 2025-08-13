package home.anita;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RoutingController {

    private static final Logger logger = LoggerFactory.getLogger(RoutingController.class);

    private final RoutingService routingService;

    public RoutingController(RoutingService routingService) {
        this.routingService = routingService;
    }

    @PostMapping("/**")
    public ResponseEntity<String> routePost(
            @RequestBody String requestBody,
            @RequestHeader HttpHeaders headers,
            HttpServletRequest request) {

        String path = request.getRequestURI();
        logger.info("Received POST request for path: {}", path);

        return routingService.routeRequest(requestBody, headers, path);
    }
}