package home.anita.server;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for health check scheduling.
 */
@Configuration
@ConfigurationProperties(prefix = "health-check")
public class HealthCheckConfig {
    
    /**
     * Health check interval in milliseconds.
     * Default is 10 seconds (10000ms).
     */
    private long interval = 10000;
    
    public long getInterval() {
        return interval;
    }
    
    public void setInterval(long interval) {
        this.interval = interval;
    }
    
    @Override
    public String toString() {
        return "HealthCheckConfig{interval=" + interval + "ms}";
    }
}