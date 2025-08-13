package home.anita;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VineRoutingServer {
    public static void main(String[] args) {
        SpringApplication.run(VineRoutingServer.class, args);
    }
}