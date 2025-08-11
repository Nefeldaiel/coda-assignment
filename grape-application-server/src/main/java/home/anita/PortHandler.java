package home.anita;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.web.server.PortInUseException;
import org.springframework.context.ConfigurableApplicationContext;

import java.net.BindException;

public class PortHandler {

    private static final Logger logger = LoggerFactory.getLogger(PortHandler.class);

    public static void startWithPortRange(Class<?> mainClass, String[] args) {
        SpringApplication app = new SpringApplication(mainClass);
        ConfigurableApplicationContext tempContext = null;

        try {
            tempContext = app.run(new String[0]);
            AppConfig config = tempContext.getBean(AppConfig.class);

            int defaultPort = config.getPort().getStart();
            int maxPort = config.getPort().getMax();

            tempContext.close();

            int requestedPort = getPortFromArgs(args, "port", defaultPort);

            logger.info("Attempting to start server with port: {}, max port: {}", requestedPort, maxPort);

            for (int port = requestedPort; port <= maxPort; port++) {
                try {
                    logger.info("Trying to start server on port: {}", port);
                    System.setProperty("server.port", String.valueOf(port));

                    app = new SpringApplication(mainClass);
                    app.run(args);

                    logger.info("Server successfully started on port: {}", port);
                    return;

                } catch (Exception e) {
                    if (isPortInUseException(e)) {
                        logger.warn("Port {} is in use, trying next port...", port);
                        if (port == maxPort) {
                            logger.error("All ports from {} to {} are in use. Unable to start server.", requestedPort, maxPort);
                            System.exit(1);
                        }
                    } else {
                        logger.error("Error starting server on port {}: {}", port, e.getMessage());
                        throw e;
                    }
                }
            }
        } catch (Exception e) {
            if (tempContext != null) {
                tempContext.close();
            }
            throw e;
        }
    }


    private static int getPortFromArgs(String[] args, String argName, int defaultValue) {
        for (int i = 0; i < args.length - 1; i++) {
            if (("--" + argName).equals(args[i])) {
                try {
                    return Integer.parseInt(args[i + 1]);
                } catch (NumberFormatException e) {
                    logger.warn("Invalid value for {}: {}. Using default: {}", argName, args[i + 1], defaultValue);
                    return defaultValue;
                }
            }
        }
        return defaultValue;
    }

    private static boolean isPortInUseException(Exception e) {
        Throwable cause = e;
        while (cause != null) {
            if (cause instanceof PortInUseException ||
                    cause instanceof BindException ||
                    (cause.getMessage() != null && cause.getMessage().contains("Address already in use"))) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }
}