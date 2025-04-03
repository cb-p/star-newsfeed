package nl.utwente.star.junit;

import nl.utwente.star.NewsfeedClient;
import nl.utwente.star.message.Message;
import nl.utwente.star.message.client.ProtocolRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class GeneralTest {

    static NewsfeedClient newsfeedClient;
    private static final int correlationId = 1;

    @BeforeAll
    public static void setupClient() throws IOException {
        newsfeedClient = new NewsfeedClient();
    }

    @Test
    // RESP-TIME-01
    public void testTiming() throws IOException {
        List<String> versions = List.of("1.0");
        Message protocolRequest = new ProtocolRequest(correlationId,versions);
        long time = System.currentTimeMillis();
        newsfeedClient.send(protocolRequest);
        newsfeedClient.waitAndReceive();
        assertTrue((System.currentTimeMillis() - time) < 1000);
    }
}