package com.adobe.primetime.adde.configuration.json;

import com.google.api.client.util.Key;

import javax.annotation.Nullable;

public class ActionJson {
    @Key("action-id")
    private String actionID;

    @Key("action-type")
    private String actionType;

    @Key("class")
    private String className;

    @Key("arguments")
    private ActionArgumentsJson arguments;

    public String getActionID() {
        return actionID;
    }

    public String getActionType() {
        return actionType;
    }

    public String getClassName() {
        return className;
    }

    public ActionArgumentsJson getArguments() {
        return arguments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ActionJson)) return false;

        ActionJson actionJson = (ActionJson) o;

        if (actionID != null ? !actionID.equals(actionJson.actionID) : actionJson.actionID != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return actionID != null ? actionID.hashCode() : 0;
    }
}
