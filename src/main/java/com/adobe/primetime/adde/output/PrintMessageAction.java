package com.adobe.primetime.adde.output;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class PrintMessageAction extends Action {
    private static final Logger LOG = LoggerFactory.getLogger(PrintMessageAction.class);

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
    public void executeAction(Map<String,Object> actorMap) {
        if (targetType == TargetType.STDOUT){
            LOG.info(message);
        }
    }

}
