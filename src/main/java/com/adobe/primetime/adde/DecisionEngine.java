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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class DecisionEngine {
    private static final Logger LOG = LoggerFactory.getLogger(DecisionEngine.class);
    private final String ENGINE_ID = "esperEngine";

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

    public void setConfigurationFile(String filePath){
        configurationFilePath = filePath;
    }

    public void initializeEngine(){
        if (configurationFilePath == null){
            LOG.info("Configuration file path not specified");

            // Setting up engine with empty configuration.
            Configuration cepConfig = new Configuration();
            epService = EPServiceProviderManager.getProvider(ENGINE_ID, cepConfig);
            epRuntime= epService.getEPRuntime();

            return;
        }

        confParser = new ConfigurationParser(configurationFilePath);
        confParser.parseJsonAndValidate();

        inputMap = confParser.getInputMap();
        fetcherMap = confParser.getFetcherMap();
        ruleMap = confParser.getRuleMap();
        actionMap = confParser.getActionMap();

        Configuration cepConfig = new Configuration();

        // Add event types
        for (String inputID : inputMap.keySet()){
            InputData input = inputMap.get(inputID);
            cepConfig.addEventType(input.getInputID(),input.getTypeMap());
        }

        // Setup the rule engine
        epService = EPServiceProviderManager.getProvider(ENGINE_ID, cepConfig);

        // Define rules
        RuleManager ruleManager = new RuleManager(ruleMap, actionMap);
        ruleManager.addRulesToEngine(epService);

        epRuntime= epService.getEPRuntime();

        // Define fetchers
        fetcherManager = new FetcherManager(fetcherMap,inputMap,this);
        fetcherManager.startFetchers();

        // Define returnValueLock
        returnValueLock = new Object();
    }

    public void addInputData(String inputID, Map<String, Object> dataMap){
        if (inputID == null || dataMap == null){
            throw new NullPointerException();
        }

        if (!inputMap.containsKey(inputID)){
            LOG.error("Input with ID: " + inputID + " is not defined.");
            return;
        }

        Map<String,Object> typeMap = inputMap.get(inputID).getTypeMap();

        for (String inputName : dataMap.keySet()){
            if (!typeMap.containsKey(inputName)){
                LOG.error("Input with ID: " + inputID + " is not defined.");
                return;
            }

            Object dataValueObj = dataMap.get(inputName);
            Object inputTypeObj = typeMap.get(inputName);

            if (dataValueObj.getClass().equals(inputTypeObj) ){
                epRuntime.sendEvent(dataMap, inputID);
            }
            else{
                LOG.error("Wrong type used for " + inputName +
                        ". Required " + inputTypeObj +
                        " but used " + dataValueObj.getClass().getName());
            }
        }
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

    public Map<String,Map<String,Object>> addInputDataWithReturnValue(String inputID, String actionID, Map<String, Object> dataMap){
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

        //TODO: I am not 100% sure that this will work all the time.
        // Is it possible the first notify() will be executed before the first wait?
        // If so, that is bad...

        new Thread() {
            public void run() {

                ((ReturnAction) action).setupDoneSignal();
                synchronized (returnValueLock) {
                    returnValueLock.notify();
                }

                returnValue = ((ReturnAction) action).getReturnValue();
                synchronized (returnValueLock){
                    returnValueLock.notify();
                }
            }
        }.start();

        synchronized (returnValueLock){
            try {
                returnValueLock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        addInputData(inputID,dataMap);

        synchronized (returnValueLock){
            try {
                returnValueLock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return returnValue;

    }

    public void shutdown(){
        LOG.info("Stopping fetchers...");
        fetcherManager.stopFetchers();

        LOG.info("Removing statements and listeners...");
        EPAdministrator epAdministrator = epService.getEPAdministrator();
        for (String stmtName : epAdministrator.getStatementNames()){
            epAdministrator.getStatement(stmtName).removeAllListeners();
        }
        epAdministrator.destroyAllStatements();

        LOG.info("Destroying ESPER service...");
        epService.removeAllServiceStateListeners();
        epService.removeAllStatementStateListeners();
        epService.destroy();
    }
}
