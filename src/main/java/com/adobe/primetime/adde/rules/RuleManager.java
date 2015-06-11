package com.adobe.primetime.adde.rules;

import com.adobe.primetime.adde.DecisionEngine;
import com.adobe.primetime.adde.output.ReturnAction;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.adobe.primetime.adde.output.Action;

import java.util.Map;

/**
 * RuleManager is responsible with adding rules to ESPER Rule Engine.
 */
public class RuleManager {
    private Map<String,RuleData> ruleMap;
    private Map<String,Action> actionMap;
    private DecisionEngine engine;

    public RuleManager(Map<String,RuleData> ruleMap, Map<String,Action> actionMap, DecisionEngine engine) {
        this.ruleMap = ruleMap;
        this.actionMap = actionMap;
        this.engine = engine;
    }

    public void addRulesToEngine(EPServiceProvider epService){
        for (String ruleID : ruleMap.keySet()){
            engine.addLogToHistory("[CONFIG] - Defining rule with ID = '" + ruleID + "'...");
            RuleData ruleData = ruleMap.get(ruleID);

            // Create statement
            EPStatement stmt = ruleData.createAndAddStatementToEsper(epService);

            // Attach actions to statement
            for (String actionID : ruleData.getActions()){
                if (actionMap.containsKey(actionID)){
                    engine.addLogToHistory("[CONFIG] - Attaching action with ID = '" + actionID + "' to rule with ID ='" + ruleID +"'...");
                    Action action = actionMap.get(actionID);
                    stmt.addListener(action);

                    //If it is a ReturnAction we need to let it know that it has been attached to a rule.
                    if (action instanceof ReturnAction){
                        ((ReturnAction) action).increaseNumOfRulesAttached();
                    }
                }
                else{
                    throw new RuleException(
                            ruleID + ": Can not attach action '" + actionID + "' to rule. Action ID does not exist"
                    );
                }
            }
        }
    }
}
