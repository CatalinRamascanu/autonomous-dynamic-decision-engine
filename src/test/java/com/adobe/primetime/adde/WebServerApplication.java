package com.adobe.primetime.adde;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebServerApplication {
    private static final Logger LOG = LoggerFactory.getLogger(WebServerApplication.class);

    public static void main(String[] args) {
        //Setup engine
        LOG.info("Initializing Decision Engine...");
        DecisionEngine decisionEngine = FactoryDecisionEngine.getSingletonInstance();
        decisionEngine.setConfigurationFile("src/test/resources/configFile.json");
        decisionEngine.initializeEngine();
        LOG.info("Initialized.");

        decisionEngine.startWebServer();
    }

}
