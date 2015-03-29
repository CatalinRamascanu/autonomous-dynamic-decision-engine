package com.adobe.primetime.adde;

import com.adobe.primetime.adde.configuration.*;
import com.adobe.primetime.adde.configuration.json.ConfigurationJson;
import com.adobe.primetime.adde.esper.EventDataManager;
import com.adobe.primetime.adde.rules.RuleManager;
import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by ramascan on 10/03/15.
 */
public class Main {
    public static void main(String[] args){

        ConfigurationParser confParser = new ConfigurationParser("testZone/configFile.json");
        try {
            confParser.parseJsonAndValidate();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Configuration cepConfig = new Configuration();

        // Define event types
        EventDataManager.addInputToConfig(cepConfig, confParser.getInputSet());

        // We setup the engine
        EPServiceProvider cep = EPServiceProviderManager.getProvider("esperEngine", cepConfig);

        // Define rules
        RuleManager ruleManager = new RuleManager(confParser.getRuleSet(), confParser.getActionSet());
        ruleManager.addRulesToEngine(cep);

        EPRuntime cepRT = cep.getEPRuntime();

        System.out.println("All running fine.");

        // We generate a few inputs...
        for (int i = 0; i < 20; i++) {
            Map event = new HashMap();

            float auth_rate = (float) generator.nextInt(10);
            int num_online_users = generator.nextInt(10);
            float wrong_pass_rate = (float) generator.nextInt(10);

            event.put("auth_rate", auth_rate);
            event.put("num_online_users", num_online_users);
            event.put("wrong_pass_rate", wrong_pass_rate);
            event.put("pass-status", "Success");
            cepRT.sendEvent(event, "adobeInput");
        }
    }

    private static Random generator=new Random();
}
