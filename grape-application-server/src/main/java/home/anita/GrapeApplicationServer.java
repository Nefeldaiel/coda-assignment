package home.anita;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
public class GrapeApplicationServer {
    public static void main(String[] args) {
        PortHandler.startWithPortRange(GrapeApplicationServer.class, args);
    }
}