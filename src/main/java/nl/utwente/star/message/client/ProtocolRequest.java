package nl.utwente.star.message.client;

import com.google.gson.annotations.SerializedName;
import nl.utwente.star.message.Message;

import java.util.Arrays;

/// SESSION-01
public class ProtocolRequest extends Message.Request {
    @SerializedName("ProtocolVersions")
    public String[] protocolVersions;

    public ProtocolRequest(int correlationId, String... protocolVersions) {
        super(correlationId);
        this.protocolVersions = protocolVersions;
    }

    @Override
    public String toString() {
        return "ProtocolRequest{" +
                "protocolVersions=" + Arrays.toString(protocolVersions) +
                ", correlationId=" + correlationId +
                '}';
    }
}
