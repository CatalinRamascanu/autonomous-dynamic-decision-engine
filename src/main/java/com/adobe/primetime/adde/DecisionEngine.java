package com.adobe.primetime.adde;

import com.adobe.primetime.adde.configuration.ConfigurationParser;
import com.adobe.primetime.adde.fetcher.FetcherAgent;
import com.adobe.primetime.adde.fetcher.FetcherData;
import com.adobe.primetime.adde.input.InputData;
import com.adobe.primetime.adde.fetcher.FetcherManager;
import com.adobe.primetime.adde.output.Action;
import com.adobe.primetime.adde.output.ReturnAction;
import com.adobe.primetime.adde.output.RuleListener;
import com.adobe.primetime.adde.rules.RuleData;
import com.adobe.primetime.adde.rules.RuleException;
import com.adobe.primetime.adde.rules.RuleManager;
import com.adobe.primetime.adde.rules.RuleModel;
import com.espertech.esper.client.*;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class DecisionEngine {
    private static final Logger LOG = LoggerFactory.getLogger(DecisionEngine.class);
    private final String ENGINE_ID = "esperEngine";

    private boolean isRunning = false;

    private String configurationFilePath;
    private ConfigurationParser confParser;
    private EPServiceProvider epService;
    private EPRuntime epRuntime;

    private Map<String,InputData> inputMap;
    private Map<String, FetcherData> fetcherMap;
    private Map<String,RuleData> ruleMap;
    private Map<String,Action> actionMap;

    FetcherManager fetcherManager;

    Map<String,Map<String,Object>> returnValue;
    Object returnValueLock;

    // Web monitor objects
    // LogHistoryMap:
    // Key = User session ID (Way of identifying each user).
    // Value = The last log that was sent to the user. Each log will be identified by its position in the array list.
    Map<String,Integer> logHistoryMap = new HashMap<>();
    List<String> logHistory = new ArrayList<>();

    public void setConfigurationFile(String filePath){
        configurationFilePath = filePath;
    }

    public void initializeEngine(){
        if (configurationFilePath == null){
            LOG.info("[CONFIG] - Configuration file path not specified");
            addLogToHistory("Configuration file path not specified.\nSetting up engine with empty configuration...");
            // Setting up engine with empty configuration.
            Configuration cepConfig = new Configuration();
            epService = EPServiceProviderManager.getProvider(ENGINE_ID, cepConfig);
            epRuntime= epService.getEPRuntime();
            addLogToHistory("[CONFIG] - Engine has been initialized.");
            isRunning = true;
            return;
        }

        addLogToHistory("[CONFIG] - Setting up engine with configuration from file '" + configurationFilePath + "'..");

        confParser = new ConfigurationParser(this,configurationFilePath);
        confParser.parseJsonAndValidate();

        inputMap = confParser.getInputMap();
        fetcherMap = confParser.getFetcherMap();
        ruleMap = confParser.getRuleMap();
        actionMap = confParser.getActionMap();

        Configuration cepConfig = new Configuration();

        // Add event types
        for (String inputID : inputMap.keySet()){
            addLogToHistory("[CONFIG] - Defining input with ID = '" + inputID + "'...");
            InputData input = inputMap.get(inputID);
            cepConfig.addEventType(input.getInputID(),input.getTypeMap());
        }
        addLogToHistory("[CONFIG] - Input data defined successfully.");

        // Setup the rule engine
        epService = EPServiceProviderManager.getProvider(ENGINE_ID, cepConfig);

        // Define rules
        RuleManager ruleManager = new RuleManager(ruleMap, actionMap,this);
        ruleManager.addRulesToEngine(epService);
        addLogToHistory("[CONFIG] - Rules and actions defined successfully.");

        epRuntime= epService.getEPRuntime();

        // Define fetchers
        fetcherManager = new FetcherManager(fetcherMap,inputMap,this);
        fetcherManager.startFetchers();

        // Define returnValueLock
        returnValueLock = new Object();

        isRunning = true;

        addLogToHistory("[CONFIG] - Engine has been initialized.");
    }

    public void addInputData(String inputID, Map<String, Object> dataMap){
        if (inputID == null || dataMap == null){
            throw new EngineException("addInputData() method contains null parameters.");
        }

        if (!inputMap.containsKey(inputID)){
            throw new EngineException("Input with ID '" + inputID + "' is not defined.");
        }

        if (inputMap.get(inputID).getTypeMap().size() != dataMap.size()){
            throw new EngineException("Wrong number of fields used for input with ID '" + inputID + "'." +
                    "It requires " + inputMap.get(inputID).getTypeMap().size() + "' fields and you are trying to add " +
                    dataMap.size() + " fields. You must add values for all fields.");
        }

        Map<String,Object> typeMap = inputMap.get(inputID).getTypeMap();

        for (String inputName : dataMap.keySet()){
            if (!typeMap.containsKey(inputName)){
                throw new EngineException("Input field '" + inputName + "' is not defined in input '" + inputID +"'.");
            }

            Object dataValueObj = dataMap.get(inputName);
            Object inputTypeObj = typeMap.get(inputName);

            if (!dataValueObj.getClass().equals(inputTypeObj) ){
                throw new EngineException("Wrong type used for '" + inputName +
                        "'. Required '" + inputTypeObj +
                        "' but used '" + dataValueObj.getClass().getName() +"'.");
            }
        }
        epRuntime.sendEvent(dataMap, inputID);
    }

    public void addNewRule(RuleModel ruleModel){
        if (ruleModel == null){
            throw new NullPointerException();
        }

        // Validate model
        String ruleID = ruleModel.getRuleID();
        if (ruleID == null){
            throw new RuleException("Rule Model contains a rule ID which is null ");
        }
        if (ruleMap.containsKey(ruleID)){
            throw new RuleException(
                    "Can not add rule with ID: "+ ruleID + ". Another rule with the same ID already exists."
            );
        }

        for (String inputDomain : ruleModel.getInputDomains()){
            if (!inputMap.containsKey(inputDomain)){
                throw new RuleException(
                        ruleID + ": Input with id: " + inputDomain + " does not exist.Can not use " +
                                "as input domain."
                );
            }
        }

        for (String action : ruleModel.getActions()){
            if (!actionMap.containsKey(action)){
                throw new RuleException(
                        ruleID + ": Action with id: " + action + " does not exist."
                );
            }
        }

        // Create rule statement;
        RuleData ruleData = new RuleData(inputMap,ruleModel);
        ruleMap.put(ruleData.getRuleID(), ruleData);

        // Create ESPER statement
        EPStatement stmt = ruleData.createAndAddStatementToEsper(epService);

        // Attach actions to statement
        for (String actionID : ruleData.getActions()){
            if (actionMap.containsKey(actionID)){
                stmt.addListener(actionMap.get(actionID));
            }
        }
    }

    public void addRuleListener(String ruleID, RuleListener listener){
        if (ruleID == null || listener == null){
            throw new NullPointerException();
        }

        if (!ruleMap.containsKey(ruleID)){
            LOG.error("Rule with ID: " + ruleID + "does not exist. Can not add condition listener.");
            return;
        }

        if (actionMap.containsKey(listener.getListenerID())){
            LOG.error("There is another action with the same ID: " + listener.getListenerID() +
                    ". Can not add condition listener. ");
            return;
        }

        EPStatement stmt = epService.getEPAdministrator().getStatement(ruleID);
        stmt.addListener(listener);

    }

    public Map<String,Map<String,Object>> addInputDataWithReturnValue(
            final String inputID,
            final String actionID,
            final Map<String, Object> dataMap){
        if (actionID == null){
            throw new NullPointerException();
        }

        if (!actionMap.containsKey(actionID)){
            LOG.error("Action with ID: " + actionID + " is not defined.");
            return null;
        }

        final Action action = actionMap.get(actionID);

        if (!(action instanceof ReturnAction)){
            LOG.error("Action with ID: " + actionID + " is not a ReturnAction type.");
            return null;
        }

        ((ReturnAction) action).setupDoneSignal();
        addInputData(inputID,dataMap);
        returnValue = ((ReturnAction) action).getReturnValue();

        return returnValue;
    }

    public Map<String,Map<String,Object>> addInputDataWithReturnValue(
            final String inputID,
            final String actionID,
            final Map<String, Object> dataMap,
            final long waitTimeout){
        if (actionID == null){
            throw new NullPointerException();
        }

        if (!actionMap.containsKey(actionID)){
            LOG.error("Action with ID: " + actionID + " is not defined.");
            return null;
        }

        final Action action = actionMap.get(actionID);

        if (!(action instanceof ReturnAction)){
            LOG.error("Action with ID: " + actionID + " is not a ReturnAction type.");
            return null;
        }

        ((ReturnAction) action).setWaitTimeout(waitTimeout);
        ((ReturnAction) action).setupDoneSignal();
        addInputData(inputID,dataMap);
        returnValue = ((ReturnAction) action).getReturnValue();

        return returnValue;
    }

    public void shutdown(){
        if (isRunning){
            addLogToHistory("[SHUTDOWN] - Stopping data-fetchers...");
            fetcherManager.stopFetchers();

            addLogToHistory("[SHUTDOWN] - Removing rules and actions...");
            EPAdministrator epAdministrator = epService.getEPAdministrator();
            for (String stmtName : epAdministrator.getStatementNames()){
                epAdministrator.getStatement(stmtName).removeAllListeners();
            }
            epAdministrator.destroyAllStatements();

            addLogToHistory("[SHUTDOWN] - Destroying ESPER service...");
            epService.removeAllServiceStateListeners();
            epService.removeAllStatementStateListeners();
            epService.destroy();

            isRunning = false;

            addLogToHistory("[SHUTDOWN] - Engine has been shutdown.");
        }
    }

    public Map<String,Object> castToInputDataType(String inputId, Map<String,String> dataMap){
        if (inputId == null || dataMap == null){
            throw new EngineException("castToInputDataType() method contains null parameters.");
        }

        if (!inputMap.containsKey(inputId)){
            throw new EngineException("Input with ID '" + inputId + "' is not defined.");
        }

        if (inputMap.get(inputId).getTypeMap().size() != dataMap.size()){
            throw new EngineException("Wrong number of fields used for input with ID '" + inputId + "'." +
                    "It requires " + inputMap.get(inputId).getTypeMap().size() + "' fields and you are trying to cast " +
                    dataMap.size() + " fields. You must use values for all fields.");
        }

        Map<String,Object> typeMap = inputMap.get(inputId).getTypeMap();
        Map<String,Object> newDataMap = new HashMap<>();
        for (String inputName : dataMap.keySet()) {
            if (!typeMap.containsKey(inputName)) {
                throw new EngineException("Input field '" + inputName + "' is not defined in input '" + inputId + "'.");
            }

            Object newDataValue;
            try {
                newDataValue = Utils.castToType(dataMap.get(inputName), typeMap.get(inputName));
            } catch (Exception e) {
                throw new EngineException(
                        "Can not cast input field '" + inputName + "' = '" + dataMap.get(inputName) + "' to the following" +
                                " required format '" + typeMap.get(inputName) + "'. "
                );
            }

            newDataMap.put(inputName, newDataValue);
        }

        return newDataMap;
    }

    // The following four getters are used in monitor.jsp
    public Map<String,RuleData> getRuleMap(){
        return ruleMap;
    }

    public Map<String,InputData> getInputMap(){
        return inputMap;
    }

    public Map<String,Action> getActionMap(){
        return actionMap;
    }

    public Map<String, FetcherData> getFetcherMap(){
        return fetcherMap;
    }

    public List<String> getLogHistoryForUser(String userId){
        if (logHistoryMap.containsKey(userId)){
            List<String> history = logHistory.subList(logHistoryMap.get(userId), logHistory.size());
            logHistoryMap.put(userId,logHistory.size());
            return history;
        }
        logHistoryMap.put(userId,logHistory.size());
        return logHistory;
    }

    public void addLogToHistory(String message){
        DateFormat dateFormat = new SimpleDateFormat("[dd/MM/yyyy-HH:mm:ss]");
        Date date = new Date();
        String logMessage = dateFormat.format(date) + ": " + message;
        LOG.info(message);
        logHistory.add(logMessage);
    }

    public void clearLogHistory(){
        logHistoryMap.clear();
        logHistory.clear();
    }

    public boolean isRunning(){
        return isRunning;
    }
}
