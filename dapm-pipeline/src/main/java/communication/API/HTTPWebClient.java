package communication.API;

import exceptions.RemoteCallException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class HTTPWebClient implements HTTPClient {
    private final WebClient webClient;

    @Autowired
    public HTTPWebClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public HTTPResponse postSync(String url) {
        return postSync(url, null);
    }

    @Override
    public HTTPResponse postSync(String url, String body) {
        return sendRequest("POST", url, body);
    }

    @Override
    public HTTPResponse putSync(String url) {
        return putSync(url, null);
    }

    @Override
    public HTTPResponse putSync(String url, String body) {
        return sendRequest("PUT", url, body);
    }

    private HTTPResponse sendRequest(String method, String url, String body) {
        try {
            WebClient.RequestBodySpec request = webClient
                    .method(org.springframework.http.HttpMethod.valueOf(method))
                    .uri(url)
                    .header("Content-Type", "application/json");

            var result = (body == null)
                    ? request.exchangeToMono(response ->
                    response.bodyToMono(String.class)
                            .map(b -> new HTTPResponse(response.statusCode(), b))
                            .defaultIfEmpty(new HTTPResponse(response.statusCode(), null))
            ).block()
                    : request.bodyValue(body).exchangeToMono(response ->
                    response.bodyToMono(String.class)
                            .map(b -> new HTTPResponse(response.statusCode(), b))
                            .defaultIfEmpty(new HTTPResponse(response.statusCode(), null))
            ).block();
            if (result == null) {
                throw new RemoteCallException("Received no response at " + url);
            }
            return result;
        } catch (Exception e) {
            throw new RemoteCallException("Request failed: " + url, e);
        }
    }
}