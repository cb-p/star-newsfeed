Feature: Protocol Negotiation
  The NewsFeed server should correctly negotiate new connections

  # SESSION-02
  Scenario:
    Given I am connected to the NewsFeed server
    When I request protocol versions:
      | 2.0 |
      | 3.0 |
      | 1.0 |
    And I wait 1 seconds
    Then the server selected protocol version "2.0"

  # TOPICS-02
  Scenario:
    Given I am connected to the NewsFeed server
    When I request protocol versions:
      | 2.0 |
    And I wait 1 seconds
    Then the server announced a list of available topics