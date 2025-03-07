package json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EventJson {

    private String title;
    private String type;
    private long timestamp;

    public EventJson() {}

    public EventJson(String title, String type, long timestamp) {
        this.title = title;
        this.type = type;
        this.timestamp = timestamp;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "EventJson{" + '\n' +
                "title= " + title + '\n' +
                "type= " + type + '\n' +
                "timestamp= " + timestamp + '\n' +
                '}';
    }
}
