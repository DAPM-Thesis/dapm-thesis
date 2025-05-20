package pipeline.processingelement.operator;

import communication.message.Message;
import org.apache.kafka.common.Configurable;
import pipeline.processingelement.Configuration;
import utils.Pair;

import java.util.Map;

public abstract class MiningOperator<O extends Message> extends Operator<Pair<O, Boolean>, O> {

    public MiningOperator(Configuration configuration) { super(configuration); }

    @Override
    protected O convertAlgorithmOutput(Pair<O, Boolean> rawOutput) {
        // Extract the O value from the Pair.
        // Optionally, you could also log or use the Boolean flag.
        return rawOutput.first();
    }

}
