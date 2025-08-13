package home.anita;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;
import java.util.Set;

@Configuration
@ConfigurationProperties(prefix = "routing")
public class RoutingConfig {

    private Set<ServerConfig> servers;

    public Set<ServerConfig> getServers() {
        return servers;
    }

    public void setServers(Set<ServerConfig> servers) {
        this.servers = servers;
    }

    public static class ServerConfig {
        private String url;

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            ServerConfig that = (ServerConfig) obj;
            return Objects.equals(url, that.url);
        }

        @Override
        public int hashCode() {
            return url != null ? url.hashCode() : 0;
        }

        @Override
        public String toString() {
            return "ServerConfig{url='" + url + "'}";
        }
    }
}