package com.adobe.primetime.adde;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WebServerApplication {
    private static final Logger LOG = LoggerFactory.getLogger(WebServerApplication.class);

    public static void main(String[] args) {
        //Setup engine
        LOG.info("Initializing Decision Engine...");
        DecisionEngine decisionEngine = DecisionEngine.getInstance();
        decisionEngine.setConfigurationFile("src/test/resources/configFile.json");
        decisionEngine.initializeEngine();
        LOG.info("Initialized.");

        // Start spring
        SpringApplication.run(WebServerApplication.class, args);
    }
}
