package com.adobe.primetime.adde.rules;

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

    public RuleManager(Map<String,RuleData> ruleMap, Map<String,Action> actionMap) {
        this.ruleMap = ruleMap;
        this.actionMap = actionMap;
    }

    public void addRulesToEngine(EPServiceProvider epService){
        for (String ruleID : ruleMap.keySet()){
            RuleData ruleData = ruleMap.get(ruleID);

            // Create statement
            EPStatement stmt = ruleData.createAndAddStatementToEsper(epService);

            // Attach actions to statement
            for (String actionID : ruleData.getActions()){
                if (actionMap.containsKey(actionID)){
                    stmt.addListener(actionMap.get(actionID));
                }
            }
        }
    }
}
