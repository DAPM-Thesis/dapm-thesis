package pipeline.accesscontrolled.processingelement;

public  class ProcessingElementAccessVerifier {
    public static boolean validateToken(ProcessingElementToken token) {
        if (token == null) {
            return false;
        }
        return token.isValid();
    }
}
