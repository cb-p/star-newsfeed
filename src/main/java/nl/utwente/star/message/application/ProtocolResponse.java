package nl.utwente.star.message.application;

import com.google.gson.annotations.SerializedName;
import nl.utwente.star.message.Message;

/// SESSION-03
public class ProtocolResponse extends Message.Response {
    @SerializedName("ProtocolVersion")
    public String protocolVersion;

    @Override
    public String toString() {
        return "ProtocolResponse{" +
                "protocolVersion='" + protocolVersion + '\'' +
                ", correlationId=" + correlationId +
                '}';
    }

    /// SESSION-04
    public boolean isRefused() {
        return protocolVersion.isEmpty();
    }
}
