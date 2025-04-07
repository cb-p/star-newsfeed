package nl.utwente.star.junit;

import nl.utwente.star.NewsfeedClient;
import nl.utwente.star.message.Message;
import nl.utwente.star.message.application.Notify;
import nl.utwente.star.message.client.ProtocolRequest;
import nl.utwente.star.message.client.SubscribeRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class NewsMessagesTest {

    private final Random random = new Random();
    static NewsfeedClient newsfeedClient;
    private int correlationId;

    //setup connection with the server
    @BeforeEach
    public void setupClient() throws IOException {
        correlationId = random.nextInt();
        newsfeedClient = new NewsfeedClient();
        List<String> versions = List.of("1.0");
        Message protocolRequest = new ProtocolRequest(correlationId,versions);
        newsfeedClient.send(protocolRequest);
        newsfeedClient.waitAndReceive();
    }

    @Test
    //NOTIFY-02
    //Tests whether the first notify message starts with sequence number 0
    public void testNotifySequenceStart() throws IOException {
        //subscribe to topic so we are receive messages
        Message subscription = new SubscribeRequest(correlationId,List.of("general"));
        newsfeedClient.send(subscription);
        newsfeedClient.waitAndReceive();
        //await first response and do the assertions
        Message response = newsfeedClient.waitAndReceive();
        assertTrue(response instanceof Notify);
        assertEquals(0,((Notify) response).sequenceNumber);
    }

    @Test
    //NOTIFY-02
    //Tests whether the sequence number of notify messages is incremented
    public void testNotifyIncrement() throws IOException {
        //subscribe to topic so we are receive messages
        Message subscription = new SubscribeRequest(correlationId,List.of("general"));
        newsfeedClient.send(subscription);
        newsfeedClient.waitAndReceive();
        Message response1 = newsfeedClient.waitAndReceive();//await additional message to increment sequence
        Message response2 = newsfeedClient.waitAndReceive();
        assertTrue(response1 instanceof Notify);
        assertTrue(response2 instanceof Notify);
        assertEquals(((Notify) response1).sequenceNumber + 1,((Notify) response2).sequenceNumber);
    }

    @Test
    //NOTIFY-02
    //Tests whether the sequence number is reset after switching topic.
    public void testNotifyDistinguish() throws IOException {
        //subscribe to topic so we are receive messages
        Message subscription = new SubscribeRequest(correlationId,List.of("general"));
        newsfeedClient.send(subscription);
        newsfeedClient.waitAndReceive();
        newsfeedClient.waitAndReceive();
        //change subscription topic
        subscription = new SubscribeRequest(correlationId,List.of("culture"));
        newsfeedClient.send(subscription);
        newsfeedClient.waitAndReceive();
        //wait until we receive or first message from the new topic
        Notify notification;
        do {
            notification = (Notify) newsfeedClient.waitAndReceive();
        } while (!Objects.equals(notification.topic, "culture"));
        //assert sequence number
        assertEquals(0,notification.sequenceNumber);
    }

    @Test
    // HEARTBEAT-01
    //Tests if a heartbeat message is sent within NotifyTime time if no other message is sent.
    public void testHeartbeat() throws IOException {
        //subscribe to topic so we are receive messages
        Message subscription = new SubscribeRequest(correlationId,List.of("culture"));
        newsfeedClient.send(subscription);
        newsfeedClient.waitAndReceive();
        //Take note of the system time and wait until a heartbeat is received.
        long time = System.currentTimeMillis();
        while (!((Notify) newsfeedClient.waitAndReceive()).isHeartbeat()) {
            assertTrue((System.currentTimeMillis() - time) < 3000);//assert whether there should have been a heartbeat
            time = System.currentTimeMillis();//reset timer
        }
        time = System.currentTimeMillis() - time;
        System.out.println("Heartbeat time: " + time);
        assertTrue(time >= 2990);//there small range for processing delay, precision should not matter that much here
    }

    @Test
    // HEARTBEAT-02
    //Tests the content of the heartbeat message.
    public void testHeartbeatAttributes() throws IOException {
        //subscribe to topic so we are receive messages
        Message subscription = new SubscribeRequest(correlationId,List.of("empty"));
        newsfeedClient.send(subscription);
        newsfeedClient.waitAndReceive();
        //we should receive a heartbeat since the topic is empty, then check the contents of the heartbeat
        Message message = newsfeedClient.waitAndReceive();
        assertTrue(message instanceof Notify);
        assertTrue(((Notify) message).isHeartbeat());
        assertFalse(((Notify) message).isSnapshot);
        assertEquals("",((Notify) message).content);
        assertEquals("",((Notify) message).topic);
    }
}
