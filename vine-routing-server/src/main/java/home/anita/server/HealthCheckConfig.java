package home.anita.server;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for health check scheduling.
 */
@Configuration
@ConfigurationProperties(prefix = "health-check")
@Data
public class HealthCheckConfig {
    
    /**
     * Health check interval in milliseconds.
     * Default is 10 seconds (10000ms).
     */
    private long interval = 10000;
    
    @Override
    public String toString() {
        return "HealthCheckConfig{interval=" + interval + "ms}";
    }
}