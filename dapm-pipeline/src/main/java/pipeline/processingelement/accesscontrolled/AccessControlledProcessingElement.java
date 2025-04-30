package pipeline.processingelement.accesscontrolled;

import pipeline.heartbeat.HeartbeatManager;
import pipeline.processingelement.ProcessingElement;

/**
 * Base class that enriches every PE with
 *  • a JWT used for intra-org authenticated calls  
 *  • a HeartbeatManager that monitors upstream/downstream neighbours
 */
public abstract class AccessControlledProcessingElement
        extends ProcessingElement
        implements AutoCloseable {

    private final PEToken token;         
    private HeartbeatManager hbManager;  

    protected AccessControlledProcessingElement(PEToken token) {
        super();
        this.token = token;
    }

    public String getJwt()            { return token.jwt(); }
    public String getOrganizationId() { return token.organizationId(); }

    public void attachHeartbeatManager(HeartbeatManager m) { this.hbManager = m; }
    public HeartbeatManager getHeartbeatManager()          { return hbManager; }

    @Override
    public void close() {
        token.close();                       
        if (hbManager != null) hbManager.close();
    }
}