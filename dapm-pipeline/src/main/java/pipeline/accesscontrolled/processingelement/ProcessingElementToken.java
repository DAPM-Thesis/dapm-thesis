package pipeline.accesscontrolled.processingelement;

public class ProcessingElementToken {
    private String tokenValue;
    private String organizationID;

    public ProcessingElementToken(String tokenValue, String organizationID) {
        this.tokenValue = tokenValue;
        this.organizationID = organizationID;
    }

    public String getTokenValue() {
        return tokenValue;
    }

    public String getOrganizationID() {
        return organizationID;
    }

    public boolean isValid() {
        // TODO: Implementing actual code: (or delegating the task to access verifier) || or even should this method be moved to ACPE?
        return tokenValue != null && !tokenValue.isEmpty();
    }

    @Override
    public String toString() {
        return tokenValue + " (" + organizationID + ")";
    }
}
