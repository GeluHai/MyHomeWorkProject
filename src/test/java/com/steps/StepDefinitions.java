package com.steps;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.utils.JsonReader;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.utils.Utils.properties;
import static org.junit.Assert.*;


public class StepDefinitions {

    public static List<JsonObject> jsonObjectsFromFile;


    @Before
    public void setUp() {
        if (jsonObjectsFromFile == null) {  // Load data only if it hasn't been loaded yet
            jsonObjectsFromFile = new ArrayList<>();
            String filePath = properties.getProperty("downloads.file.path");

            JsonReader jsonReader = new JsonReader();
            try {
                jsonObjectsFromFile = jsonReader.readJsonFile(filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }

            assertNotNull("File path should not be null", filePath);
            assertFalse("Objects should be loaded", jsonObjectsFromFile.isEmpty());
        }
    }


    @Given("the file from preconditions")
    public void theFileFromPreconditions() {
        //this is a placeholder
        //the file is read at the beginning and is used for the rest of the tests
    }

    @Then("calculate the most listened-to podcast from {string} city")
    public void calculateTheMostListenedToPodcastFrom(String cityName) {
        String fieldName = "city";
        String showId = "downloadIdentifier.showId";

        List<JsonObject> matchingEntries = collectEntriesWithFieldValueMatch(jsonObjectsFromFile, fieldName, cityName);

        findMostFrequentFieldValue(matchingEntries, showId);
    }

    @Then("calculate the most used device to listen to podcast")
    public void calculateTheMostUsedDeviceToListenToPodcast() {
        String deviceType = "deviceType";

        findMostFrequentFieldValue(jsonObjectsFromFile, deviceType);
    }

    @Then("calculate how many opportunities to insert an ad in the preroll there are for each podcast show")
    public void calculateHowManyOpportunitiesToInsertAnAdInThePrerollThereAreForEachPodcastShow() {
        Map<String, Integer> prerollCounts = countPrerollAppearancesByShowId(jsonObjectsFromFile);

        for (Map.Entry<String, Integer> entry : prerollCounts.entrySet()) {
            System.out.println("Show Id: " + entry.getKey() + ", Preroll Opportunity Number: " + entry.getValue());
        }
    }

    @Then("display the weekly podcasts and the time they air")
    public void displayTheWeeklyPodcastsAndTheTimeTheyAir() {
        System.out.println("Weekly shows are: \n");
        findWeeklyShows(jsonObjectsFromFile);
    }


    //This section contains the methods used the steps above//


    public static List<JsonObject> collectEntriesWithFieldValueMatch(List<JsonObject> jsonObjects, String fieldName, String fieldValue) {
        List<JsonObject> matchingObjects = new ArrayList<>();

        for (JsonObject jsonObject : jsonObjects) {
            JsonElement fieldElement = getNestedField(jsonObject, fieldName);

            if (fieldElement != null && fieldElement.isJsonPrimitive() && fieldElement.getAsString().equals(fieldValue)) {
                matchingObjects.add(jsonObject);
            }
        }
        return matchingObjects;
    }

    public static void findMostFrequentFieldValue(List<JsonObject> jsonObjects, String fieldName) {
        Map<String, Integer> valueCounts = new HashMap<>();

        for (JsonObject jsonObject : jsonObjects) {
            JsonElement fieldElement = getNestedField(jsonObject, fieldName);

            if (fieldElement != null) {
                // Check if the field is an array or a single element
                if (fieldElement.isJsonArray()) {
                    // Iterate over the array and count occurrences of each value
                    for (JsonElement element : fieldElement.getAsJsonArray()) {
                        String value = element.getAsString();
                        valueCounts.put(value, valueCounts.getOrDefault(value, 0) + 1);
                    }
                } else {
                    // If it's a single value, count its occurrence
                    String value = fieldElement.getAsString();
                    valueCounts.put(value, valueCounts.getOrDefault(value, 0) + 1);
                }
            }
        }

        // Find the value with the highest count
        String mostFrequentValue = null;
        int maxCount = 0;
        for (Map.Entry<String, Integer> entry : valueCounts.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mostFrequentValue = entry.getKey();
            }
        }

        if (mostFrequentValue != null) {
            switch (fieldName) {
                case "downloadIdentifier.showId":
                    System.out.println("Most popular show is: " + mostFrequentValue);
                    System.out.println("Number of downloads is: " + maxCount);

                    assertEquals("The most popular show is not correct!", "Who Trolled Amber", mostFrequentValue);
                    assertEquals("The number of downloads is not correct!", 24, maxCount);
                    break;
                case "deviceType":
                    System.out.println("Most popular device is: " + mostFrequentValue);
                    System.out.println("Number of downloads is: " + maxCount);

                    assertEquals("The most popular device is not correct!", "mobiles & tablets", mostFrequentValue);
                    assertEquals("The number of downloads is not correct!", 60, maxCount);
                    break;
                default:
                    System.out.println("No case found for this field");
                    break;
            }
        }
    }

