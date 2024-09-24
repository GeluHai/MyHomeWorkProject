package com.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class JsonReader {

    public List<JsonObject> readJsonFile(String filePath) throws IOException {
        List<JsonObject> jsonObjects = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                JsonObject jsonObject = JsonParser.parseString(line).getAsJsonObject();
                jsonObjects.add(jsonObject);
            }
        }
        return jsonObjects;
    }
}


