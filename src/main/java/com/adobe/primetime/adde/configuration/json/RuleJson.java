package com.adobe.primetime.adde.configuration.json;

import com.google.api.client.util.Key;

import java.util.List;

public class RuleJson {
    @Key("rule-id")
    private String ruleID;

    @Key("actors")
    private List<String> actors;

    @Key("condition")
    private String condition;

    @Key("actions")
    private List<String> actions;

    public List<String> getActions() {
        return actions;
    }

    public String getRuleID() {
        return ruleID;
    }

    public List<String> getActors() {
        return actors;
    }

    public String getCondition() {
        return condition;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RuleJson)) return false;

        RuleJson ruleJson = (RuleJson) o;

        if (ruleID != null ? !ruleID.equals(ruleJson.ruleID) : ruleJson.ruleID != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return ruleID != null ? ruleID.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Rule{" +
                "ruleID='" + ruleID + '\'' +
                ", actors=" + actors +
                ", condition=" + condition +
                ", actions=" + actions +
                '}';
    }
}
