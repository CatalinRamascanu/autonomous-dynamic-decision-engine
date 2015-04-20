package com.adobe.primetime.adde;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;


// TODO: Implement these case from easiest to hardest
public class TestFramework {
    private static final Logger LOG = LoggerFactory.getLogger(TestFramework.class);
    private static Random generator=new Random();

    private DecisionEngine decisionEngine;


    @BeforeMethod
    public void setUp() {
        LOG.info("Initializing Decision Engine...");
        decisionEngine = new DecisionEngine();
        decisionEngine.setConfigurationFile("src/test/resources/configFileFramework.json");
        decisionEngine.initializeEngine();
        LOG.info("Initialized.");
    }


    @Test
    public void testElement() {
        LOG.info("Starting test...");

        for (int i = 0; i < 10; i++) {
            Map<String, Object> event = new HashMap<>();
            long auth_rate = generator.nextInt(10);
            event.put("element", auth_rate);
            Object result = decisionEngine.callMethod("elements", "return-element", event);

            LOG.info("Got result: {}", result);
        }

        LOG.info("Done.");
    }

    @Test
    public void testTimeWindow() {
        LOG.info("Starting test...");

        for (int i = 0; i < 10; i++) {
            Map<String, Object> event = new HashMap<>();
            long auth_rate = generator.nextInt(10);
            event.put("element", auth_rate);
            Object result = decisionEngine.callMethod("elements", "return-time-sum", event);

            LOG.info("Got result: {}", result);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // pass
            }
        }

        LOG.info("Done.");
    }

    @Test
    public void testBatchWindow() {
        LOG.info("Starting test...");

        for (int i = 0; i < 10; i++) {
            Map<String, Object> event = new HashMap<>();
            long auth_rate = generator.nextInt(10);
            event.put("element", auth_rate);
            Object result = decisionEngine.callMethod("elements", "return-batch-sum", event);

            LOG.info("Got result: {}", result);
        }

        LOG.info("Done.");
    }
}
