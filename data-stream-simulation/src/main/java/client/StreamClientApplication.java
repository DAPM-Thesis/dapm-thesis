package client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class StreamClientApplication {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(StreamClientApplication.class, args);
        StreamClient client = context.getBean(StreamClient.class);
        client.startStream();
    }
}
