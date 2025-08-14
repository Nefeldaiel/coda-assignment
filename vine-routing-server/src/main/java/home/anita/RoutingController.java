package home.anita;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class RoutingController {

    private final RoutingService routingService;
    private final RoutingConfig routingConfig;

    public RoutingController(RoutingService routingService, RoutingConfig routingConfig) {
        this.routingService = routingService;
        this.routingConfig = routingConfig;
    }

    @PostMapping("/**")
    public ResponseEntity<String> routePost(
            @RequestBody String requestBody,
            @RequestHeader HttpHeaders headers,
            HttpServletRequest request) {

        String path = request.getRequestURI();
        log.info("Received POST request for path: {}", path);

        return routingService.routeRequest(requestBody, headers, path, routingConfig.getServers());
    }
}