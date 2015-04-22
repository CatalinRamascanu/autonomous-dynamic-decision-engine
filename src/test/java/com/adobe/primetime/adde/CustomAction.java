package com.adobe.primetime.adde;

import com.adobe.primetime.adde.output.Action;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class CustomAction extends Action {
    private static final Logger LOG = LoggerFactory.getLogger(CustomAction.class);

    private String myMessage;

    public CustomAction(String s1, String s2, Number number){
        myMessage = s1 + s2 + number;
    }

    @Override
    public void executeAction(Map<String, Object> actorMap) {
        LOG.info(myMessage);
    }
}
