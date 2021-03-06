package com.adobe.primetime.adde.rules;

import java.util.ArrayList;

/**
 * It is used to add new rules dynamically into the engine.
 */
public class RuleModel {
    private String ruleID = null;
    private ArrayList<String> inputDomains = new ArrayList();
    private ArrayList<String> actors = new ArrayList();
    private String condition = null;
    private ArrayList<String> actions = new ArrayList();

    public void setRuleID(String ruleID) {
        this.ruleID = ruleID;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public void addInputDomain(String inputDomain){
        inputDomains.add(inputDomain);
    }

    public void addActor(String actor){
        actors.add(actor);
    }

    public void addAction(String actionID){
        actions.add(actionID);
    }

    public String getRuleID() {
        return ruleID;
    }

    public ArrayList<String> getInputDomains() {
        return inputDomains;
    }

    public ArrayList<String> getActors() {
        return actors;
    }

    public String getCondition() {
        return condition;
    }

    public ArrayList<String> getActions() {
        return actions;
    }
}
