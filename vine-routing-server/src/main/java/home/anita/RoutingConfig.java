package home.anita;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
@ConfigurationProperties(prefix = "routing")
@Data
public class RoutingConfig {

    private Set<ServerConfig> servers;

    @Data
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    public static class ServerConfig {
        @EqualsAndHashCode.Include
        private String url;

        @Override
        public String toString() {
            return "ServerConfig{url='" + url + "'}";
        }
    }
}