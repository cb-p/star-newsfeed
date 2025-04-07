package nl.utwente.star.junit;

import nl.utwente.star.NewsfeedClient;
import nl.utwente.star.message.Message;
import nl.utwente.star.message.application.ProtocolResponse;
import nl.utwente.star.message.client.ProtocolRequest;
import nl.utwente.star.message.client.StopSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class ProtocolNegotiationTest {

    private final Random random = new Random();
    static NewsfeedClient newsfeedClient;
    private int correlationId;

    @BeforeEach
    //sets up the client connection to the server, but does not communicate protocol version.
    public void setupClient() throws IOException {
        correlationId = random.nextInt();
        newsfeedClient = new NewsfeedClient();
    }

    @Test
    //SESSION-03
    //Assert the response to a protocol request.
    public void testProtocolResponseAttributes() throws IOException {
        List<String> versions = List.of("1.0");
        Message protocolRequest = new ProtocolRequest(correlationId,versions);
        newsfeedClient.send(protocolRequest);
        Message message = newsfeedClient.waitAndReceive();
        //assert whether we received right type of response message
        assertTrue(message instanceof ProtocolResponse);
        //assert the contents of the response message
        assertEquals(correlationId,((ProtocolResponse) message).correlationId);
        assertEquals("1.0",((ProtocolResponse) message).protocolVersion);
    }

    @Test
    //SESSION-04
    //Test the server response to unsupported protocol version.
    public void testProtocolResponseInvalid() throws IOException {
        List<String> versions = List.of("test");
        Message protocolRequest = new ProtocolRequest(correlationId,versions);
        newsfeedClient.send(protocolRequest);
        Message message = newsfeedClient.waitAndReceive();
        //assert contents of the response
        assertTrue(message instanceof ProtocolResponse);
        assertEquals("",((ProtocolResponse) message).protocolVersion);
    }

    @Test
    //SESSION-06
    //Tests whether the stop message actually stops the newsfeed from sending messages.
    public void testProtocolEndSession() throws IOException, InterruptedException {
        List<String> versions = List.of("1.0");
        Message protocolRequest = new ProtocolRequest(correlationId,versions);
        newsfeedClient.send(protocolRequest);
        newsfeedClient.waitAndReceive();
        newsfeedClient.send(new StopSession());//ask server to stop
        //try receiving on another thread
        Thread receive = new Thread(){
            public void run(){
                newsfeedClient.waitAndReceive();
            }
        };
        receive.start();
        Thread.sleep(10000);//wait for 10 seconds to receive
        assertTrue(receive.isAlive());
    }
}
