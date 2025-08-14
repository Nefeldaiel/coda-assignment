package home.anita;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Application configuration properties for the Grape Application Server.
 * Handles port range settings and slow feature configuration.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {

    public static final int START_PORT_DEFAULT_VALUE = 9001;
    public static final int MAX_PORT_DEFAULT_VALUE = 9010;
    public static final boolean SLOW_ENABLED_DEFAULT_VALUE = false;
    public static final long SLOW_SLEEP_TIME_MS_DEFAULT_VALUE = 800L;

    private Port port = new Port();
    private Slow slow = new Slow();

    /**
     * Configuration for server port range when no explicit port is specified.
     */
    @Data
    public static class Port {
        private int start = START_PORT_DEFAULT_VALUE;
        private int max = MAX_PORT_DEFAULT_VALUE;
    }

    /**
     * Configuration for the slow feature that adds artificial delays to responses.
     */
    @Data
    public static class Slow {
        private boolean enabled = SLOW_ENABLED_DEFAULT_VALUE;
        private long sleepTimeMs = SLOW_SLEEP_TIME_MS_DEFAULT_VALUE;
    }
}