package communication;

public interface ProducingProcessingElement {
    void registerProducer(Producer producer);
    boolean stopDataProduction();
    boolean resumeDataProduction();
}
