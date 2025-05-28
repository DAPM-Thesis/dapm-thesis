package pipeline.processingelement;


public abstract class ProcessingElement {
    protected final Configuration configuration;
    
    private volatile boolean available = true;
    private String instanceID;
    protected HeartbeatManager heartbeatManager;

    public ProcessingElement(Configuration configuration) { 
        this.configuration = configuration;
        this.available = true; 
    }

    public void setInstanceId(String instanceID) {
        this.instanceID = instanceID;
    }
    public String getInstanceId() {
        return instanceID;
    }

    protected void initHeartbeat(String brokerUrl){
        this.heartbeatManager = new HeartbeatManager(this, brokerUrl);
    }

    public abstract boolean start();
    public abstract boolean terminate();

    public boolean isAvailable() {
        return available;
    }
    public void setAvailable(boolean available) {
        this.available = available;
    }

}
