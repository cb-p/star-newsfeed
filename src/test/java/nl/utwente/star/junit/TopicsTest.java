package nl.utwente.star.junit;

import nl.utwente.star.Config;
import nl.utwente.star.NewsfeedClient;
import nl.utwente.star.message.Message;
import nl.utwente.star.message.application.NewTopicAvailable;
import nl.utwente.star.message.application.SubscribeResponse;
import nl.utwente.star.message.client.ProtocolRequest;
import nl.utwente.star.message.client.SubscribeRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TopicsTest {


    private final Random random = new Random();
    static NewsfeedClient newsfeedClient;
    private int correlationId;

    @BeforeEach
    //sets up the client connection to the server
    public void setupClient() throws IOException {
        correlationId = random.nextInt();
        newsfeedClient = new NewsfeedClient();
        newsfeedClient.sendString("MANUFACTURER:" + Config.get("sut.manufacturer"));
    }

    @Test
    //TOPICS-01
    //check whether the five default topics are available in version 1.0
    public void testTopicList() throws IOException {
        //connect to version 1.0
        List<String> versions = List.of("1.0");
        Message protocolRequest = new ProtocolRequest(correlationId,versions);
        newsfeedClient.send(protocolRequest);
        newsfeedClient.waitAndReceive();
        //subscribe to the five default topics
        Message subscription = new SubscribeRequest(correlationId,List.of("general","breaking", "sport", "weather", "culture", "fdfdfdfd"));
        newsfeedClient.send(subscription);
        Message response = newsfeedClient.waitAndReceive();
        //check if all the topics are indeed available on the server.
        assertTrue(((SubscribeResponse) response).topics.contains("general"));
        assertTrue(((SubscribeResponse) response).topics.contains("breaking"));
        assertTrue(((SubscribeResponse) response).topics.contains("sport"));
        assertTrue(((SubscribeResponse) response).topics.contains("weather"));
        assertTrue(((SubscribeResponse) response).topics.contains("culture"));
        assertFalse(((SubscribeResponse) response).topics.contains("fdfdfdfd"));
    }

    @Test
    //TOPICS-03
    //Tests if at version 3 we receive a message about new available topics.
    public void testAdditionOfTopics() throws IOException {
        List<String> versions = List.of("3.0");
        Message protocolRequest = new ProtocolRequest(correlationId,versions);
        newsfeedClient.send(protocolRequest);
        newsfeedClient.waitAndReceive();
        //wait until we receive a NewTopicAvailable message
        Message message;
        do {
            message = newsfeedClient.waitAndReceive();
        } while (!(message instanceof NewTopicAvailable));
        //asserts whether the message contains a topic name.
        assertFalse(((NewTopicAvailable) message).topic.isEmpty());
    }
}
