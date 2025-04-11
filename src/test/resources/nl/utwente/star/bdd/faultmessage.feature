Feature: Faultmessage


  # FAULT-01
  Scenario:
    Given I am connected to the NewsFeed server
    When I subscribe to topics:
      |sport|
    And I wait 1 seconds
    Then the server responds with a fault message

  # FAULT-01
  Scenario:
    Given I am connected to the NewsFeed server
    When I unsubscribe
    And I wait 1 seconds
    Then the server responds with a fault message

