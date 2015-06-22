package com.adobe.primetime.adde.output;

import com.adobe.primetime.adde.DecisionEngine;
import com.adobe.primetime.adde.configuration.json.ActionArgumentsJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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

    public PrintMessageAction(DecisionEngine engine, String actionID, ActionArgumentsJson args){
        this.engine = engine;
        this.actionID = actionID;

        if (args.getMessage() == null){
            throw new ActionException(
                    actionID + " - No 'message' field was passed as argument."
            );
        }
        else {
            message = args.getMessage();
        }

        if (args.getTarget() == null){
            throw new ActionException(
                    actionID + " - No 'target' field was passed as argument."
            );
        }

        try {
            targetType = TargetType.valueOf(args.getTarget().toUpperCase());
        }
        catch (IllegalArgumentException e){
            throw new ActionException(
                    actionID + " - Target value '" + args.getTarget() + "' is invalid." +
                            "Please use one of the following: 'STDOUT', 'STDERR', 'FILE'."
            );
        }

        if (targetType == TargetType.FILE){
            if (args.getFileName() == null){
                throw new ActionException(
                        actionID + " - No 'file-name' field was passed as argument."
                );
            }
            else {
                outputFileName = args.getFileName();
            }
        }
    }

    @Override
    public void executeAction(String ruleID, Map<String,Object> actorMap) {
        if (targetType == TargetType.STDOUT){
            LOG.info(message);
            engine.addLogToHistory("[ACTION] - '" + actionID + "' printed message to STDOUT.");
            return;
        }
        if (targetType == TargetType.STDERR){
            LOG.error(message);
            engine.addLogToHistory("[ACTION] - '" + actionID + "' printed message to STDERR.");
            return;
        }
        if (targetType == TargetType.FILE){
            try {
                engine.addLogToHistory("[ACTION] - '" + actionID + "' is opening file '" + outputFileName + "'...");
                File file = new File(outputFileName);
                PrintWriter out = new PrintWriter(new FileWriter(file, true));
                out.write(message);
                out.flush();
                engine.addLogToHistory("[ACTION] - '" + actionID + "' printed message to '" + outputFileName +"'.");
                out.close();
                engine.addLogToHistory("[ACTION] - '" + actionID + "' closed file '" + outputFileName +"'.");
            } catch (IOException e) {
                engine.addLogToHistory("[ACTION] - '" + actionID + "' generated an exception. Exception message: \n" + e.getMessage());
                e.printStackTrace();
            }
        }
        engine.addLogToHistory("[ACTION] - '" + actionID + "' finished executing.");
    }

}
