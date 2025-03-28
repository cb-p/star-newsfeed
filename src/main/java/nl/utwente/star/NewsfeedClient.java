package nl.utwente.star;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import nl.utwente.star.message.Message;
import nl.utwente.star.message.application.*;
import nl.utwente.star.message.client.ProtocolRequest;
import nl.utwente.star.message.client.SubscribeRequest;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class NewsfeedClient implements AutoCloseable {
    private static final Gson GSON = new GsonBuilder().create();

    private final Socket socket;
    private final OutputStream out;
    private final Scanner scanner;

    public NewsfeedClient() throws IOException {
        socket = new Socket("localhost", 2000);
        out = socket.getOutputStream();

        scanner = new Scanner(new InputStreamReader(socket.getInputStream()));
        scanner.useDelimiter("<!>");
    }

    public void send(Message message) throws IOException {
        System.out.println("-> " + message);
        String json = GSON.toJson(new RawMessage(message));
        sendString(json);
    }

    public void sendString(String message) throws IOException {
        out.write((message + "<!>").getBytes(StandardCharsets.UTF_8));
    }

    public Message receive() {
        if (scanner.hasNext()) {
            RawMessage raw = GSON.fromJson(scanner.next(), RawMessage.class);
            Message message = raw.parse();
            System.out.println("<- " + message);
            return message;
        } else {
            throw new RuntimeException("no more messages.");
        }
    }

    @Override
    public void close() throws Exception {
        socket.close();
    }

    /// See README of the newsfeed server.
    public static class RawMessage {
        public String name;
        public JsonElement parameters;

        @SuppressWarnings("unused")
        private RawMessage() {
        }

        public RawMessage(Message message) {
            this.name = message.getClass().getSimpleName();
            this.parameters = GSON.toJsonTree(message);
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

    public static void main(String[] args) throws Exception {
//        Axini
//        console -> sequence numbers always 0
//        Logica -> doesn't send available topics on 2.0 OR 3.0
//        e-news
//        NewsWire -> takes highest protocol version instead of preference
//        OnTarget
//        SmartSoft -> send all available messages (subscription response is correct)
//        TestGroup
//        TrustedTechnologies
//        univerSolutions -> subscription responds with all available topics
//        Inixa

        try (NewsfeedClient client = new NewsfeedClient()) {
            // set manufacturer
            String manufacturer = "Axini";
            System.out.println("Manufacturer: " + manufacturer);
            client.sendString("MANUFACTURER:" + manufacturer);

            // request protocol
            client.send(new ProtocolRequest(1, "3.0", "2.0", "1.0"));
            Message response = client.receive();

            // (receive available topics)
            // noinspection ConstantValue
            if (response instanceof ProtocolResponse pr
                    && !pr.protocolVersion.equals("1.0")
                    && !manufacturer.equals("Logica") && !manufacturer.equals("Inixa")) {
                client.receive();
            }

            // subscribe
            // topics: ["general","breaking","sport","weather","culture","europe","funny"]
            client.send(new SubscribeRequest(2, "general"));
            client.receive();

            // receive all messages after
            while (true) {
                client.receive();
            }
        }
    }
}
