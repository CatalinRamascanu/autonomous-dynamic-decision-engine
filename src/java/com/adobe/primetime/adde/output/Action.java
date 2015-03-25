package com.adobe.primetime.adde.output;

import com.espertech.esper.client.UpdateListener;

/**
 * Created by ramascan on 24/03/15.
 */
public abstract class Action implements UpdateListener {
    protected String actionID;

    public void setActionID(String actionID) {
        this.actionID = actionID;
    }

    public String getActionID() {
        return actionID;
    }

    public abstract void executeAction();

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
