package com.adobe.primetime.adde.output;

import com.espertech.esper.client.EventBean;

/**
 * Created by ramascan on 03/04/15.
 */
public class CallListenerAction extends Action {
    private ConditionListener listener;

    public CallListenerAction(ConditionListener listener){
        this.listener = listener;
        setActionID(listener.getListenerID());
    }

    @Override
    public void executeAction() {
        listener.onConditionTrue();
    }

}
