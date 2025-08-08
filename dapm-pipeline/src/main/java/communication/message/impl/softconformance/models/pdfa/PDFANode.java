package communication.message.impl.softconformance.models.pdfa;

public record PDFANode(String label) {

    public String serialize() { return this.label; }
}
