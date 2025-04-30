package communication.API;

public interface HTTPClient {
    HTTPResponse postSync(String url);
    HTTPResponse postSync(String url, String body);
    HTTPResponse putSync(String url);
    HTTPResponse putSync(String url, String body);
    HTTPResponse getSync(String url);
}
