package com.adobe.primetime.adde.output;

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

}
