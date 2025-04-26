package communication.API;

import org.springframework.http.HttpStatusCode;

public record HTTPResponse(HttpStatusCode status, String body) { }
