package communication.API;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import utils.LogUtil;

@Component
public class HTTPClient {
    private static final Logger log = LoggerFactory.getLogger(HTTPClient.class);
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
            LogUtil.error(e, "Request failed. URL: {}, Body: {}", url, body != null ? body : "N/A");
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
            LogUtil.error(e, "Request failed. URL: {}, Body: {}", url, body != null ? body : "N/A");
            throw new RuntimeException("Request failed: " + url, e);
        }
    }
}
