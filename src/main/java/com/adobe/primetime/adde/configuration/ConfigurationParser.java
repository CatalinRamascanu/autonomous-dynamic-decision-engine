package com.adobe.primetime.adde.configuration;

import com.adobe.primetime.adde.DecisionEngine;
import com.adobe.primetime.adde.Utils;
import com.adobe.primetime.adde.configuration.json.*;
import com.adobe.primetime.adde.fetcher.FetcherData;
import com.adobe.primetime.adde.fetcher.FetcherParser;
import com.adobe.primetime.adde.input.InputData;
import com.adobe.primetime.adde.output.Action;
import com.adobe.primetime.adde.output.PrintMessageAction;
import com.adobe.primetime.adde.rules.RuleData;

import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;

import com.google.api.client.util.ObjectParser;
import org.apache.commons.validator.routines.UrlValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigurationParser {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationParser.class);
    private String filePath;
    private DecisionEngine engine;

    private Map<String,InputData> inputMap = new HashMap();
    private Map<String,FetcherData> fetcherMap = new HashMap();
    private Map<String,RuleData> ruleMap = new HashMap();
    private Map<String,Action> actionMap = new HashMap();

    private ConfigurationJson conf;

    public ConfigurationParser(DecisionEngine engine, String filePath) {
        this.engine = engine;
        this.filePath = filePath;
    }

    public Map<String,InputData> getInputMap() {
        return inputMap;
    }

    public Map<String, FetcherData> getFetcherMap() {
        return fetcherMap;
    }

    public Map<String,RuleData> getRuleMap() {
        return ruleMap;
    }

    public Map<String,Action> getActionMap() {
        return actionMap;
    }

    public void parseJsonAndValidate(){


        // Parse Json
        ObjectParser parser = new JsonObjectParser(new JacksonFactory());
        try {
            InputStream input = new FileInputStream(filePath);
            InputStreamReader reader = new InputStreamReader(input);
            conf = parser.parseAndClose(reader, ConfigurationJson.class);
        }
        catch (IOException e){
            throw new ConfigurationException("Configuration file is not valid.",e);
        }

        // Validate and create sets

        // Input Map
        for (InputJson inputJson : conf.getInputJson()){
            InputData inputData = new InputData();
            inputData.setInputID(inputJson.getInputID());

            if (inputData.getInputID().length() == 0){
                throw new ConfigurationException("Input-id can not be an empty string.");
            }

            for (InputDataJson inputDataJson : inputJson.getData()){
                String dataName;
                Object dataType;

                dataName = inputDataJson.getName();
                if (dataName == null || dataName.length() == 0){
                    throw new ConfigurationException(
                            inputData.getInputID() + ": A data field contains an invalid name. Either null or empty string."
                    );
                }

                dataType = getTypeObject(inputDataJson.getType());
                if (dataType == null){
                    throw new ConfigurationException(
                            inputData.getInputID() + ": A data field contains an invalid type."
                    );
                }

                inputData.addDataType(dataName,dataType);
            }

            // Contain the JSON into the InputData in case it will displayed on the web monitor.
            inputData.setInputJson(inputJson);

            inputMap.put(inputData.getInputID(), inputData);
        }

        // Action Map
        for (ActionJson actionJson : conf.getActionJson()) {
            Action action = null;

            String actionID = actionJson.getActionID();
            if (actionID == "") {
                throw new ConfigurationException("Action-id can not be an empty string.");
            }

            Class<? extends Action> actionClass;
            try {
                actionClass = (Class<? extends Action>) Class.forName(actionJson.getClassName());
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                throw new ConfigurationException(
                        actionID + ": The action class '" + actionJson.getClassName() + "' does not exist." +
                                "Please check the documentation for how to call a class."
                );
            }

            String actionType = actionJson.getActionType();
            if (actionType.equals("built-in")) {
                try {
                    Constructor<? extends Action> constructor = actionClass.getConstructor(DecisionEngine.class, String.class, ActionArgumentsJson.class);
                    action = constructor.newInstance(engine,actionID, actionJson.getArguments());
                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                    throw new ConfigurationException(
                            actionID + ": Failed to create instance of class '" + actionJson.getClassName() + "'. Internal error."
                    );
                }

            } else {
                if (actionType.equals("custom")) {
                    List<Object> constructorArgs = actionJson.getArguments().getConstructorArguments();
                    if (constructorArgs == null) {
                        throw new ConfigurationException(
                                actionID + ": Argument field 'constructor-args' was not found. " +
                                        "Please include this argument field in order to call the custom action class."
                        );
                    }
                    try {
                        action = Utils.instantiate(actionClass, constructorArgs);
                        action.setActionID(actionID);
                    } catch (ReflectiveOperationException e) {
                        e.printStackTrace();
                        throw new ConfigurationException(
                                actionID + ": Failed to find constructor of class '" + actionJson.getClassName() + "'."
                        );
                    }

                } else {
                    throw new ConfigurationException(
                            actionID + ": Action type '" + actionType + "' does not exist. Please use 'built-in' or 'custom'."
                    );
                }
            }

            // Contain the JSON into the Action in case it will displayed on the web monitor.
            action.setActionJson(actionJson);

            actionMap.put(actionID, action);

        }

        // Fetcher Map
        for (FetcherJson fetcherJson : conf.getFetcherJson()){
            FetcherData fetcherData = new FetcherData();

            fetcherData.setFetcherID(fetcherJson.getFetcherID());
            if (fetcherData.getFetcherID() == ""){
                throw new ConfigurationException("Fetcher-id can not be an empty string.");
            }

            String inputID = fetcherJson.getReceiverInputID();
            if (!inputMap.containsKey(inputID)){
                throw new ConfigurationException(
                        fetcherData.getFetcherID() + ": Receiver-input-id contains an ID which does not exist."
                );
            }
            fetcherData.setReceiverInputID(inputID);

            UrlValidator urlValidator = new UrlValidator();
            if (!urlValidator.isValid(fetcherJson.getUrl())){
                throw new ConfigurationException(
                        fetcherData.getFetcherID() + ": URL provided is invalid."
                );
            }
            fetcherData.setUrl(fetcherJson.getUrl());

            String interval = fetcherJson.getInterval();
            int seconds = convertIntervalToSeconds(interval);
            if (seconds == -1){
                throw new ConfigurationException(
                        fetcherData.getFetcherID() + ": Interval time provided is not in the correct format."
                );
            }
            fetcherData.setInterval(seconds);

            // If NumOfFetches is negative, it means the fetcher will run forever.
            int numOfFetches = -1;
            if (fetcherJson.getNumOfFetches() != null){
                try{
                    numOfFetches = Integer.parseInt(fetcherJson.getNumOfFetches());
                }
                catch (NumberFormatException e){
                    throw new ConfigurationException(
                            fetcherData.getFetcherID() + ": Num-of-fetches is not an integer number."
                    );
                }
                if (numOfFetches < 0){
                    throw new ConfigurationException(
                            fetcherData.getFetcherID() + ": Num-of-fetches can not be a negative number."
                    );
                }
            }
            fetcherData.setNumOfFetches(numOfFetches);

            String fetcherParser = fetcherJson.getFetcherParser();
            if (fetcherParser != null){
                FetcherParser fetcherParserInstance = instantiate(fetcherParser, FetcherParser.class);
                fetcherData.setFetcherParser(fetcherParserInstance);
            }

            // Contain the JSON into the Action in case it will displayed on the web monitor.
            fetcherData.setFetcherJson(fetcherJson);

            fetcherMap.put(fetcherData.getFetcherID(),fetcherData);
        }

        // Rule Map
        for (RuleJson ruleJson : conf.getRuleJson()){
            // Validate JSON
            if (ruleJson.getRuleID().length() == 0){
                throw new ConfigurationException("Rule-id can not be an empty string.");
            }

            for (String inputDomain : ruleJson.getInputDomains()){
                if (!inputMap.containsKey(inputDomain)){
                    throw new ConfigurationException(
                            ruleJson.getRuleID() + ": Input with id: " + inputDomain + " does not exist.Can not use " +
                                    "as input domain."
                    );
                }
            }

            for (String action : ruleJson.getActions()){
                if (!actionMap.containsKey(action)){
                    throw new ConfigurationException(
                            ruleJson.getRuleID() + ": Action with id: " + action + " does not exist."
                    );
                }
            }

            // Create RuleData
            RuleData ruleData = new RuleData(inputMap,ruleJson);
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

    private int convertIntervalToSeconds(String interval){
        int timeInterval = 0;

        String[] tokens = interval.split(" ");
        if (tokens.length == 0){
            LOG.error("No tokens have been used in interval time. " +
                    "Please use one of the following tokens, separated by space: " +
                    "<number>h <number>m <number>s. Ex: 1h 30m");
            return -1;
        }
        if (tokens.length > 3){
            LOG.error("Too many tokens in interval time. " +
                    "It can be maximum 3. You inserted  " + tokens.length + " tokens.");
            return -1;
        }

        for (int i = 0; i < tokens.length; i++){
            String token = tokens[i];
            char lastChar = token.charAt(token.length() - 1);
            int multiplier;
            switch (lastChar){
                case 'h':
                    multiplier = 360;
                    break;
                case 'm':
                    multiplier = 60;
                    break;
                case 's':
                    multiplier = 1;
                    break;
                default:
                    LOG.error("Last char of token '" + token + "' is not valid. " +
                            "It should be one of the following: h,m,s");
                    return -1;
            }

            String numberPart = token.substring(0,token.length() - 1);
            int number = 0;
            try{
                number = Integer.parseInt(numberPart);
            }
            catch (NumberFormatException e){
                LOG.error("A token contains the following number '" + numberPart + "' which is not valid.");
                return -1;
            }
            if (number < 0){
                LOG.error("You can not pass a negative number to a token of the interval time for fetcher. " +
                        "Number: " + number);
                return -1;
            }

            timeInterval = number * multiplier;
        }

        return timeInterval;
    }

    public <T> T instantiate(final String className, final Class<T> type){
        try{
            return type.cast(Class.forName(className).newInstance());
        } catch(final InstantiationException e){
            throw new IllegalStateException(e);
        } catch(final IllegalAccessException e){
            throw new IllegalStateException(e);
        } catch(final ClassNotFoundException e){
            throw new IllegalStateException(e);
        }
    }
}
