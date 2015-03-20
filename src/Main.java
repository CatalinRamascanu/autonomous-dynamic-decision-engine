import com.espertech.esper.client.*;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by ramascan on 10/03/15.
 */
public class Main {
    public static void main(String[] args){

        ConfigParser parser = new ConfigParser("testZone/configFile.json");
        parser.parseFile();

        //The Configuration is meant only as an initialization-time object.
        Configuration cepConfig = new Configuration();
        // We register Ticks as objects the engine will have to handle
        cepConfig.addEventType(EventData.class);

        // We setup the engine
        EPServiceProvider cep = EPServiceProviderManager.getProvider("esperEngine", cepConfig);

        RuleManager ruleManager = new RuleManager();
        ruleManager.setRuleList(parser.getRuleList());

        ArrayList<String> statements = ruleManager.generateStatements();
        System.out.println(statements);

        for (String statement: statements){
            // We register an EPL statement
            EPAdministrator cepAdm = cep.getEPAdministrator();
            EPStatement cepStatement = cepAdm.createEPL(statement);
            cepStatement.addListener(new CEPListener());
        }

        EPRuntime cepRT = cep.getEPRuntime();

        // We generate a few inputs...
        for (int i = 0; i < 1; i++) {
            EventData data = new EventData();
            data.addInputNameSet(parser.getInputNameList());

            data.addInput("auth_rate","" + (double) generator.nextInt(10));
            data.addInput("num_online_users","" + (double) generator.nextInt(10));
            data.addInput("wrong_pass_rate","" + (double) generator.nextInt(10));

            cepRT.sendEvent(data);
        }
    }

    public static class CEPListener implements UpdateListener {

        public void update(EventBean[] newData, EventBean[] oldData) {
            System.out.println("Event received: " + newData[0].getUnderlying());
        }
    }

    private static Random generator=new Random();
}
