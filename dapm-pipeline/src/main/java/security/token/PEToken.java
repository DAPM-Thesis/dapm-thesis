package security.token;

import communication.API.HTTPClient;
import communication.API.HTTPResponse;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class PEToken implements AutoCloseable {

    private static final Duration REFRESH_PERIOD = Duration.ofMinutes(4); // fire 1 min before expiry

    private static final Map<String, String> ORG_HOSTS = Map.of(
            "orgA", "http://localhost:8082",
            "orgB", "http://localhost:8083"
    );

    private final String organizationId;
    private final String instanceId;
    private final HTTPClient http;

    private volatile String tokenValue = "bootstrap-placeholder";
    private Instant lastRefresh = Instant.EPOCH;

    private final ScheduledExecutorService scheduler =
            Executors.newSingleThreadScheduledExecutor();

    public PEToken(String organizationId,
                   String instanceId,
                   HTTPClient httpClient) {

        this.organizationId = Objects.requireNonNull(organizationId, "organizationId");
        this.instanceId     = Objects.requireNonNull(instanceId,     "instanceId");
        this.http           = Objects.requireNonNull(httpClient,     "httpClient");

        //refreshOnce(); // first token
        scheduler.scheduleAtFixedRate(
                this::refreshToken,
                REFRESH_PERIOD.toSeconds(),
                REFRESH_PERIOD.toSeconds(),
                TimeUnit.SECONDS);
    }

    public String organizationId() { return organizationId; }
    public String tokenValue()     { return tokenValue; }

    private void refreshToken() {
        try {
            String base = lookupHost(organizationId);
            String enc  = URLEncoder.encode(instanceId, StandardCharsets.UTF_8);
            String url  = base + "/token?instanceId=" + enc;

            HTTPResponse response = http.getSync(url);

            if (response != null &&
                response.status() != null &&
                response.status().is2xxSuccessful() &&
                response.body() != null) {

                tokenValue  = response.body();
                lastRefresh = Instant.now();
                System.out.println("[PEToken] refreshed for " + instanceId);
            } else {
                System.err.println("[PEToken] refresh failed for "
                        + instanceId + " - HTTP status "
                        + (response == null ? "<null>" : response.status()));
            }
        } catch (Exception e) {
            System.err.println("[PEToken] exception refreshing "
                    + instanceId + ": " + e.getMessage());
        }
    }

    private String lookupHost(String orgId) {
        String host = ORG_HOSTS.get(orgId);
        if (host == null)
            throw new IllegalStateException("Unknown organisation: " + orgId);
        return host.replaceAll("/+$", "");
    }

    @Override public void close() { scheduler.shutdownNow(); }

    @Override public String toString() {
        return "PEToken[" + organizationId + "|" + instanceId + "]";
    }
}
