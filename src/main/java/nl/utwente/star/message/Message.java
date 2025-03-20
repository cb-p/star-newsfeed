package nl.utwente.star.message;

import com.google.gson.annotations.SerializedName;

public abstract class Message {
    public static abstract class Request extends Message {
        @SerializedName("CorrelationId")
        public int correlationId;

        public Request(int correlationId) {
            this.correlationId = correlationId;
        }
    }

    public static abstract class Response extends Message {
        @SerializedName("CorrelationId")
        public int correlationId;
    }
}
