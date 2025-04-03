package nl.utwente.star.amp;

import PluginAdapter.Api.ConfigurationOuterClass;
import PluginAdapter.Api.LabelOuterClass.Label;
import PluginAdapter.Api.LabelOuterClass.Label.Parameter.Value;
import com.axini.adapter.generic.AxiniProtobuf;
import com.axini.adapter.generic.Handler;
import com.google.gson.*;
import com.google.protobuf.ByteString;
import nl.utwente.star.NewsfeedClient;
import nl.utwente.star.RawMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("CallToPrintStackTrace")
public class NewsfeedHandler extends Handler {
    private NewsfeedClient client;

    private void setupReceiveThread() {
        new Thread(() -> {
            while (true) {
                String raw = client.waitAndReceiveString();
                if (raw == null) {
                    break;
                }

                RawMessage message = RawMessage.parseJson(raw);
                Label label = AxiniProtobuf.createLabel(
                        message.name, "newsfeed",
                        Label.LabelType.RESPONSE,
                        message.parameters
                                .entrySet().stream()
                                .map(entry -> AxiniProtobuf.createParameter(
                                        entry.getKey(), valueFromJsonElement(entry.getValue())))
                                .toList());

                long timestamp = AxiniProtobuf.timestamp();
                adapterCore.sendResponse(label, ByteString.copyFromUtf8(raw), timestamp);
            }
        }).start();
    }

    @Override
    public ConfigurationOuterClass.Configuration defaultConfiguration() {
        return AxiniProtobuf.createConfiguration(List.of(
                AxiniProtobuf.createItem("manufacturer", "Name of Manufacturer", "Axini")));
    }

    @Override
    public void start() {
        if (client == null) {
            try {
                ConfigurationOuterClass.Configuration config = getConfiguration();
                String manufacturer = AxiniProtobuf.getStringFromConfig(config, "manufacturer");

                client = new NewsfeedClient();
                client.sendString("MANUFACTURER:" + manufacturer);
                setupReceiveThread();
                sendReadyToAmp();
            } catch (IOException e) {
                sendErrorToAmp("start error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void stop() {
        if (client != null) {
            try {
                client.close();
                client = null;
            } catch (Exception e) {
                sendErrorToAmp("stop error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void reset() {
        stop();
        start();
    }

    @Override
    public ByteString stimulate(Label stimulus) {
        String name = stimulus.getLabel();

        JsonObject parameters = new JsonObject();
        for (Label.Parameter parameter : stimulus.getParametersList()) {
            parameters.add(parameter.getName(), jsonElementFromValue(parameter.getValue()));
        }

        RawMessage message = new RawMessage(name, parameters);
        String raw = message.toJson();

        System.out.println("-> " + raw);

        ByteString physicalLabel = ByteString.copyFromUtf8(raw);
        long timestamp = AxiniProtobuf.timestamp();

        Label confirmation = AxiniProtobuf.createLabel(stimulus,
                physicalLabel, timestamp, stimulus.getCorrelationId());
        adapterCore.sendStimulusConfirmation(confirmation);

        try {
            client.sendString(raw);
        } catch (IOException e) {
            sendErrorToAmp("client message sending error: " + e.getMessage());
            e.printStackTrace();
        }

        return physicalLabel;
    }

    @Override
    public List<Label> getSupportedLabels() {
        // This is going to produce errors, but that's not important.
        return List.of();
    }

    public Value valueFromJsonElement(JsonElement element) {
        if (element instanceof JsonArray array) {
            List<JsonElement> values = new ArrayList<>();
            array.iterator().forEachRemaining(values::add);

            return Value.newBuilder()
                    .setArray(Value.Array.newBuilder()
                            .addAllValues(values.stream().map(this::valueFromJsonElement).toList())
                            .build())
                    .build();
        } else if (element instanceof JsonPrimitive primitive) {
            if (primitive.isBoolean()) {
                return Value.newBuilder().setBoolean(primitive.getAsBoolean()).build();
            } else if (primitive.isString()) {
                return Value.newBuilder().setString(primitive.getAsString()).build();
            } else if (primitive.isNumber()) {
                return Value.newBuilder().setInteger(primitive.getAsInt()).build();
            }
        }

        return Value.newBuilder().build();
    }

    public JsonElement jsonElementFromValue(Value value) {
        return switch (value.getTypeCase()) {
            case STRING -> new JsonPrimitive(value.getString());
            case INTEGER -> new JsonPrimitive(value.getInteger());
            case DECIMAL -> new JsonPrimitive(value.getDecimal());
            case BOOLEAN -> new JsonPrimitive(value.getBoolean());
            case ARRAY -> {
                JsonArray array = new JsonArray();
                value.getArray().getValuesList().stream()
                        .map(this::jsonElementFromValue)
                        .forEach(array::add);
                yield array;
            }
            default -> JsonNull.INSTANCE;
        };
    }

    public void sendReadyToAmp() {
        adapterCore.sendReady();
    }

    public void sendErrorToAmp(String message) {
        adapterCore.sendError(message);
    }
}
