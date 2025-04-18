package draft_validation.parsing;

import exceptions.ParsingException;

public class InvalidDraft extends ParsingException {
    public InvalidDraft(String message) {
        super(message);
    }
}
