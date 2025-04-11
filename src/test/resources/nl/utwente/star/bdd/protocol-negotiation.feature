Feature: Protocol Negotiation
  The NewsFeed server should correctly negotiate new connections

  # SESSION-02/SESSION-04
  Scenario:
    Given I am connected to the NewsFeed server
    When I request no protocol versions:
    And I wait 1 seconds
    Then the server selected protocol version ""
    When I request protocol versions:
      | 10.0 |
      | 2.0 |
      | 3.0 |
      | 1.0 |
    And I wait 1 seconds
    Then the server selected protocol version "2.0"
    When I request protocol versions:
      | 1.0 |
      | 3.0 |
      | 2.0 |
    And I wait 1 seconds
    Then the server selected protocol version "1.0"

  # TOPICS-02
  Scenario:
    Given I am connected to the NewsFeed server
    When I request protocol versions:
      | 2.0 |
    And I wait 1 seconds
    Then the server announced a list of available topics

  # SESSION-05
  Scenario:
    Given I am connected to the NewsFeed server
    When I request protocol versions:
     | 2.0 |
    And I wait 3 seconds
    Then the server selected protocol version "2.0"
    When I subscribe to topics:
      | general |
    And I wait 3 seconds
    And I request protocol versions:
     | 1.0 |
    And I wait 3 seconds
    Then the server selected protocol version "1.0"
    When I wait 5 seconds
    Then there should be no more notifications

