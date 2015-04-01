package com.adobe.primetime.adde.esper;

import com.espertech.esper.client.Configuration;
import com.adobe.primetime.adde.input.DataType;
import com.adobe.primetime.adde.input.InputData;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by ramascan on 20/03/15.
 */
public class EventDataManager {
    public static void addInputToConfig(Configuration cepConfig,Set<InputData> inputList){
        for (InputData input : inputList){
            // Add event type
            cepConfig.addEventType(input.getInputID(),input.getTypeMap());
        }
    }
}
