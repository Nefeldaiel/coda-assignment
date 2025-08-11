package home.anita;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {

    public static final int DEFAULT_START_PORT = 9001;
    public static final int DEFAULT_MAX_PORT = 9010;

    private Port port = new Port();
    
    public Port getPort() {
        return port;
    }
    
    public void setPort(Port port) {
        this.port = port;
    }
    
    public static class Port {
        private int start = DEFAULT_START_PORT;
        private int max = DEFAULT_MAX_PORT;
        
        public int getStart() {
            return start;
        }
        
        public void setStart(int start) {
            this.start = start;
        }
        
        public int getMax() {
            return max;
        }
        
        public void setMax(int max) {
            this.max = max;
        }
    }
}