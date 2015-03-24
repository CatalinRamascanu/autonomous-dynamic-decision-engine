import com.espertech.esper.client.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by ramascan on 20/03/15.
 */
public class EventDataManager {

    public static void addInputToConfig(Configuration cepConfig,Set<InputData> inputList){
        for (InputData input : inputList){

            Map<String, Object> def = new HashMap<String, Object>();

            // Define data types
            for (DataType dataType : input.getTypeSet()){
                def.put(dataType.getName(), dataType.getType());
            }

            // Add event type
            cepConfig.addEventType(input.getInputID(),def);
        }
    }
}