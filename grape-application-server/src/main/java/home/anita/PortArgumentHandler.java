package home.anita;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handles command line port arguments for the application.
 * Processes --port arguments and sets system properties for explicit port configuration.
 */
@Component
@Slf4j
public class PortArgumentHandler extends ArgumentHandler {

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
            log.info("Explicit port specified: {}", explicitPort);
        } else {
            log.info("No explicit port specified, will use port range from configuration");
        }
    }
}