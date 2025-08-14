package home.anita;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handles command line slow feature arguments for the application.
 * Processes --slow arguments and configures artificial response delays for testing.
 */
@Component
@Slf4j
public class SlowArgumentHandler extends ArgumentHandler {

    public static final boolean SLOW_FEATURE_ENABLED_DEFAULT_VALUE = false;

    /**
     * Configures slow feature settings from command line arguments.
     * If --slow=true is specified, enables artificial delays in responses.
     * Used for testing latency scenarios and load balancing behavior.
     */
    public void configure(String[] args) {
        boolean slowEnabled = getBooleanFromArgs(args, "slow", SLOW_FEATURE_ENABLED_DEFAULT_VALUE);

        if (slowEnabled) {
            System.setProperty("app.slow.enabled", "true");
        }

        log.info("Slow feature: {}", slowEnabled ? "ENABLED" : "DISABLED");
    }
}