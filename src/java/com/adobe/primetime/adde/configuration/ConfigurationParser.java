package com.adobe.primetime.adde.configuration;

import com.adobe.primetime.adde.configuration.json.*;
import com.adobe.primetime.adde.input.InputData;
import com.adobe.primetime.adde.output.Action;
import com.adobe.primetime.adde.output.PrintMessageAction;
import com.adobe.primetime.adde.rules.RuleData;

import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ObjectParser;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ramascan on 26/03/15.
 */
public class ConfigurationParser {
    private String filePath;

    private Map<String,InputData> inputMap = new HashMap();
    private Map<String,RuleData> ruleMap = new HashMap();
    private Map<String,Action> actionMap = new HashMap();

    private ConfigurationJson conf;

    public ConfigurationParser(String filePath) {
        this.filePath = filePath;
    }

    public Map<String,InputData> getInputMap() {
        return inputMap;
    }

    public Map<String,RuleData> getRuleMap() {
        return ruleMap;
    }

    public Map<String,Action> getActionMap() {
        return actionMap;
    }

    public void parseJsonAndValidate() throws IOException {

        // Parse Json
        ObjectParser parser = new JsonObjectParser(new JacksonFactory());
        InputStream input = new FileInputStream(filePath);
        InputStreamReader reader = new InputStreamReader(input);
        conf = parser.parseAndClose(reader, ConfigurationJson.class);

        // Validate and create sets

        // Input Sets
        for (InputJson inputJson : conf.getInputJson()){
            InputData inputData = new InputData();
            inputData.setInputID(inputJson.getInputID());

            if (inputData.getInputID().length() == 0){
                // TODO: InputID is empty string.
            }

            for (InputDataJson inputDataJson : inputJson.getData()){
                String dataName;
                Object dataType;

                dataName = inputDataJson.getName();
                if (dataName == null || dataName.length() == 0){
                    // TODO: DataType name is null or empty string.
                }

                dataType = getTypeObject(inputDataJson.getType());
                if (dataType == null){
                    // TODO: DataType type is invalid.
                }

                inputData.addDataType(dataName,dataType);
            }

            inputMap.put(inputData.getInputID(), inputData);
        }

        // Action Set
        for (ActionJson actionJson : conf.getActionJson()){

            String actionID = actionJson.getActionID();
            if (actionID == ""){
                // TODO: actionId is empty String.
            }

            String actionType = actionJson.getActionType();
            if (actionType.equals("print-message")){
                PrintMessageAction action = new PrintMessageAction();
                action.setActionID(actionID);

                String targetType = actionJson.getTarget();
                if (targetType != null){
                    action.setTargetType(targetType);
                }
                else{
                    //TODO: Target-type field is not specified.
                }

                String message = actionJson.getMessage();
                if (message != null){
                    action.setMessage(message);
                }
                else{
                    //TODO: Message field is not specified.
                }

                actionMap.put(action.getActionID(), action);
            }
        }

        // Rule Set
        for (RuleJson ruleJson : conf.getRuleJson()){
            RuleData ruleData = new RuleData(inputMap);
            ruleData.setRuleID(ruleJson.getRuleID());

            if (ruleData.getRuleID().length() == 0){
                // TODO: RuleID is empty string.
            }

            ruleData.createSelectClause(ruleJson.getActors());
            ruleData.createFromClause();
            ruleData.createWhereClause(ruleJson.getCondition());

            for (String action : ruleJson.getActions()){
                if (!actionMap.containsKey(action)){
                    // TODO: The action does not exist. It was not defined.
                }
            }

            ruleData.setActions(ruleJson.getActions());

            ruleMap.put(ruleData.getRuleID(), ruleData);
        }
    }

    private Object getTypeObject(String value){
        switch (value){
            case "string":
                return String.class;
            case "int":
                return Integer.class;
            case "long":
                return Long.class;
            case "float":
                return Float.class;
            case "double":
                return Double.class;
            default:
                return null;
        }
    }
}
