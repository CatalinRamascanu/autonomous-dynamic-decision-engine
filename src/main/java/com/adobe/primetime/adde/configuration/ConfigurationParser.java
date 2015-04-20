package com.adobe.primetime.adde.configuration;

import com.adobe.primetime.adde.configuration.json.*;
import com.adobe.primetime.adde.fetcher.FetcherData;
import com.adobe.primetime.adde.fetcher.FetcherParser;
import com.adobe.primetime.adde.input.InputData;
import com.adobe.primetime.adde.output.Action;
import com.adobe.primetime.adde.output.PrintMessageAction;
import com.adobe.primetime.adde.rules.RuleData;

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
import java.util.HashMap;
import java.util.Map;

public class ConfigurationParser {
    private static final Logger LOG = LoggerFactory.getLogger(ConfigurationParser.class);
    private String filePath;

    private Map<String,InputData> inputMap = new HashMap();
    private Map<String,FetcherData> fetcherMap = new HashMap();
    private Map<String,RuleData> ruleMap = new HashMap();
    private Map<String,Action> actionMap = new HashMap();

    private ConfigurationJson conf;

    public ConfigurationParser(String filePath) {
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

            inputMap.put(inputData.getInputID(), inputData);
        }

        // Action Map
        for (ActionJson actionJson : conf.getActionJson()){

            String actionID = actionJson.getActionID();
            if (actionID == ""){
                throw new ConfigurationException("Action-id can not be an empty string.");
            }

            String actionType = actionJson.getActionType();

            // TODO: these ifs must go in https://github.com/CatalinRamascanu/autonomous-dynamic-decision-engine/issues/8
            if (actionType.equals("return-value")) {
                ReturnAction action = new ReturnAction();
                // TODO: implement this case
                actionMap.put(action.getActionID(), action);

            } else if (actionType.equals("print-message")){
                PrintMessageAction action = new PrintMessageAction();
                action.setActionID(actionID);

                String targetType = actionJson.getTarget();
                if (targetType != null){
                    action.setTargetType(targetType);
                }
                else{
                    throw new ConfigurationException(
                            action.getActionID() + ": That action type provided does not exist."
                    );
                }

                String message = actionJson.getMessage();
                if (message != null){
                    action.setMessage(message);
                }
                else{
                    throw new ConfigurationException(
                            action.getActionID() + ": Message field was not provided."
                    );
                }

                actionMap.put(action.getActionID(), action);
            }
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

            int numOfFetches = 0;
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
            fetcherData.setNumOfFetches(numOfFetches);

            String fetcherParser = fetcherJson.getFetcherParser();
            if (fetcherParser != null){
                FetcherParser fetcherParserInstance = instantiate(fetcherParser, FetcherParser.class);
                fetcherData.setFetcherParser(fetcherParserInstance);
            }

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
