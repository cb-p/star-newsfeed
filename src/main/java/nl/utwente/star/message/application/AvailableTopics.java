package nl.utwente.star.message.application;

import com.google.gson.annotations.SerializedName;
import nl.utwente.star.message.Message;

import java.util.List;

/// TOPICS-02
public class AvailableTopics extends Message {
    @SerializedName("Topics")
    public List<String> topics;

    @Override
    public String toString() {
        return "AvailableTopics{" +
                "topics=" + topics +
                '}';
    }
}
