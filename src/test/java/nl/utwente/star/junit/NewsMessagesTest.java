package nl.utwente.star.junit;

import nl.utwente.star.NewsfeedClient;
import nl.utwente.star.message.Message;
import nl.utwente.star.message.application.Notify;
import nl.utwente.star.message.client.ProtocolRequest;
import nl.utwente.star.message.client.SubscribeRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class NewsMessagesTest {

    static NewsfeedClient newsfeedClient;
    private static final int correlationId = 1;

    @BeforeAll
    public static void setupClient() throws IOException {
        newsfeedClient = new NewsfeedClient();
        List<String> versions = List.of("1.0");
        Message protocolRequest = new ProtocolRequest(correlationId,versions);
        newsfeedClient.send(protocolRequest);
        newsfeedClient.waitAndReceive();
    }

    @Test
    //NOTIFY-02
    public void testNotifyAttributes() throws IOException {
        Message subscription = new SubscribeRequest(correlationId,List.of("general"));
        newsfeedClient.send(subscription);
        newsfeedClient.waitAndReceive();
        Message response = newsfeedClient.waitAndReceive();
        assertTrue(response instanceof Notify);
        assertEquals(0,((Notify) response).sequenceNumber);
        response = newsfeedClient.waitAndReceive();
        assertTrue(response instanceof Notify);
        assertEquals(1,((Notify) response).sequenceNumber);
        for (int i = 1; i < 255; i++) {
            newsfeedClient.waitAndReceive();
        }
        assertEquals(0,((Notify) response).sequenceNumber);
        subscription = new SubscribeRequest(correlationId,List.of("culture"));
        newsfeedClient.send(subscription);
        newsfeedClient.waitAndReceive();
        Notify notification;

        do {
            notification = (Notify) newsfeedClient.waitAndReceive();
        } while (!Objects.equals(notification.topic, "culture"));

        assertEquals(0,notification.sequenceNumber);
    }

    @Test
    // HEARTBEAT-01
    public void testHeartbeat() throws IOException {
        Message subscription = new SubscribeRequest(correlationId,List.of("culture"));
        newsfeedClient.send(subscription);
        newsfeedClient.waitAndReceive();
        long time = System.currentTimeMillis();
        while (!((Notify) newsfeedClient.waitAndReceive()).isHeartbeat()) {
            assertTrue((System.currentTimeMillis() - time) < 3000);
            time = System.currentTimeMillis();
        }
        time = System.currentTimeMillis() - time;
        System.out.println("Heartbeat time: " + time);
        assertTrue(time >= 2990);
    }

    @Test
    // HAERTBEAT-02
    public void testHeartbeatAttributes() throws IOException {
        Message subscription = new SubscribeRequest(correlationId,List.of("empty"));
        newsfeedClient.send(subscription);
        newsfeedClient.waitAndReceive();
        Message message = newsfeedClient.waitAndReceive();
        assertTrue(message instanceof Notify);
        assertTrue(((Notify) message).isHeartbeat());
        assertFalse(((Notify) message).isSnapshot);
        assertEquals("",((Notify) message).content);
        assertEquals("",((Notify) message).topic);
    }
}
