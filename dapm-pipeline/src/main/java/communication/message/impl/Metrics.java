package communication.message.impl;

import annotations.AutoRegisterMessage;
import communication.message.Message;
import communication.message.serialization.MessageVisitor;
import communication.message.serialization.deserialization.DeserializationStrategyRegistration;
import communication.message.serialization.deserialization.impl.MetricsDeserializationStrategy;

import java.util.List;

@AutoRegisterMessage
@DeserializationStrategyRegistration(strategy = MetricsDeserializationStrategy.class)
public class Metrics extends Message {
    private final List<Integer> metrics;

    public Metrics(Integer... metrics) { this.metrics = List.of(metrics); }

    public Metrics(List<Integer> metrics) { this.metrics = metrics; }

    @Override
    public void acceptVisitor(MessageVisitor<?> visitor) {
        visitor.visit(this);
    }

    @Override
    public String toString() {
        return metrics.toString();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Metrics otherMetrics)) return false;
        return metrics.equals(otherMetrics.metrics);
    }

    @Override
    public int hashCode() {
        return metrics.hashCode();
    }
}
