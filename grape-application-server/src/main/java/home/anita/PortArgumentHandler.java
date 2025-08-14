package home.anita;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Handles command line port arguments for the application.
 * Processes --port arguments and sets system properties for explicit port configuration.
 */
@Component
public class PortArgumentHandler extends ArgumentHandler {

    private static final Logger logger = LoggerFactory.getLogger(PortArgumentHandler.class);
    public static final int NO_PORT_SPECIFIED = -1;

    /**
     * Configures port settings from command line arguments.
     * If --port is specified, sets system property for explicit port use.
     * Otherwise, allows PortChecker to use configured port range.
     */
    public void configure(String[] args) {
        int explicitPort = getIntFromArgs(args, "port", NO_PORT_SPECIFIED);

        if (explicitPort != NO_PORT_SPECIFIED) {
            System.setProperty("server.port.explicit", String.valueOf(explicitPort));
            logger.info("Explicit port specified: {}", explicitPort);
        } else {
            logger.info("No explicit port specified, will use port range from configuration");
        }
    }
}