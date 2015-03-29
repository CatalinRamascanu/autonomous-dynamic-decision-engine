package com.adobe.primetime.adde.configuration;

import com.adobe.primetime.adde.configuration.json.*;
import com.adobe.primetime.adde.input.DataType;
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
import java.util.HashSet;
import java.util.Set;

/**
 * Created by ramascan on 26/03/15.
 */
public class ConfigurationParser {
    private String filePath;

    private Set<InputData> inputSet = new HashSet();
    private Set<RuleData> ruleSet = new HashSet();
    private Set<Action> actionSet = new HashSet();

    private ConfigurationJson conf;

    public ConfigurationParser(String filePath) {
        this.filePath = filePath;
    }

    public Set<InputData> getInputSet() {
        return inputSet;
    }

    public Set<RuleData> getRuleSet() {
        return ruleSet;
    }

    public Set<Action> getActionSet() {
        return actionSet;
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
                DataType dataType = new DataType();

                dataType.setName(inputDataJson.getName());
                if (dataType.getName() == null || dataType.getName().length() == 0){
                    // TODO: DataType name is null or empty string.
                }

                dataType.setType(getTypeObject(inputDataJson.getType()));
                if (dataType.getType() == null){
                    // TODO: DataType type is invalid.
                }

                inputData.addDataType(dataType);
            }

            inputSet.add(inputData);
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

                actionSet.add(action);
            }
        }

        // Rule Set
        for (RuleJson ruleJson : conf.getRuleJson()){
            RuleData ruleData = new RuleData(inputSet);
            ruleData.setRuleID(ruleJson.getRuleID());

            if (ruleData.getRuleID().length() == 0){
                // TODO: RuleID is empty string.
            }

            ruleData.createSelectClause(ruleJson.getActors());
            ruleData.createFromClause();
            ruleData.createWhereClause(ruleJson.getCondition());

            for (String action : ruleJson.getActions()){
                if (!actionSet.contains(action)){
                    // TODO: The action does not exist. It was not defined.
                }
            }

            ruleData.setActions(ruleJson.getActions());

            ruleSet.add(ruleData);
        }
    }

    private Object getTypeObject(String value){
        switch (value){
            case "string":
                return String.class;
            case "int":
                return int.class;
            case "long":
                return long.class;
            case "float":
                return float.class;
            case "double":
                return double.class;
            default:
                return null;
        }
    }
}
