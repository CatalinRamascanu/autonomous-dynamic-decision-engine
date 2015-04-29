package com.adobe.primetime.adde.webserver.controller;

import java.util.Map;

import com.adobe.primetime.adde.DecisionEngine;
import com.adobe.primetime.adde.EngineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EngineController {
    private static final Logger LOG = LoggerFactory.getLogger(EngineController.class);
    private DecisionEngine decisionEngine = DecisionEngine.getInstance();


    // Example usage: http://127.0.0.1:8080/insert_input?inputId=elements&element=2
    @RequestMapping("/insert_input")
    public ResponseEntity insertInput(@RequestParam(value = "inputId") String inputId,
                             @RequestParam Map<String, String> dataMap) throws ControllerException {
        try {
            dataMap.remove("inputId");
            Map<String,Object> newDataMap = decisionEngine.castToInputDataType(inputId,dataMap);
            decisionEngine.addInputData(inputId,newDataMap);
        }
        catch (EngineException e){
            LOG.warn(e.getMessage());
            throw new ControllerException(e.getMessage());
        }

        // TODO: What should I respond in case of success?
        return new ResponseEntity(HttpStatus.ACCEPTED);
    }
}
