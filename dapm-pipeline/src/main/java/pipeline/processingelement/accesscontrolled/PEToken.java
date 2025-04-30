package pipeline.processingelement.accesscontrolled;

import communication.API.HTTPClient;
import communication.API.HTTPResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class PEToken implements AutoCloseable {
    private static final Duration REFRESH_PERIOD = Duration.ofMinutes(5L);
    private final String organizationId;
    private final String elementId;
    private final HTTPClient http;
    private volatile String currentJwt = "initial-placeholder";
    private Instant lastRefresh;
    private final ScheduledExecutorService scheduler;

    public PEToken(String organizationId, String elementId, HTTPClient httpClient) {
        this.lastRefresh = Instant.EPOCH;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        this.organizationId = organizationId;
        this.elementId = (String)Objects.requireNonNull(elementId);
        this.http = (HTTPClient)Objects.requireNonNull(httpClient);
        this.doRefresh();
        this.scheduler.scheduleAtFixedRate(this::doRefresh, REFRESH_PERIOD.toSeconds(), REFRESH_PERIOD.toSeconds(), TimeUnit.SECONDS);
    }

    public String organizationId() {
        return this.organizationId;
    }

    public String jwt() {
        return this.currentJwt;
    }

    private void doRefresh() {
        String var10001;
        try {
            String url = "/" + this.organizationId + "/" + this.elementId + "/token";
            HTTPResponse resp = this.http.getSync(url);
            if (resp.status().is2xxSuccessful() && resp.body() != null) {
                this.currentJwt = resp.body();
                this.lastRefresh = Instant.now();
            } else {
                var10001 = this.elementId;
                System.err.println("[PEToken] refresh failed for " + var10001 + " (" + String.valueOf(resp.status()) + ")");
            }
        } catch (Exception var3) {
            var10001 = this.elementId;
            System.err.println("[PEToken] exception refreshing token for " + var10001 + ": " + var3.getMessage());
        }

    }

    public void close() {
        this.scheduler.shutdownNow();
    }

    public String toString() {
        return "PEToken[" + this.organizationId + "]";
    }
}
