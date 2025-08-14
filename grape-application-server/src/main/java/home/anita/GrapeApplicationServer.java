package home.anita;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class GrapeApplicationServer {
    public static void main(String[] args) {

        // TODO Fix this complex logic
        // Create a temporary context to get the handler beans
        SpringApplication tempApp = new SpringApplication(GrapeApplicationServer.class);
        tempApp.setLogStartupInfo(false);

        try (ConfigurableApplicationContext tempContext = tempApp.run(new String[]{"--spring.main.web-application-type=none"})) {
            SlowArgumentHandler slow = tempContext.getBean(SlowArgumentHandler.class);
            PortArgumentHandler port = tempContext.getBean(PortArgumentHandler.class);

            slow.configure(args);
            port.configure(args);
        }

        // Now run the actual application
        SpringApplication.run(GrapeApplicationServer.class, args);
    }
}