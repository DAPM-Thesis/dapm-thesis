package communication.message.impl.softconformance;

import annotations.AutoRegisterMessage;
import communication.message.Message;
import communication.message.impl.softconformance.models.SoftConformanceStatus;
import communication.message.serialization.MessageVisitor;
import communication.message.serialization.deserialization.DeserializationStrategyRegistration;
import communication.message.serialization.deserialization.impl.SoftConformanceReportDeserializationStrategy;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@AutoRegisterMessage
@DeserializationStrategyRegistration(strategy = SoftConformanceReportDeserializationStrategy.class)
public class SoftConformanceReport extends Message implements Map<String, SoftConformanceStatus> {

    private Map<String, SoftConformanceStatus> content = new HashMap<>();

    @Override
    public int size() {
        return content.size();
    }

    @Override
    public boolean isEmpty() {
        return content.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return content.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return content.containsValue(value);
    }

    @Override
    public SoftConformanceStatus get(Object key) {
        return content.get(key);
    }

    @Override
    public SoftConformanceStatus put(String key, SoftConformanceStatus value) {
        return content.put(key, value);
    }

    @Override
    public SoftConformanceStatus remove(Object key) {
        return content.remove(key);
    }

    @Override
    public void putAll(Map<? extends String, ? extends SoftConformanceStatus> m) {
        content.putAll(m);
    }

    @Override
    public void clear() {
        content.clear();
    }

    @Override
    public Set<String> keySet() {
        return content.keySet();
    }

    @Override
    public Collection<SoftConformanceStatus> values() {
        return content.values();
    }

    @Override
    public Set<Entry<String, SoftConformanceStatus>> entrySet() {
        return content.entrySet();
    }

    @Override
    public void acceptVisitor(MessageVisitor<?> v) {
        v.visit(this);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof SoftConformanceReport otherReport)) return false;
        return content.equals(otherReport.content);
    }
}