// package pipeline.processingelement.accesscontrolled;

// import communication.API.HTTPClient;
// import communication.API.HTTPResponse;

// import java.time.Duration;
// import java.util.Objects;
// import java.util.concurrent.*;

// public final class AccessVerifier implements AutoCloseable {

//     private static final Duration REFRESH = Duration.ofMinutes(5);

//     /* id info */
//     private final String organizationId;
//     private final String elementId;

//     /* collaborators */
//     private final HTTPClient http;
//     private final ScheduledExecutorService exec =
//             Executors.newSingleThreadScheduledExecutor();

//     /* token container */
//     private final PEToken token;

//     public AccessVerifier(String orgId,
//                           String elementId,
//                           HTTPClient http) {

//         this.organizationId = orgId;
//         this.elementId      = Objects.requireNonNull(elementId);
//         this.http           = Objects.requireNonNull(http);

//         /* first value */
//         this.token = new PEToken(fetch());

//         exec.scheduleAtFixedRate(
//                 this::refreshSafely,
//                 REFRESH.toSeconds(), REFRESH.toSeconds(),
//                 TimeUnit.SECONDS);
//     }

//     public PEToken token() { return token; }

//     /* ------------- helpers ---------------- */
//     private void refreshSafely() {
//         try { token.update(fetch()); }
//         catch (Exception e) {
//             System.err.println("[AccessVerifier] refresh failed: " + e);
//         }
//     }
//     private String fetch() {
//         String url = '/' + organizationId + '/' + elementId + "/token";
//         HTTPResponse resp = http.getSync(url);
//         if (resp.status().is2xxSuccessful() && resp.body()!=null)
//             return resp.body();
//         throw new IllegalStateException("GET " + url + " -> " + resp.status());
//     }

//     @Override public void close() { exec.shutdownNow(); }
// }
