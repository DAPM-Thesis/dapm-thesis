package communication;

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

    public void post(String url) {
        try {
        webClient.post().uri(url)
                .retrieve()
                .bodyToMono(Void.class)
                .subscribe();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
