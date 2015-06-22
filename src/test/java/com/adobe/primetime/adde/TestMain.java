package com.adobe.primetime.adde;

import com.adobe.primetime.adde.output.RuleListener;
import com.adobe.primetime.adde.rules.RuleModel;

import org.omg.PortableServer.THREAD_POLICY_ID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class TestMain {
    private static final Logger LOG = LoggerFactory.getLogger(TestMain.class);
    private static Random generator = new Random();
    private DecisionEngine decisionEngine;

    @BeforeMethod
    public void setUp() {
        LOG.info("Initializing Decision Engine...");
        decisionEngine = FactoryDecisionEngine.getSingletonInstance();
        decisionEngine.setConfigurationFile("src/test/resources/configFile.json");
        decisionEngine.initializeEngine();
        LOG.info("Initialized.");
    }

    @Test
    public void testAddRuleThroughAPI(){
        LOG.info("Setting up rule model...");
        RuleModel ruleModel = new RuleModel();
        ruleModel.setRuleID("rule_02");
        ruleModel.addInputDomain("adobeInput");
        ruleModel.addActor("num_online_users");
        ruleModel.addActor("auth_rate");
        ruleModel.addActor("wrong_pass_rate");
        ruleModel.setCondition("1 = wrong_pass_rate && num_online_users < 5");
        ruleModel.addAction("auth-rate-action");
        ruleModel.addAction("my-action");

        LOG.info("Creating rule from rule model...");
        decisionEngine.addNewRule(ruleModel);
        LOG.info("Added rule into engine.");
    }

    @Test
    public void testCustomListener(){
        LOG.info("Setting up custom listener...");

        decisionEngine.addRuleListener("rule_02", new RuleListener() {
            @Override
            public String getListenerID() {
                return "printHelloListener";
            }

            @Override
            public void executeAction(String ruleID,Map<String,Object> actorMap) {
                LOG.info("RuleListener triggered with Map: \n" + actorMap);

            }
        });

        LOG.info("Listener setup complete.");
    }

    @Test
    public void testAddInput(){
        LOG.info("Generating input...");

        for (int i = 0; i < 20; i++) {
            Map<String,Object> event = new HashMap();

            float auth_rate = (float) generator.nextInt(10);
            int num_online_users = generator.nextInt(10);
            float wrong_pass_rate = (float) generator.nextInt(10);

            event.put("auth_rate", auth_rate);
            event.put("num_online_users", num_online_users);
            event.put("wrong_pass_rate", wrong_pass_rate);
            event.put("pass-status", "Success");
            decisionEngine.addInputData("adobeInput", event);
        }

        LOG.info("Input inserted successfully into engine.");
    }

    @Test
    public void testAddInputWithReturnValue(){
        LOG.info("Starting test...");

        for (int i = 0; i < 10; i++) {
            Map<String, Object> event = new HashMap<>();
            long auth_rate = generator.nextInt(10);
            event.put("element", auth_rate);
            LOG.info("Adding element = " + auth_rate);
            Map<String,Map<String,Object>> result = decisionEngine.addInputDataWithReturnValue("elements", "return-element", event);
            LOG.info("Got result: {}", result);
        }

        LOG.info("Done.");
    }

    @Test
    public void testWebServer(){
        decisionEngine.startWebServer();
    }

    @AfterMethod
    public void shutdown(){
        LOG.info("Shutting down engine...");
        decisionEngine.shutdown();
    }

}
