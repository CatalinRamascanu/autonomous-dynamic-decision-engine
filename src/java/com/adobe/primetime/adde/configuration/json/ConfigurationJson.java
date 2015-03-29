package com.adobe.primetime.adde.configuration.json;

import com.google.api.client.util.Key;

import java.util.Set;

/**
 * Created by ramascan on 26/03/15.
 */
public class ConfigurationJson {
    @Key("input")
    private Set<InputJson> inputJson;

    @Key("rules")
    private Set<RuleJson> ruleJson;

   @Key("actions")
    private Set<ActionJson> actionJson;

    public Set<InputJson> getInputJson() {
        return inputJson;
    }

    public Set<RuleJson> getRuleJson() {
        return ruleJson;
    }

    public Set<ActionJson> getActionJson() {
        return actionJson;
    }

    @Override
    public String toString() {
        return "ConfigurationJson{" +
                "input=" + inputJson +
                ",\n rules=" + ruleJson +
                ",\n actions=" + actionJson +
                '}';
    }
}
