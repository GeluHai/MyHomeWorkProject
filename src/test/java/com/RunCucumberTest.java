package com;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(
        stepNotifications = true,
        features = "src/test/java/com/features",
        glue = "com.steps",
        plugin = {"pretty"},

        tags = "@homeWork"

)
public class RunCucumberTest {
}


