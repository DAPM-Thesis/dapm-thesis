package communication.API;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class HTTPClient {
    private final WebClient webClient;

    @Autowired
    public HTTPClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public String postSync(String url) {
        try {
       return webClient.post().uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
