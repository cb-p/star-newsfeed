package nl.utwente.star.message.client;

import com.google.gson.annotations.SerializedName;
import nl.utwente.star.message.Message;

import java.util.Arrays;

/// SUBSCR-02
public class SubscribeRequest extends Message.Request {
    @SerializedName("Topics")
    public String[] topics;

    public SubscribeRequest(int correlationId, String... topics) {
        super(correlationId);
        this.topics = topics;
    }

    @Override
    public String toString() {
        return "SubscribeRequest{" +
                "topics=" + Arrays.toString(topics) +
                ", correlationId=" + correlationId +
                '}';
    }
}
