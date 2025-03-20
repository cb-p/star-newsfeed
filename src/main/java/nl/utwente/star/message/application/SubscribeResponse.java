package nl.utwente.star.message.application;

import com.google.gson.annotations.SerializedName;
import nl.utwente.star.message.Message;

import java.util.Arrays;

/// SUBSCR-03
public class SubscribeResponse extends Message.Response {
    @SerializedName("Topics")
    public String[] topics;

    @Override
    public String toString() {
        return "SubscribeResponse{" +
                "topics=" + Arrays.toString(topics) +
                ", correlationId=" + correlationId +
                '}';
    }
}
