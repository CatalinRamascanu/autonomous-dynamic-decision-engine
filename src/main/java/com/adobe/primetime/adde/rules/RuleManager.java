package com.adobe.primetime.adde.rules;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.soda.*;
import com.adobe.primetime.adde.output.Action;

import java.util.Map;
import java.util.Set;

/**
 * Created by ramascan on 18/03/15.
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
            EPStatementObjectModel model = new EPStatementObjectModel();

            // Set clauses
            model.setSelectClause(ruleData.getSelectClause());
            model.setFromClause(ruleData.getFromClause());
            model.setWhereClause(ruleData.getWhereClause());

            // Create statement
            EPStatement stmt = epService.getEPAdministrator().create(model,ruleData.getRuleID());

            // Attach actions to statement
            for (String actionID : ruleData.getActions()){
                if (actionMap.containsKey(actionID)){
                    stmt.addListener(actionMap.get(actionID));
                }
                else {
                    System.err.println(actionID + "is an undefined action.");
                }
            }
        }
    }
}
