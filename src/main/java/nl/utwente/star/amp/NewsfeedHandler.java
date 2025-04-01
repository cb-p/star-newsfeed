package nl.utwente.star.amp;

import PluginAdapter.Api.ConfigurationOuterClass;
import PluginAdapter.Api.LabelOuterClass.Label;
import PluginAdapter.Api.LabelOuterClass.Label.Parameter.Value;
import com.axini.adapter.generic.AxiniProtobuf;
import com.axini.adapter.generic.Handler;
import com.google.protobuf.ByteString;
import nl.utwente.star.NewsfeedClient;
import nl.utwente.star.message.Message;
import nl.utwente.star.message.application.*;
import nl.utwente.star.message.client.ProtocolRequest;
import nl.utwente.star.message.client.StopSession;
import nl.utwente.star.message.client.SubscribeRequest;
import nl.utwente.star.message.client.Unsubscribe;

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

                Message message = client.decode(raw);
                System.out.println("<- " + message);

                Label label;
                if (message instanceof ProtocolResponse response) {
                    label = NewsfeedLabels.protocolResponse(response.correlationId, response.protocolVersion);
                } else if (message instanceof AvailableTopics response) {
                    label = NewsfeedLabels.availableTopics(response.topics);
                } else if (message instanceof SubscribeResponse response) {
                    label = NewsfeedLabels.subscribeResponse(response.correlationId, response.topics);
                } else if (message instanceof Notify response) {
                    if (response.isHeartbeat()) {
                        label = NewsfeedLabels.heartbeat();
                    } else {
                        label = NewsfeedLabels.notify(response.sequenceNumber, response.isSnapshot, response.topic, response.content);
                    }
                } else if (message instanceof NewTopicAvailable response) {
                    label = NewsfeedLabels.newTopicAvailable(response.topic);
                } else if (message instanceof Fault) {
                    label = NewsfeedLabels.fault();
                } else {
                    System.out.println("  - unhandled!");
                    sendErrorToAmp("unhandled message " + message);
                    continue;
                }

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
        Message message;
        switch (stimulus.getLabel()) {
            case "protocol_request" -> message = new ProtocolRequest(
                    (int) param(stimulus, "correlation_id").getInteger(),
                    param(stimulus, "protocol_versions").getArray().getValuesList()
                            .stream().map(Value::getString).toList()
            );
            case "stop_session" -> message = new StopSession();
            case "unsubscribe" -> message = new Unsubscribe();
            case "subscribe_request" -> message = new SubscribeRequest(
                    (int) param(stimulus, "correlation_id").getInteger(),
                    param(stimulus, "topics").getArray().getValuesList()
                            .stream().map(Value::getString).toList()
            );
            default -> {
                sendErrorToAmp("unknown stimulus " + stimulus.getLabel());
                return ByteString.EMPTY;
            }
        }

        System.out.println("-> " + message);

        String raw = client.encode(message);
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
        List<Label> labels = new ArrayList<>();

        // Stimuli
        labels.add(NewsfeedLabels.protocolRequest(0, List.of("foo")));
        labels.add(NewsfeedLabels.stopSession());
        labels.add(NewsfeedLabels.unsubscribe());
        labels.add(NewsfeedLabels.subscribeRequest(0, List.of("foo")));

        // Responses
        labels.add(NewsfeedLabels.protocolResponse(0, "foo"));
        labels.add(NewsfeedLabels.availableTopics(List.of("foo")));
        labels.add(NewsfeedLabels.subscribeResponse(0, List.of("foo")));
        labels.add(NewsfeedLabels.heartbeat());
        labels.add(NewsfeedLabels.notify(0, false, "foo", "bar"));
        labels.add(NewsfeedLabels.newTopicAvailable("foo"));
        labels.add(NewsfeedLabels.fault());

        return labels;
    }

    public Value param(Label label, String name) {
        return label.getParametersList().stream()
                .filter(param -> param.getName().equals(name))
                .map(Label.Parameter::getValue)
                .findFirst().orElse(null);
    }

    public void sendReadyToAmp() {
        adapterCore.sendReady();
    }

    public void sendErrorToAmp(String message) {
        adapterCore.sendError(message);
    }
}
