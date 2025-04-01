package nl.utwente.star.amp;

import PluginAdapter.Api.LabelOuterClass.Label;
import PluginAdapter.Api.LabelOuterClass.Label.Parameter.Value;
import com.axini.adapter.generic.AxiniProtobuf;

import java.util.List;

public class NewsfeedLabels {
    private static Value stringArrayValue(List<String> strings) {
        return Value.newBuilder()
                .setArray(Value.Array.newBuilder()
                        .addAllValues(strings.stream().map(AxiniProtobuf::createStringValue).toList())
                        .build())
                .build();
    }

    // -- STIMULI

    public static Label protocolRequest(int correlationId, List<String> protocolVersions) {
        return AxiniProtobuf.createLabel("protocol_request", "newsfeed",
                Label.LabelType.STIMULUS,
                List.of(
                        AxiniProtobuf.createParameter("correlation_id", AxiniProtobuf.createIntValue(correlationId)),
                        AxiniProtobuf.createParameter("protocol_versions", stringArrayValue(protocolVersions))
                ));
    }

    public static Label stopSession() {
        return AxiniProtobuf.createLabel("stop_session", "newsfeed",
                Label.LabelType.STIMULUS, List.of());
    }

    public static Label subscribeRequest(int correlationId, List<String> topics) {
        return AxiniProtobuf.createLabel("subscribe_request", "newsfeed",
                Label.LabelType.STIMULUS,
                List.of(
                        AxiniProtobuf.createParameter("correlation_id", AxiniProtobuf.createIntValue(correlationId)),
                        AxiniProtobuf.createParameter("topics", stringArrayValue(topics))
                ));
    }

    public static Label unsubscribe() {
        return AxiniProtobuf.createLabel("unsubscribe", "newsfeed",
                Label.LabelType.STIMULUS, List.of());
    }

    // -- RESPONSES

    public static Label protocolResponse(int correlationId, String protocolVersion) {
        return AxiniProtobuf.createLabel("protocol_response", "newsfeed",
                Label.LabelType.RESPONSE,
                List.of(
                        AxiniProtobuf.createParameter("correlation_id", AxiniProtobuf.createIntValue(correlationId)),
                        AxiniProtobuf.createParameter("protocol_version", AxiniProtobuf.createStringValue(protocolVersion))
                ));
    }

    public static Label availableTopics(List<String> topics) {
        return AxiniProtobuf.createLabel("available_topics", "newsfeed",
                Label.LabelType.RESPONSE,
                List.of(
                        AxiniProtobuf.createParameter("topics", stringArrayValue(topics))
                ));
    }

    public static Label subscribeResponse(int correlationId, List<String> topics) {
        return AxiniProtobuf.createLabel("subscribe_response", "newsfeed",
                Label.LabelType.RESPONSE,
                List.of(
                        AxiniProtobuf.createParameter("correlation_id", AxiniProtobuf.createIntValue(correlationId)),
                        AxiniProtobuf.createParameter("topics", stringArrayValue(topics))
                ));
    }

    public static Label heartbeat() {
        return AxiniProtobuf.createLabel("heartbeat", "newsfeed",
                Label.LabelType.RESPONSE, List.of());
    }

    public static Label notify(int sequenceNumber, boolean isSnapshot, String topic, String content) {
        return AxiniProtobuf.createLabel("notify", "newsfeed",
                Label.LabelType.RESPONSE,
                List.of(
                        AxiniProtobuf.createParameter("sequence_number", AxiniProtobuf.createIntValue(sequenceNumber)),
                        AxiniProtobuf.createParameter("is_snapshot", AxiniProtobuf.createBooleanValue(isSnapshot)),
                        AxiniProtobuf.createParameter("topic", AxiniProtobuf.createStringValue(topic)),
                        AxiniProtobuf.createParameter("content", AxiniProtobuf.createStringValue(content))
                ));
    }

    public static Label newTopicAvailable(String topic) {
        return AxiniProtobuf.createLabel("new_topic_available", "newsfeed",
                Label.LabelType.RESPONSE,
                List.of(
                        AxiniProtobuf.createParameter("topic", AxiniProtobuf.createStringValue(topic))
                ));
    }

    public static Label fault() {
        return AxiniProtobuf.createLabel("fault", "newsfeed",
                Label.LabelType.RESPONSE, List.of());
    }
}
