package com.adobe.primetime.adde;

import com.adobe.primetime.adde.configuration.ConfigurationParser;
import com.adobe.primetime.adde.esper.EventDataManager;
import com.adobe.primetime.adde.input.DataType;
import com.adobe.primetime.adde.input.InputData;
import com.adobe.primetime.adde.output.Action;
import com.adobe.primetime.adde.rules.RuleData;
import com.adobe.primetime.adde.rules.RuleManager;
import com.espertech.esper.client.Configuration;
import com.espertech.esper.client.EPRuntime;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPServiceProviderManager;

import java.io.IOException;
import java.util.*;

/**
 * Created by ramascan on 01/04/15.
 */
public class DecisionEngine {

    private String configurationFilePath;
    private ConfigurationParser confParser;
    private EPServiceProvider epService;
    private EPRuntime epRuntime;

    private Set<InputData> inputSet;
    private Set<RuleData> ruleSet;
    private Set<Action> actionSet;

    public void setConfigurationFile(String filePath){
        configurationFilePath = filePath;
    }

    public void initializeEngine(){
        if (configurationFilePath == null){
            System.err.println("Configuration file path not specified");
            return;
        }

        confParser = new ConfigurationParser(configurationFilePath);
        try {
            confParser.parseJsonAndValidate();
            inputSet = confParser.getInputSet();
            ruleSet = confParser.getRuleSet();
            actionSet = confParser.getActionSet();
        } catch (IOException e) {
            System.err.println("Could not parse configuration file.");
            e.printStackTrace();
        }

        Configuration cepConfig = new Configuration();

        // Define event types
        EventDataManager.addInputToConfig(cepConfig, inputSet);

        // Setup the rule engine
        epService = EPServiceProviderManager.getProvider("esperEngine", cepConfig);

        // Define rules
        RuleManager ruleManager = new RuleManager(ruleSet, actionSet);
        ruleManager.addRulesToEngine(epService);

        epRuntime= epService.getEPRuntime();
    }

    public void addInputData(String inputId, Map<String, Object> dataMap){
        InputData newData = new InputData();
        newData.setInputID(inputId);
        if (!inputSet.contains(newData)){
            System.err.println("Input with ID: " + inputId + " is not defined.");
            return;
        }

        Map<String,Object> typeMap = null;
        for (InputData inData : inputSet){
            if (inputId.equals(inData.getInputID())){
                typeMap = inData.getTypeMap();
                break;
            }
        }

        for (String inputName : dataMap.keySet()){
            if (!typeMap.containsKey(inputName)){
                System.err.println("Input with ID: " + inputId + " is not defined.");
                return;
            }

            Object dataValueObj = dataMap.get(inputName);
            Object inputTypeObj = typeMap.get(inputName);

            if (dataValueObj.getClass().equals(inputTypeObj) ){
                epRuntime.sendEvent(dataMap, inputId);
            }
            else{
                System.err.println("Wrong type used for " + inputName +
                        ". Required " + inputTypeObj +
                        " but used " + dataValueObj.getClass().getName());
            }
        }
    }
}
