package com.adobe.primetime.adde.rules;

import java.util.ArrayList;

/**
 * Created by ramascan on 18/03/15.
 * Represents a rule defined in the configuration file.
 */
public class RuleData {
    private String ruleID;
    private ArrayList<String> actors;
    private String condition;
    private ArrayList<String> actions;

    public RuleData(){
        actors = new ArrayList<String>();
        actions = new ArrayList<String>();
    }

    public RuleData(String ruleID, ArrayList<String> actors, String condition, ArrayList<String> actions) {
        this.ruleID = ruleID;
        this.actors = actors;
        this.condition = condition;
        this.actions = actions;
    }

    public void setRuleID(String ruleID) {
        this.ruleID = ruleID;
    }

    public void setActors(ArrayList<String> actors) {
        this.actors = actors;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public void setActions(ArrayList<String> actions) {
        this.actions = actions;
    }

    public String getRuleID() {
        return ruleID;
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

    @Override
    public String toString() {
        return "RuleData{" +
                "ruleID='" + ruleID + '\'' +
                ", actors=" + actors +
                ", condition='" + condition + '\'' +
                ", actions=" + actions +
                '}';
    }
}
