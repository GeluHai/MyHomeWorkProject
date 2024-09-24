package com.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Utils {

    public static Properties properties = new Properties();

    static {
        try (InputStream input = new FileInputStream("src/test/resources/testdata.properties")) {
            properties.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new RuntimeException("Failed to load testdata.properties file");
        }
    }
}


