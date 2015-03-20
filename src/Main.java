import com.espertech.esper.client.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by ramascan on 10/03/15.
 */
public class Main {
    public static void main(String[] args){

        ConfigParser parser = new ConfigParser("testZone/configFile.json");
        parser.parseFile();

        Configuration cepConfig = new Configuration();

        // Define event types
        EventDataManager.addInputToConfig(cepConfig, parser.getInputSet());

        // We setup the engine
        EPServiceProvider cep = EPServiceProviderManager.getProvider("esperEngine", cepConfig);

        // Define rules
        RuleManager ruleManager = new RuleManager();
        ruleManager.setRuleSet(parser.getRuleSet());
        ruleManager.addRulesToEngine(cep);

        EPRuntime cepRT = cep.getEPRuntime();

        System.out.println("All running fine.");

        // We generate a few inputs...
        for (int i = 0; i < 1; i++) {
            Map event = new HashMap();
            event.put("auth_rate", generator.nextFloat());
            event.put("num_online_users", generator.nextInt(10));
            event.put("wrong_pass_rate", (float) generator.nextInt(10));
            event.put("pass-status", "Success");
            cepRT.sendEvent(event, "adobeInput");
        }
    }


    private static Random generator=new Random();
}
