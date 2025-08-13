package home.anita;

import home.anita.server.HealthAwareServerSelector;
import home.anita.server.HealthCheckService;
import home.anita.server.RandomServerSelector;
import home.anita.server.ServerSelector;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Configuration class for routing components.
 */
@Configuration
public class RoutingConfiguration {

    /**
     * Provides the primary server selector bean.
     * Uses HealthAwareServerSelector by default, which only routes to healthy servers.
     * 
     * @param randomServerSelector The random server selector for fallback
     * @param healthCheckService The health check service for server status
     * @return The configured server selector
     */
    @Bean
    @Primary
    public ServerSelector serverSelector(RandomServerSelector randomServerSelector,
                                         HealthCheckService healthCheckService) {
        return new HealthAwareServerSelector(randomServerSelector, healthCheckService);
    }

    /**
     * Fallback server selector bean if no primary is configured.
     * This ensures the application can still function even if health checking is disabled.
     * 
     * @return Random server selector as fallback
     */
    @Bean
    @ConditionalOnMissingBean(ServerSelector.class)
    public ServerSelector fallbackServerSelector() {
        return new RandomServerSelector();
    }

}