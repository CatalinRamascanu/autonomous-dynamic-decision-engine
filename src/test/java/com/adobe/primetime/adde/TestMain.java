package com.adobe.primetime.adde;

import com.adobe.primetime.adde.output.ConditionListener;
import com.adobe.primetime.adde.rules.RuleModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class TestMain {
    private static final Logger LOG = LoggerFactory.getLogger(TestMain.class);

    @Test
    public static void testMain(){
        LOG.info("Initializing Decision Engine...");
        DecisionEngine decisionEngine = new DecisionEngine();
        decisionEngine.setConfigurationFile("src/test/resources/configFile.json");
        decisionEngine.initializeEngine();
        LOG.info("Initialized.");

        // Add rule through api
        LOG.info("Setting up rules and listeners...");
        RuleModel ruleModel = new RuleModel();
        ruleModel.setRuleID("rule_02");
        ruleModel.addInputDomain("adobeInput");
        ruleModel.addActor("num_online_users");
        ruleModel.addActor("auth_rate");
        ruleModel.addActor("wrong_pass_rate");
        ruleModel.setCondition("1 = wrong_pass_rate && num_online_users < 5");
        ruleModel.addAction("auth-rate-action");
        ruleModel.addAction("action_rule_02");

        decisionEngine.addNewRule(ruleModel);

        // Add custom listener through api
        decisionEngine.addConditionListener("rule_02", new ConditionListener() {
            @Override
            public String getListenerID() {
                return "printHelloListener";
            }

            @Override
            public void onConditionTrue() {
                LOG.info("ConditionListener triggered.");
            }
        });

        LOG.info("Setup complete.");
        LOG.info("Starting test...");

        // We generate a few inputs...
        for (int i = 0; i < 20; i++) {
            Map<String,Object> event = new HashMap();

            float auth_rate = (float) generator.nextInt(10);
            int num_online_users = generator.nextInt(10);
            float wrong_pass_rate = (float) generator.nextInt(10);

            event.put("auth_rate", auth_rate);
            event.put("num_online_users", num_online_users);
            event.put("wrong_pass_rate", wrong_pass_rate);
            event.put("pass-status", "Success");
            decisionEngine.addInputData("adobeInput",event);
        }

        LOG.info("Done.");
    }

    private static Random generator=new Random();
}
