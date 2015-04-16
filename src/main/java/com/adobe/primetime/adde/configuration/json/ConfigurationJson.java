package com.adobe.primetime.adde.configuration.json;

import com.google.api.client.util.Key;

import java.util.Set;

public class ConfigurationJson {
    @Key("input")
    private Set<InputJson> inputJson;

    @Key("fetchers")
    private Set<FetcherJson> fetcherJson;

    @Key("rules")
    private Set<RuleJson> ruleJson;

   @Key("actions")
    private Set<ActionJson> actionJson;

    public Set<InputJson> getInputJson() {
        return inputJson;
    }

    public Set<FetcherJson> getFetcherJson() {
        return fetcherJson;
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
