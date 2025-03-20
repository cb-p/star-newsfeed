package nl.utwente.star.message.application;

import com.google.gson.annotations.SerializedName;
import nl.utwente.star.message.Message;

import java.util.Arrays;

/// TOPICS-02
public class AvailableTopics extends Message {
    @SerializedName("Topics")
    public String[] topics;

    @Override
    public String toString() {
        return "AvailableTopics{" +
                "topics=" + Arrays.toString(topics) +
                '}';
    }
}
