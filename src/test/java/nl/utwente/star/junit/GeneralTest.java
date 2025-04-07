package nl.utwente.star.junit;

import nl.utwente.star.NewsfeedClient;
import nl.utwente.star.message.Message;
import nl.utwente.star.message.application.SubscribeResponse;
import nl.utwente.star.message.client.ProtocolRequest;
import nl.utwente.star.message.client.SubscribeRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class GeneralTest {

    private final Random random = new Random();
    static NewsfeedClient newsfeedClient;
    private int correlationId;

    @BeforeEach
    public void setupClient() throws IOException {
        correlationId = random.nextInt();
        newsfeedClient = new NewsfeedClient();
    }

    @Test
    // RESP-TIME-01
    // Asserts whether the server responds within one second.
    public void testTiming() throws IOException {
        List<String> versions = List.of("1.0");
        Message protocolRequest = new ProtocolRequest(correlationId,versions);
        long time = System.currentTimeMillis();//set timer
        newsfeedClient.send(protocolRequest);
        newsfeedClient.waitAndReceive();
        assertTrue((System.currentTimeMillis() - time) < 1000);//check if received response within one second
    }

    @Test
    //CASE-01 (+ SUBSCR-03)
    //asserts whether topics are case-sensitive.
    public void testCaseSensitivity() throws IOException {
        List<String> versions = List.of("1.0");
        Message protocolRequest = new ProtocolRequest(correlationId,versions);
        newsfeedClient.send(protocolRequest);
        newsfeedClient.waitAndReceive();
        //subscribe to a topic that would exist if the SUT is not case-sensitive.
        Message subscription = new SubscribeRequest(correlationId,List.of("GENERAL"));
        newsfeedClient.send(subscription);
        Message response = newsfeedClient.waitAndReceive();
        assertTrue(response instanceof SubscribeResponse);
        assertTrue(((SubscribeResponse) response).topics.isEmpty());//no topics should be subscribed
        //and now test the subscription for the topic in the right uper/lower case.
        subscription = new SubscribeRequest(correlationId,List.of("general"));
        newsfeedClient.send(subscription);
        response = newsfeedClient.waitAndReceive();
        assertTrue(response instanceof SubscribeResponse);
        assertTrue(((SubscribeResponse) response).topics.contains("general"));
    }
}