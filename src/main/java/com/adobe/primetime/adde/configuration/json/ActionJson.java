package com.adobe.primetime.adde.configuration.json;

import com.google.api.client.util.Key;

import javax.annotation.Nullable;

public class ActionJson {
    @Key("action-id")
    private String actionID;

    @Key("action-type")
    private String actionType;

    @Nullable
    @Key("message")
    private String message;

    @Nullable
    @Key("target")
    private String target;

    public String getActionID() {
        return actionID;
    }

    public String getActionType() {
        return actionType;
    }

    @Nullable
    public String getMessage() {
        return message;
    }

    @Nullable
    public String getTarget() {
        return target;
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

    @Override
    public String toString() {
        return "Action{" +
                "actionID='" + actionID + '\'' +
                ", actionType='" + actionType + '\'' +
                ", message='" + message + '\'' +
                ", target='" + target + '\'' +
                '}';
    }
}
