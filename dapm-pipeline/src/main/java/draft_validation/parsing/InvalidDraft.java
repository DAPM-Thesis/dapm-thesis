package draft_validation.parsing;

public class InvalidDraft extends RuntimeException {
    public InvalidDraft(String message) {
        super(message);
    }
}
