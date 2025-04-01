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
   Given I am connected to the NewsFeed server
       And I successfully request protocol version "2.0"
       When I subscribe to topics:
         | general |
       And I wait 10 seconds
       Then all received notifications since subscribing should be of topic "general"
