package com.adobe.primetime.adde;

import com.adobe.primetime.adde.output.ConditionListener;
import com.adobe.primetime.adde.rules.RuleModel;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by ramascan on 10/03/15.
 */
public class Main {
    public static void main(String[] args){
        DecisionEngine decisionEngine = new DecisionEngine();
        decisionEngine.setConfigurationFile("src/test/resources/configFile.json");
        decisionEngine.initializeEngine();

        System.out.println("All running fine.");

        // Add rule through api
        RuleModel ruleModel = new RuleModel();
        ruleModel.setRuleID("rule_02");
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
                System.out.println("Hello");
            }
        });

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
    }

    private static Random generator=new Random();
}
