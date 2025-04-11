Feature: Subscribing
  The NewsFeed server should correctly subscribe to topics

  # NOTIFY-03
  Scenario:
    Given I am connected to the NewsFeed server
    And I successfully request protocol version "2.0"
    When I subscribe to topics:
      | general |
    And I wait 10 seconds
    Then all received notifications since subscribing should be of topic "general"

   # SUBSCR-05
   Scenario:
    Given I am connected to the NewsFeed server
    And I successfully request protocol version "2.0"
    When I subscribe to topics:
      | general |
    And I wait 10 seconds
    Then all received notifications since subscribing should be of topic "general"
    When I subscribe to topics:
      | sport |
    And I wait 10 seconds
    Then all received notifications since subscribing should be of topic "sport"

   # SUBSCR-06
   Scenario:
     Given I am connected to the NewsFeed server
       And I successfully request protocol version "2.0"
       When I subscribe to topics:
         | general |
       And I wait 5 seconds
       And I unsubscribe
       And I wait 10 seconds
       Then there should be no more notifications

  # SUBSCR-04
  Scenario:
    Given I am connected to the NewsFeed server
    And I successfully request protocol version "2.0"
    When I subscribe to topics:
      | empty |
    And I wait 3 seconds
    Then we should have received 1 heartbeats


  # SNAPSHOT-01
  Scenario:
  Given I am connected to the NewsFeed server
    And I successfully request protocol version "2.0"
    And I wait 15 seconds
    And I subscribe to topics:
    | general |
    | sport |
    | breaking |
    | weather |
    | culture |
    And I wait 3 seconds
    Then the first message is a snapshot