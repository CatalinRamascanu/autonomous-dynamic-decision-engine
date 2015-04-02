package com.adobe.primetime.adde.output;

import com.espertech.esper.client.EventBean;

/**
 * Created by ramascan on 24/03/15.
 */
public class PrintMessageAction extends Action {
    private enum TargetType {
        STDOUT,
        STDERR,
        FILE
    }

    private TargetType targetType;
    private String outputFileName;
    private String message;

    public void setTargetType(String targetType) {
        this.targetType = TargetType.valueOf(targetType);
    }

    public void setOutputFileName(String outputFileName) {
        this.outputFileName = outputFileName;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public void executeAction() {
        if (targetType == TargetType.STDOUT){
            System.out.println(message);
        }
    }

    @Override
    public void update(EventBean[] eventBeans, EventBean[] eventBeans1) {
//        System.out.println("Action with ID: " + super.actionID + " fired.");
        executeAction();
//        System.out.println(eventBeans[0].getUnderlying());
    }
}
