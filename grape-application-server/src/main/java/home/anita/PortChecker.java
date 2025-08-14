package home.anita;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Customizes web server port configuration at startup.
 * Uses explicit port if specified via command line, otherwise finds available port in range.
 * Implements Spring's WebServerFactoryCustomizer to configure the embedded server.
 */
@Component
@RequiredArgsConstructor
public class PortChecker implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {

    private static final Logger logger = LoggerFactory.getLogger(PortChecker.class);

    private final AppConfig appConfig;

    /**
     * Configures the web server port based on explicit port or available port range.
     * If explicit port is set, uses that port directly (will fail if port is in use).
     * Otherwise, searches for available port within the configured range.
     */
    @Override
    public void customize(ConfigurableWebServerFactory factory) {
        String explicitPort = System.getProperty("server.port.explicit");

        if (explicitPort != null) {
            // Port was explicitly set via command line argument
            int port = Integer.parseInt(explicitPort);
            logger.info("Using explicitly requested port: {}", port);
            factory.setPort(port);
            return;
        }

        // No explicit port, try default port and increment if needed
        int startPort = appConfig.getPort().getStart();
        int maxPort = appConfig.getPort().getMax();

        logger.info("Searching for available port from {} to {}", startPort, maxPort);

        for (int port = startPort; port <= maxPort; port++) {
            if (isPortAvailable(port)) {
                logger.info("Found available port: {}", port);
                factory.setPort(port);
                return;
            } else {
                logger.debug("Port {} is in use, trying next port", port);
            }
        }

        throw new RuntimeException(String.format(
                "All ports from %d to %d are in use. Unable to start server.", startPort, maxPort));
    }

    /**
     * Checks if a specific port is available for use.
     */
    private boolean isPortAvailable(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}