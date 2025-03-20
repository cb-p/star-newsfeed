package nl.utwente.star.message.application;

import com.google.gson.annotations.SerializedName;
import nl.utwente.star.message.Message;

/// NOTIFY-01
public class Notify extends Message {
    @SerializedName("SequenceNumber")
    public int sequenceNumber;

    @SerializedName("IsSnapshot")
    public boolean isSnapshot;

    @SerializedName("Topic")
    public String topic;

    @SerializedName("Content")
    public String content;

    @Override
    public String toString() {
        if (isHeartbeat()) {
            return "Heartbeat{sequenceNumber=" + sequenceNumber + "}";
        }

        return "Notify{" +
                "sequenceNumber=" + sequenceNumber +
                ", isSnapshot=" + isSnapshot +
                ", topic='" + topic + '\'' +
                ", content='" + content + '\'' +
                '}';
    }

    /// HEARTBEAT-02
    public boolean isHeartbeat() {
        return topic.isEmpty() && content.isEmpty() && !isSnapshot;
    }
}
