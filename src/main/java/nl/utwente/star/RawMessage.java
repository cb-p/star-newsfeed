package nl.utwente.star;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import nl.utwente.star.message.Message;
import nl.utwente.star.message.application.*;
import nl.utwente.star.message.client.ProtocolRequest;
import nl.utwente.star.message.client.SubscribeRequest;

public class RawMessage {
    public static final Gson GSON = new GsonBuilder().create();

    public String name;
    public JsonObject parameters;

    @SuppressWarnings("unused")
    private RawMessage() {
    }

    public RawMessage(Message message) {
        this.name = message.getClass().getSimpleName();
        this.parameters = GSON.toJsonTree(message).getAsJsonObject();
    }

    public RawMessage(String name, JsonObject parameters) {
        this.name = name;
        this.parameters = parameters;
    }

    public static RawMessage parseJson(String json) {
        return GSON.fromJson(json, RawMessage.class);
    }

    public String toJson() {
        return GSON.toJson(this);
    }

    public Message parse() {
        return switch (this.name) {
            case "AvailableTopics" -> GSON.fromJson(this.parameters, AvailableTopics.class);
            case "Fault" -> GSON.fromJson(this.parameters, Fault.class);
            case "NewTopicAvailable" -> GSON.fromJson(this.parameters, NewTopicAvailable.class);
            case "Notify" -> GSON.fromJson(this.parameters, Notify.class);
            case "ProtocolResponse" -> GSON.fromJson(this.parameters, ProtocolResponse.class);
            case "SubscribeResponse" -> GSON.fromJson(this.parameters, SubscribeResponse.class);

            case "ProtocolRequest" -> GSON.fromJson(this.parameters, ProtocolRequest.class);
            case "SubscribeRequest" -> GSON.fromJson(this.parameters, SubscribeRequest.class);

            default ->
                    throw new RuntimeException("unknown message with name '" + this.name + "' and parameters " + this.parameters);
        };
    }
}
