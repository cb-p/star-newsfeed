package nl.utwente.star.bdd;

import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nl.utwente.star.NewsfeedClient;
import nl.utwente.star.message.Message;
import nl.utwente.star.message.application.*;
import nl.utwente.star.message.client.ProtocolRequest;
import nl.utwente.star.message.client.SubscribeRequest;
import nl.utwente.star.message.client.Unsubscribe;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class StepDefinitions {
    private static final String MANUFACTURER = "Inixa";

    private NewsfeedClient client;

    // These are null if the server hasn't sent us it.
    private String serverProtocolVersion = null;
    private List<String> availableTopics = null;
    private boolean faultMessageReceived = false;
    private int heartbeatCount = 0;

    // Notifications since subscribing.
    private final LinkedList<Notify> notificationsSinceSubscribing = new LinkedList<>();

    private void setupReceiveThread() {
        new Thread(() -> {
            while (true) {
                Message message = client.waitAndReceive();
                if (message == null) {
                    break;
                } else if (message instanceof ProtocolResponse response) {
                    serverProtocolVersion = response.protocolVersion;
                    notificationsSinceSubscribing.clear();
                } else if (message instanceof Fault) {
                    faultMessageReceived = true;
                }
                else if (message instanceof AvailableTopics response) {
                    availableTopics = response.topics;
                } else if (message instanceof SubscribeResponse) {
                    // FIXME: handle this response properly if needed.
                    notificationsSinceSubscribing.clear();
                    heartbeatCount = 0;
                } else if (message instanceof Notify response) {
                    if (!response.isHeartbeat()) {
                        notificationsSinceSubscribing.add(response);

                    } else {
                        heartbeatCount++;
                    }
                }
            }
        }).start();
    }

    @After
    public void after() throws Exception {
        if (client != null) {
            client.close();
        }
    }

    // -- STEP DEFINITIONS

    @Given("I am connected to the NewsFeed server")
    public void i_am_connected_to_the_NewsFeed_server() throws IOException {
        client = new NewsfeedClient();
        client.sendString("MANUFACTURER:" + MANUFACTURER);

        setupReceiveThread();
    }

    @Given("I successfully request protocol version {string}")
    public void I_successfully_request_protocol_version(String protocolVersion) throws IOException, InterruptedException {
        i_request_protocol_versions(List.of(protocolVersion));
        i_wait_n_seconds(1);
        the_server_selected_protocol_version(protocolVersion);
    }

    @When("I request protocol versions:")
    public void i_request_protocol_versions(List<String> versions) throws IOException {
        client.send(new ProtocolRequest(1, versions));
    }

    @When("I request no protocol versions:")
    public void i_request_protocol_versions() throws IOException {
        client.send(new ProtocolRequest(1, new ArrayList<>()));
    }

    @When("I wait {int} seconds")
    public void i_wait_n_seconds(int n) throws InterruptedException {
        Thread.sleep(n * 1000L);
    }

    @When("I subscribe to topics:")
    public void i_subscribe_to_topics(List<String> topics) throws IOException {
        client.send(new SubscribeRequest(2, topics));
    }

    @When("I unsubscribe")
    public void i_unsubscribe_to_topics() throws IOException {
        client.send(new Unsubscribe());
        notificationsSinceSubscribing.clear();
    }

    @Then("the server responds with a fault message")
    public void the_server_responds_with_fault_message() throws IOException {
        assertThat(faultMessageReceived).isTrue();
    }

    @Then("the server selected protocol version {string}")
    public void the_server_selected_protocol_version(String version) {
        assertThat(serverProtocolVersion).isNotNull().isEqualTo(version);
    }

    @Then("the server announced a list of available topics")
    public void theServerAnnouncedAListOfAvailableTopics() {
        assertThat(availableTopics).isNotNull();
    }

    @Then("all received notifications since subscribing should be of topic {string}")
    public void all_received_notifications_since_subscribing_should_be_of_topic(String topic) {
        for (Notify notification : notificationsSinceSubscribing) {
            assertThat(notification.topic).isEqualTo(topic);
        }

    }
    @Then("there should be no more notifications")
    public void there_should_be_no_more_notifications() {

        assertThat(notificationsSinceSubscribing.size()).isEqualTo(0);


    }

    @Then("we should have received {int} heartbeats")
    public void we_should_have_received_heartbeats(int num) {
        assertThat(heartbeatCount).isEqualTo(num);
    }

    @Then("the first message is a snapshot")
    public void the_first_message_is_a_snapshot() throws IOException {
        assertThat(notificationsSinceSubscribing.getFirst().isSnapshot()).isTrue();
    }


}
