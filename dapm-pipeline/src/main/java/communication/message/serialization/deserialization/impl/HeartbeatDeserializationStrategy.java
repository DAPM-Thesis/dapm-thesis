package communication.message.serialization.deserialization.impl;

import java.time.Instant;
import java.util.Map;

import org.apache.commons.logging.Log;

import communication.message.Message;
import communication.message.impl.Heartbeat;
import communication.message.serialization.deserialization.DeserializationStrategy;
import communication.message.serialization.parsing.JSONParser;
import utils.LogUtil;

public class HeartbeatDeserializationStrategy implements DeserializationStrategy {

    @Override
    public Message deserialize(String payloadJson) {
        if (payloadJson == null || payloadJson.isEmpty()) {
            LogUtil.info("[HB DESER ERR] Payload for Heartbeat is null or empty.");
            return null;
        }
        try {
            Object parsed = new JSONParser().parse(payloadJson);
            if (!(parsed instanceof Map)) {
                LogUtil.info("[HB DESER ERR] Parsed payload is not a Map for Heartbeat: {}", payloadJson);
                return null;
            }
            Map<String, Object> map = (Map<String, Object>) parsed;
            String instanceID = (String) map.get("instanceID");
            String timestamp = (String) map.get("timestamp");
            if (instanceID.isEmpty() || instanceID == null || timestamp.isEmpty() || timestamp == null) {
                LogUtil.info("[HB DESER ERR] Missing fields in Heartbeat payload: {}", payloadJson);
                return null;
            }
            Instant time = Instant.parse(timestamp);
            return new Heartbeat(instanceID, time);
        } catch (Exception e) {
            LogUtil.error(e, "[HB DESER ERR] Failed to deserialize Heartbeat payload: {}", payloadJson);
            return null;
        }
    }

}
