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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SubscriptionsTest {

    private final Random random = new Random();
    static NewsfeedClient newsfeedClient;
    private int correlationId;

    @BeforeEach
    //sets up the client connection to the server
    public void setupClient() throws IOException {
        correlationId = random.nextInt();
        newsfeedClient = new NewsfeedClient();
        List<String> versions = List.of("1.0");
        Message protocolRequest = new ProtocolRequest(correlationId,versions);
        newsfeedClient.send(protocolRequest);
        newsfeedClient.waitAndReceive();
    }

    @Test
    //SUBSCR-01
    //Asserts the format and contents of the subscribe response.
    public void testSubscribeResponse() throws IOException {
        //subscribe to topic
        Message subscription = new SubscribeRequest(correlationId,List.of("general"));
        newsfeedClient.send(subscription);
        //receive and assert subscribeResponse message
        Message response = newsfeedClient.waitAndReceive();
        assertTrue(response instanceof SubscribeResponse);//check response message type
        assertTrue(((SubscribeResponse) response).topics.contains("general"));//check values
        assertEquals(correlationId,((SubscribeResponse) response).correlationId);
    }

    @Test
    //SUBSCR-03
    //Tests to see if unavailable subscriptions are ignored.
    public void testSubscription() throws IOException {
        //subscribe to an unavailable topic.
        Message subscription = new SubscribeRequest(correlationId,List.of("fhdaifhdafhdkhf"));
        newsfeedClient.send(subscription);
        Message response = newsfeedClient.waitAndReceive();
        //topic should be ignored
        assertTrue(((SubscribeResponse) response).topics.isEmpty());
        //subscribe to an available topic.
        subscription = new SubscribeRequest(correlationId,List.of("general"));
        newsfeedClient.send(subscription);
        response = newsfeedClient.waitAndReceive();
        //topic should be added to subscribe response.
        assertTrue(((SubscribeResponse) response).topics.contains("general"));
    }
}
