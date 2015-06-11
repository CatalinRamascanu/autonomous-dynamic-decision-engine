package com.adobe.primetime.adde.webserver.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.adobe.primetime.adde.DecisionEngine;
import com.adobe.primetime.adde.EngineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@RestController
public class EngineController {
    private static final Logger LOG = LoggerFactory.getLogger(EngineController.class);
    private DecisionEngine decisionEngine = DecisionEngine.getInstance();

    // Example usage: http://127.0.0.1:8080/insert_input?inputId=elements&element=2
    @RequestMapping("/insert_input")
    public ResponseEntity<String> insertInput(@RequestParam(value = "inputId") String inputId,
                             @RequestParam Map<String, String> dataMap) throws ControllerException {
        try {
            decisionEngine.addLogToHistory("[URL-QUERY] - Received data for inputId = '" + inputId + "'.\n" +
                    "Inserting data in engine...");
            dataMap.remove("inputId");
            Map<String,Object> newDataMap = decisionEngine.castToInputDataType(inputId,dataMap);
            decisionEngine.addInputData(inputId,newDataMap);
            decisionEngine.addLogToHistory("[URL-QUERY] - Successfully inserted data for inputId = '" + inputId +"'.");
        }
        catch (EngineException e){
            LOG.warn(e.getMessage());
            decisionEngine.addLogToHistory("[URL-QUERY] - " + e.getMessage());
            throw new ControllerException(e.getMessage());
        }

        // TODO: What should I respond in case of success?
        ResponseEntity<String> responseEntity = new ResponseEntity<String>("It works!", HttpStatus.ACCEPTED);
        return responseEntity;
    }

    @RequestMapping("/monitor_updates")
    public List<String> sendMonitorUpdates(HttpServletRequest request) {
        final String sessionId = request.getRequestedSessionId();
        DecisionEngine engine = DecisionEngine.getInstance();
        List<String> logHistory = new ArrayList<>();
        if (engine.isRunning()){
            logHistory = engine.getLogHistoryForUser(sessionId);
        }

        return logHistory;
    }

    @RequestMapping("/monitor")
    public ModelAndView test(ModelAndView modelAndView) {
        return new ModelAndView("monitor");
    }

    @RequestMapping("/shutdown")
     public boolean shutdownEngine(HttpServletRequest request) {
        DecisionEngine engine = DecisionEngine.getInstance();
        if (engine.isRunning()){
            engine.addLogToHistory("[SHUTDOWN] - Process initialized by user with IP: '" + request.getRemoteAddr() + "'...");
            engine.shutdown();
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            engine.clearLogHistory();
            return true;
        }
        else {
            return false;
        }
    }

    @RequestMapping("/startup")
    public boolean startupEngine(HttpServletRequest request) {
        DecisionEngine engine = DecisionEngine.getInstance();
        engine.addLogToHistory("[STARTUP] - Process initialized by user with IP: '" + request.getRemoteAddr() + "'...");
        if (!engine.isRunning()){
            engine.initializeEngine();
            return true;
        }
        else {
            return false;
        }
    }

    @RequestMapping("/isRunning")
    public boolean isRunning(HttpServletRequest request) {
        return DecisionEngine.getInstance().isRunning();
    }
}
