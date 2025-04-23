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
        return postSync(url, null);
    }

    public String postSync(String url, String body) {
        try {
            WebClient.RequestBodySpec request = webClient.post()
                    .uri(url)
                    .header("Content-Type", "application/json");

            WebClient.ResponseSpec response = (body == null)
                    ? request.retrieve()
                    : request.bodyValue(body).retrieve();

            return response.bodyToMono(String.class).block();
        } catch (Exception e) {
            throw new RuntimeException("Request failed: " + url, e);
        }
    }

    public String putSync(String url) {
        return putSync(url, null);
    }

    public String putSync(String url, String body) {
        try {
            WebClient.RequestBodySpec request = webClient.put()
                    .uri(url)
                    .header("Content-Type", "application/json");

            WebClient.ResponseSpec response = (body == null)
                    ? request.retrieve()
                    : request.bodyValue(body).retrieve();

            return response.bodyToMono(String.class).block();
        } catch (Exception e) {
            throw new RuntimeException("Request failed: " + url, e);
        }
    }
}
