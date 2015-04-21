package com.adobe.primetime.adde.output;

import com.adobe.primetime.adde.Utils;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;

import java.util.Map;

public abstract class Action implements UpdateListener {
    protected String actionID;

    public void setActionID(String actionID) {
        this.actionID = actionID;
    }

    public String getActionID() {
        return actionID;
    }

    public abstract void executeAction(Map<String,Object> actorMap);

    @Override
    public void update(EventBean[] newData, EventBean[] oldData) {
        // TODO: When does newData contains more than 1 EventBean?
        executeAction(Utils.getActorMap(newData[0]));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Action)) return false;

        Action action = (Action) o;

        if (!actionID.equals(action.actionID)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return actionID.hashCode();
    }
}
