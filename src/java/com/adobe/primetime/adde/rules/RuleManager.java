package com.adobe.primetime.adde.rules;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.espertech.esper.client.soda.*;
import com.adobe.primetime.adde.input.DataType;
import com.adobe.primetime.adde.input.InputData;
import com.adobe.primetime.adde.output.Action;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * Created by ramascan on 18/03/15.
 * RuleManager is responsible with adding rules to ESPER Rule Engine.
 */
public class RuleManager {
    private Set<RuleData> ruleSet;
    private Set<Action> actionSet;

    public RuleManager(Set<RuleData> ruleSet, Set<Action> actionSet) {
        this.ruleSet = ruleSet;
        this.actionSet = actionSet;
    }

    public void addRulesToEngine(EPServiceProvider epService){
        for (RuleData ruleData : ruleSet){
            EPStatementObjectModel model = new EPStatementObjectModel();

            // Set clauses
            model.setSelectClause(ruleData.getSelectClause());
            model.setFromClause(ruleData.getFromClause());
            model.setWhereClause(ruleData.getWhereClause());

            // Create statement
            EPStatement stmt = epService.getEPAdministrator().create(model);

            // Attach actions to statement
            for (String actionID : ruleData.getActions()){
                for (Action action : actionSet){
                    if (actionID.equals(action.getActionID())){
                        stmt.addListener(action);
                    }
                }
            }
        }
    }
}
