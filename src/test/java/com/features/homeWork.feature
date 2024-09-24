@homeWork


Feature: These are the tests that cover the Homework exercises


  @homeWork3
  Scenario Outline: Calculate and display the most listened-to podcast from <city>
  and the number of downloads associated to the podcast
    Given the file from preconditions
    Then calculate the most listened-to podcast from "<city>" city

    Examples:
      | city          |
      | san francisco |


  @homeWork4
  Scenario: Calculate and display the most used device to listen to podcast from
  and the number of downloads associated to the device
    Given the file from preconditions
    Then calculate the most used device to listen to podcast


  @homeWork5
  Scenario: Calculate and display how many opportunities to insert an ad in the preroll there were for every podcast show
    Given the file from preconditions
    Then calculate how many opportunities to insert an ad in the preroll there are for each podcast show


  @homeWork6
  Scenario: Calculate and display the weekly podcasts and the time they air
    Given the file from preconditions
    Then display the weekly podcasts and the time they air