    public static Map<String, Integer> countPrerollAppearancesByShowId(List<JsonObject> jsonObjects) {
        Map<String, Integer> prerollCountByShowId = new HashMap<>();

        for (JsonObject jsonObject : jsonObjects) {
            if (jsonObject.has("downloadIdentifier")) {
                JsonObject downloadIdentifier = jsonObject.getAsJsonObject("downloadIdentifier");
                if (downloadIdentifier.has("showId")) {
                    String showId = downloadIdentifier.get("showId").getAsString();

                    if (jsonObject.has("opportunities")) {
                        JsonArray opportunities = jsonObject.getAsJsonArray("opportunities");

                        for (JsonElement opportunityElement : opportunities) {
                            if (opportunityElement.isJsonObject()) {
                                JsonObject opportunity = opportunityElement.getAsJsonObject();

                                if (opportunity.has("positionUrlSegments")) {
                                    JsonObject positionUrlSegments = opportunity.getAsJsonObject("positionUrlSegments");

                                    for (Map.Entry<String, JsonElement> entry : positionUrlSegments.entrySet()) {
                                        JsonElement adBreakArrayElement = entry.getValue();

                                        if (adBreakArrayElement.isJsonArray()) {
                                            JsonArray adBreakArray = adBreakArrayElement.getAsJsonArray();

                                            for (JsonElement element : adBreakArray) {
                                                if (element.isJsonPrimitive() && element.getAsString().equals("preroll")) {
                                                    prerollCountByShowId.put(showId, prerollCountByShowId.getOrDefault(showId, 0) + 1);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return sortByValueDescending(prerollCountByShowId);
    }

    private static Map<String, Integer> sortByValueDescending(Map<String, Integer> map) {
        List<Map.Entry<String, Integer>> list = new ArrayList<>(map.entrySet());

        list.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

        Map<String, Integer> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        assertEquals("The preroll opportunity number is not correct!", Optional.of(40).get(), sortedMap.get("Stuff You Should Know"));
        assertEquals("The preroll opportunity number is not correct!", Optional.of(40).get(), sortedMap.get("Who Trolled Amber"));
        assertEquals("The preroll opportunity number is not correct!", Optional.of(30).get(), sortedMap.get("Crime Junkie"));
        assertEquals("The preroll opportunity number is not correct!", Optional.of(10).get(), sortedMap.get("The Joe Rogan Experience"));

        return sortedMap;
    }

    public static void findWeeklyShows(List<JsonObject> jsonObjects) {
        Map<String, List<LocalDateTime>> showTimesMap = new HashMap<>();

        for (JsonObject jsonObject : jsonObjects) {
            String showId = jsonObject.getAsJsonObject("downloadIdentifier").get("showId").getAsString();
            JsonArray opportunities = jsonObject.getAsJsonArray("opportunities");

            // Extract originalEventTime from each opportunity (assuming all have the same originalEventTime per episode)
            if (!opportunities.isEmpty()) {
                long eventTimeMillis = opportunities.get(0).getAsJsonObject().get("originalEventTime").getAsLong();
                LocalDateTime eventTime = Instant.ofEpochMilli(eventTimeMillis).atZone(ZoneOffset.UTC).toLocalDateTime();

                // Add eventTime to the list for the corresponding show
                showTimesMap.computeIfAbsent(showId, k -> new ArrayList<>()).add(eventTime);
            }
        }

        // Check for weekly shows and print the output
        for (Map.Entry<String, List<LocalDateTime>> entry : showTimesMap.entrySet()) {
            String showId = entry.getKey();
            List<LocalDateTime> eventTimes = entry.getValue();

            // Check if the show has weekly broadcasts
            if (isWeekly(eventTimes)) {
                // Format and print the day of the week and time
                LocalDateTime firstEventTime = eventTimes.get(0);
                String dayOfWeek = firstEventTime.format(DateTimeFormatter.ofPattern("EEE"));
                String time = firstEventTime.format(DateTimeFormatter.ofPattern("HH:mm"));
                System.out.println(showId + " - " + dayOfWeek + " " + time);

                if (showId.equals("Crime Junkie")) {
                    assertEquals("Wed 22:00", dayOfWeek + " " + time);
                }
                if (showId.equals("Who Trolled Amber")) {
                    assertEquals("Mon 20:00", dayOfWeek + " " + time);
                }
            }
        }
    }

    // Helper method to check if all times are weekly (same day and time)
    private static boolean isWeekly(List<LocalDateTime> eventTimes) {
        if (eventTimes.size() < 2) {
            return false; // Less than 2 events can't establish a weekly pattern
        }

        LocalDateTime firstTime = eventTimes.get(0);
        for (int i = 1; i < eventTimes.size(); i++) {
            LocalDateTime nextTime = eventTimes.get(i);

            // Check if the day of week and time of day are the same
            if (firstTime.getDayOfWeek() != nextTime.getDayOfWeek() ||
                    firstTime.getHour() != nextTime.getHour() ||
                    firstTime.getMinute() != nextTime.getMinute()) {
                return false;
            }
        }
        return true; // All events have the same day and time
    }

    // Helper method to get a nested field from a JsonObject, using dot notation for field names
    public static JsonElement getNestedField(JsonObject jsonObject, String fieldName) {
        String[] fieldParts = fieldName.split("\\.");  // Split the field name by dots
        JsonElement currentElement = jsonObject;

        // Traverse through the nested fields
        for (String part : fieldParts) {
            if (currentElement.isJsonObject()) {
                currentElement = currentElement.getAsJsonObject().get(part);
            } else {
                return null;  // Return null if the structure does not match
            }

            if (currentElement == null) {
                return null;  // Return null if any part of the nested field is missing
            }
        }

        return currentElement;  // Return the final nested field element
    }
}