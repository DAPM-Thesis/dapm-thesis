package pipeline.processingelement.accesscontrolled;

import pipeline.heartbeat.HeartbeatManager;
import pipeline.processingelement.ProcessingElement;
import security.token.PEToken;

/**
 * Base class that enriches every PE with
 *  • a JWT used for intra-org authenticated calls  
 *  • a HeartbeatManager that monitors upstream/downstream neighbours
 */
public abstract class AccessControlledProcessingElement implements AutoCloseable {

    private final PEToken token;
    private HeartbeatManager heartbeatManager;

    protected AccessControlledProcessingElement(PEToken token) {
        super();
        this.token = token;
    }

    public String getTokenValue()      { return token.tokenValue(); }
    public String getOrganizationId()  { return token.organizationId(); }

    public void attachHeartbeatManager(HeartbeatManager m) {
        this.heartbeatManager = m;
    }
    public HeartbeatManager getHeartbeatManager() { return heartbeatManager; }

    @Override
    public void close() {
        token.close();
        if (heartbeatManager != null) heartbeatManager.close();
    }
}