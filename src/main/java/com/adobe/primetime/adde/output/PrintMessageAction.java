package com.adobe.primetime.adde.output;

import com.adobe.primetime.adde.configuration.json.ActionArgumentsJson;
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

    public PrintMessageAction(String actionID, ActionArgumentsJson args){
        this.actionID = actionID;

        if (args.getMessage() == null){
            throw new ActionException(
                    actionID + " - No 'message' field was passed as argument."
            );
        }

        if (args.getTarget() == null){
            throw new ActionException(
                    actionID + " - No 'target' field was passed as argument."
            );
        }

        message = args.getMessage();

        try {
            targetType = TargetType.valueOf(args.getTarget());
        }
        catch (IllegalArgumentException e){
            throw new ActionException(
                    actionID + " - Target value '" + args.getTarget() + "' is invalid." +
                            "Please use one of the following: 'STDOUT', 'STDERR', 'FILE'."
            );
        }
    }

    @Override
    public void executeAction(Map<String,Object> actorMap) {
        if (targetType == TargetType.STDOUT){
            LOG.info(message);
        }
    }

}
