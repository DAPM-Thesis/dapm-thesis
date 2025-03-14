package client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import datatype.event.Event;
import datatype.serialization.DataTypeSerializer;
import json.EventJson;
import service.Producer;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.util.retry.Retry;
import util.JXESParser;

import java.time.Duration;
import java.util.Date;
import java.util.HashSet;

@Service
public class StreamClient {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final Producer producer;

    public StreamClient(WebClient.Builder webClientBuilder) {
        this.producer = new Producer();
        String URL = "https://stream.wikimedia.org/v2/stream/recentchange";
        this.webClient = webClientBuilder.baseUrl(URL).build();
        this.objectMapper = new ObjectMapper();
    }

    public void startStream() {

        try {
            webClient.get()
                    .retrieve()
                    .bodyToFlux(String.class)
                    // In case of a time-out of more than 10 seconds,
                    // attempt 3 connection retries with 3 seconds delay between each attempt
                    .timeout(Duration.ofSeconds(10))
                    .retryWhen(Retry.backoff(3, Duration.ofSeconds(3)))
                    .filter(data -> data != null && data.contains("en.wikipedia.org"))
                    .<Event>handle((data, sink) -> {
                        EventJson eventJson;
                        try {
                            eventJson = objectMapper.readValue(data, EventJson.class);
                        } catch (JsonProcessingException e) {
                            sink.error(new RuntimeException("Error converting event to JXES", e));
                            return;
                        }
                        // This step might not be necessary, immediately parse to JXES in subscribe()
                        sink.next(new Event(
                                eventJson.getTitle(),
                                eventJson.getType(),
                                new Date(eventJson.getTimestamp() * 1000).toString(),
                                new HashSet<>()
                        ));
                    })
                    .subscribe(event -> {

                        DataTypeSerializer dataTypeSerializer = new DataTypeSerializer();
                        String JXESEvent = dataTypeSerializer.visit(event);

                        System.out.println("Received: \n" + JXESEvent);

                        this.producer.publish("ingest", JXESEvent);
                    });
        }
        catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
    }
}
