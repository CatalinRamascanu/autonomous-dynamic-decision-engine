import com.espertech.esper.client.*;

import java.util.Random;

/**
 * Created by ramascan on 10/03/15.
 */
public class Main {
    public static void main(String[] args){
        //The Configuration is meant only as an initialization-time object.
        Configuration cepConfig = new Configuration();
        // We register Ticks as objects the engine will have to handle
        cepConfig.addEventType("StockTick",Tick.class.getName());

        // We setup the engine
        EPServiceProvider cep = EPServiceProviderManager.getProvider("myCEPEngine", cepConfig);

        EPRuntime cepRT = cep.getEPRuntime();

        // We register an EPL statement
        EPAdministrator cepAdm = cep.getEPAdministrator();
        EPStatement cepStatement = cepAdm.createEPL("select * from " +
                "StockTick(symbol='AAPL').win:length(2) " +
                "having avg(price) > 6.0");

        cepStatement.addListener(new CEPListener());

        // We generate a few ticks...
        for (int i = 0; i < 5; i++) {
            GenerateRandomTick(cepRT);
        }

    }

    public static class CEPListener implements UpdateListener {

        public void update(EventBean[] newData, EventBean[] oldData) {
            System.out.println("Event received: " + newData[0].getUnderlying());
        }
    }

    private static Random generator=new Random();

    public static void GenerateRandomTick(EPRuntime cepRT){
        double price = (double) generator.nextInt(10);
        long timeStamp = System.currentTimeMillis();
        String symbol = "AAPL";
        Tick tick= new Tick(symbol,price,timeStamp);
        System.out.println("Sending tick:" + tick);
        cepRT.sendEvent(tick);
    }
}
