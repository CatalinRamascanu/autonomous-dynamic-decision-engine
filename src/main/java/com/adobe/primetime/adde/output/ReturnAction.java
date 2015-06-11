package com.adobe.primetime.adde.output;

import com.adobe.primetime.adde.DecisionEngine;
import com.adobe.primetime.adde.configuration.json.ActionArgumentsJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class ReturnAction extends Action {
    private static final Logger LOG = LoggerFactory.getLogger(ReturnAction.class);
    private List<String> actorsToReturn;
    private Map<String,Map<String,Object>> valuesMap;
    private CountDownLatch doneSignal;
    private static int numOfRulesAttached = 0;

    // TODO: Either find a way to calculate timeout or maybe let the user choose his own timeout.
    private final long WAIT_TIMEOUT = 2;

    public ReturnAction(String actionID, ActionArgumentsJson args){
        valuesMap = new HashMap<>();
        this.actionID = actionID;
        actorsToReturn = args.getActorsToReturn();
        doneSignal = new CountDownLatch(0);

        if (actorsToReturn == null){
            throw new ActionException(
                    actionID + " - No 'actors-to-return' field was passed as argument."
            );
        }
    }

    public void increaseNumOfRulesAttached(){
        numOfRulesAttached++;
    }

    @Override
    public void executeAction(String ruleID, Map<String, Object> actorMap) {
        // If nobody is waiting for a return value then there is no need to execute action.
        if (doneSignal.getCount() == 0){
            LOG.debug("ReturnAction executed by rule '" + ruleID + "' but doneSignal was not activated.");
            DecisionEngine.getInstance().addLogToHistory("[ACTION] - '" + actionID + "' can not return value " +
                    "because data was not inserted through addInputDataWithReturnValue() method.");
            return;
        }

        for (String actorID : actorsToReturn){
            if (!actorMap.containsKey(actorID)){
                LOG.error("Actor '" + actorID + "' can not be return. " +
                                "It is not defined in the actors field of the rule '" + ruleID + "'."
                );
            }
            else{
                if (valuesMap.containsKey(ruleID)){
                    valuesMap.get(ruleID).put(actorID,actorMap.get(actorID));
                }
                else{
                    Map<String,Object> newValue = new HashMap<>();
                    newValue.put(actorID,actorMap.get(actorID));
                    valuesMap.put(ruleID,newValue);
                }
            }
        }

        LOG.debug("Decreasing signal count because rule '" + ruleID + "' was triggered.");
        doneSignal.countDown();
    }

    public void setupDoneSignal(){
        LOG.debug("Activating signal with numOfRulesAttached = " + numOfRulesAttached + " ...");
        doneSignal = new CountDownLatch(numOfRulesAttached);
    }

    public Map<String,Map<String,Object>> getReturnValue() {
        try {
            LOG.debug("Waiting until all returnAction are executed or timeout expires...");
            doneSignal.await(WAIT_TIMEOUT, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Map<String,Map<String,Object>> aux = new HashMap<>(valuesMap);
        valuesMap.clear();

        LOG.debug("Waiting done. Returning value...");

        DecisionEngine.getInstance().addLogToHistory("[ACTION] - '" + actionID + "' is returning value...");

        return aux;
    }
}
