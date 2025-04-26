package communication.API;

import exceptions.RemoteCallException;
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
    public HTTPResponse postSync(String url) {
        return postSync(url, null);
    }

    public HTTPResponse postSync(String url, String body) {
        return sendRequest("POST", url, body);
    }

    public HTTPResponse putSync(String url) {
        return putSync(url, null);
    }

    public HTTPResponse putSync(String url, String body) {
        return sendRequest("PUT", url, body);
    }

    private HTTPResponse sendRequest(String method, String url, String body) {
        try {
            WebClient.RequestBodySpec request = webClient
                    .method(org.springframework.http.HttpMethod.valueOf(method))
                    .uri(url)
                    .header("Content-Type", "application/json");

            return (body == null)
                    ? request.exchangeToMono(response ->
                    response.bodyToMono(String.class)
                            .map(b -> new HTTPResponse(response.statusCode(), b))
            ).block()
                    : request.bodyValue(body).exchangeToMono(response ->
                    response.bodyToMono(String.class)
                            .map(b -> new HTTPResponse(response.statusCode(), b))
            ).block();
        } catch (Exception e) {
            throw new RemoteCallException("Request failed: " + url, e);
        }
    }
}