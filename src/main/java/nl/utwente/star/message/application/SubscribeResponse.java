package nl.utwente.star.message.application;

import com.google.gson.annotations.SerializedName;
import nl.utwente.star.message.Message;

import java.util.List;

/// SUBSCR-03
public class SubscribeResponse extends Message.Response {
    @SerializedName("Topics")
    public List<String> topics;

    @Override
    public String toString() {
        return "SubscribeResponse{" +
                "topics=" + topics +
                ", correlationId=" + correlationId +
                '}';
    }
}
