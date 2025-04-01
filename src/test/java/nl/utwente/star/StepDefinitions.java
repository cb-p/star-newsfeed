package nl.utwente.star;

import io.cucumber.java.After;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import nl.utwente.star.message.Message;
import nl.utwente.star.message.application.AvailableTopics;
import nl.utwente.star.message.application.Notify;
import nl.utwente.star.message.application.ProtocolResponse;
import nl.utwente.star.message.application.SubscribeResponse;
import nl.utwente.star.message.client.ProtocolRequest;
import nl.utwente.star.message.client.SubscribeRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class StepDefinitions {
    private static final String MANUFACTURER = "Axini";

    private NewsfeedClient client;

    // These are null if the server hasn't sent us it.
    private String serverProtocolVersion = null;
    private List<String> availableTopics = null;

    // Notifications since subscribing.
    private final List<Notify> notificationsSinceSubscribing = new ArrayList<>();

    private void setupReceiveThread() {
        new Thread(() -> {
            while (true) {
                Message message = client.waitAndReceive();
                if (message == null) {
                    break;
                } else if (message instanceof ProtocolResponse response) {
                    serverProtocolVersion = response.protocolVersion;
                } else if (message instanceof AvailableTopics response) {
                    availableTopics = response.topics;
                } else if (message instanceof SubscribeResponse) {
                    // FIXME: handle this response properly if needed.
                    notificationsSinceSubscribing.clear();
                } else if (message instanceof Notify response) {
                    if (!response.isHeartbeat()) {
                        notificationsSinceSubscribing.add(response);
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

    @When("I wait {int} seconds")
    public void i_wait_n_seconds(int n) throws InterruptedException {
        Thread.sleep(n * 1000L);
    }

    @When("I subscribe to topics:")
    public void i_subscribe_to_topics(List<String> topics) throws IOException {
        client.send(new SubscribeRequest(2, topics));
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
}
