package com.adobe.primetime.adde;

import com.adobe.primetime.adde.configuration.ConfigurationParser;
import com.adobe.primetime.adde.esper.EventDataManager;
import com.adobe.primetime.adde.input.InputData;
import com.adobe.primetime.adde.output.Action;
import com.adobe.primetime.adde.rules.RuleData;
import com.adobe.primetime.adde.rules.RuleManager;
import com.adobe.primetime.adde.rules.RuleModel;
import com.espertech.esper.client.*;

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

    private Map<String,InputData> inputMap;
    private Map<String,RuleData> ruleMap;
    private Map<String,Action> actionMap;

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
            inputMap = confParser.getInputMap();
            ruleMap = confParser.getRuleMap();
            actionMap = confParser.getActionMap();
        } catch (IOException e) {
            System.err.println("Could not parse configuration file.");
            e.printStackTrace();
        }

        Configuration cepConfig = new Configuration();

        // Define event types
        EventDataManager.addInputToConfig(cepConfig, inputMap);

        // Setup the rule engine
        epService = EPServiceProviderManager.getProvider("esperEngine", cepConfig);

        // Define rules
        RuleManager ruleManager = new RuleManager(ruleMap, actionMap);
        ruleManager.addRulesToEngine(epService);

        epRuntime= epService.getEPRuntime();
    }

    public void addInputData(String inputId, Map<String, Object> dataMap){
        if (inputId == null || dataMap == null){
            throw new NullPointerException();
        }

        if (!inputMap.containsKey(inputId)){
            System.err.println("Input with ID: " + inputId + " is not defined.");
            return;
        }

        Map<String,Object> typeMap = inputMap.get(inputId).getTypeMap();

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

    public void addNewRule(RuleModel ruleModel){
        if (ruleModel == null){
            throw new NullPointerException();
        }

        RuleData ruleData = new RuleData(inputMap);

        String ruleID = ruleModel.getRuleID();
        if (ruleModel.getRuleID() == null){
            System.err.println("addNewRule: Rule ID is null.");
            return;
        }
        if (ruleMap.containsKey(ruleID)){
            System.err.println("Can not add rule with ID: "+ ruleID + ". Another rule with the same ID already exists.");
        }

        // Create rule statement;
        ruleData.setRuleID(ruleID);

        ruleData.createSelectClause(ruleModel.getActors());
        ruleData.createFromClause();
        ruleData.createWhereClause(ruleModel.getCondition());

        ruleData.setActions(ruleModel.getActions());

        ruleMap.put(ruleData.getRuleID(), ruleData);

        // Create ESPER statement
        EPStatement stmt = ruleData.createEsperStatement(epService);

        // Attach actions to statement
        for (String actionID : ruleData.getActions()){
            if (actionMap.containsKey(actionID)){
                stmt.addListener(actionMap.get(actionID));
            }
            else {
                System.err.println(actionID + "is an undefined action. " +
                                "Rule with ID: " + ruleID + " will not contain this action.");
            }
        }
    }
}
