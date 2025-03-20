package nl.utwente.star.message.application;

import com.google.gson.annotations.SerializedName;
import nl.utwente.star.message.Message;

/// TOPICS-03
public class NewTopicAvailable extends Message {
    @SerializedName("Topic")
    public String topic;

    @Override
    public String toString() {
        return "NewTopicAvailable{" +
                "topic='" + topic + '\'' +
                '}';
    }
}
