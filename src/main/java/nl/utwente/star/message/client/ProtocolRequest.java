package nl.utwente.star.message.client;

import com.google.gson.annotations.SerializedName;
import nl.utwente.star.message.Message;

import java.util.List;

/// SESSION-01
public class ProtocolRequest extends Message.Request {
    @SerializedName("ProtocolVersions")
    public List<String> protocolVersions;

    public ProtocolRequest(int correlationId, List<String> protocolVersions) {
        super(correlationId);
        this.protocolVersions = protocolVersions;
    }

    @Override
    public String toString() {
        return "ProtocolRequest{" +
                "protocolVersions=" + protocolVersions +
                ", correlationId=" + correlationId +
                '}';
    }
}
