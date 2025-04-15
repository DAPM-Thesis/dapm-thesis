// TODO: Confirm that should it be JSON or JXES format. currently it's JSON
package communication.message.serialization.deserialization;

import communication.message.Message;
import communication.message.impl.Heartbeat;
import communication.message.impl.Heartbeat.TokenStatus;
import org.json.JSONObject;

import java.time.Instant;
import java.time.format.DateTimeParseException;

public class HeartbeatDeserializationStrategy implements DeserializationStrategy {
    @Override
    public Message deserialize(String payload) {
        try {
            JSONObject json = new JSONObject(payload);
            String senderId = json.getString("senderId");
            String timestampStr = json.getString("timestamp");
            Instant timestamp = Instant.parse(timestampStr);
            String tokenStatusStr = json.getString("tokenStatus");
            TokenStatus tokenStatus = TokenStatus.valueOf(tokenStatusStr);
            boolean immediateFlag = json.getBoolean("immediateFlag");

            return new Heartbeat(this, senderId, timestamp, tokenStatus, immediateFlag);
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Error parsing timestamp in Heartbeat payload", e);
        } catch (Exception e) {
            throw new RuntimeException("Error deserializing Heartbeat payload", e);
        }
    }
}
