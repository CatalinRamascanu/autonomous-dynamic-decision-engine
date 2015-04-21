package com.adobe.primetime.adde.fetcher;

import com.adobe.primetime.adde.DecisionEngine;
import com.adobe.primetime.adde.input.InputData;
import com.espertech.esper.client.EPRuntime;

import java.util.*;

public class FetcherManager {
    private Map<String,FetcherData> fetcherMap;
    private Map<String,InputData> inputMap;
    private ArrayList<Timer> timers = new ArrayList<>();
    private DecisionEngine decisionEngine;
    public FetcherManager(Map<String, FetcherData> fetcherMap, Map<String, InputData> inputMap, DecisionEngine decisionEngine) {
        this.fetcherMap = fetcherMap;
        this.inputMap = inputMap;
        this.decisionEngine = decisionEngine;
    }

    public void startFetchers(){
        for (String fetcherID : fetcherMap.keySet()){
            FetcherData fetcherData = fetcherMap.get(fetcherID);
            InputData inputData = inputMap.get(fetcherData.getReceiverInputID());
            Timer timer = new Timer();

            FetcherAgent fetcherAgent = new FetcherAgent(decisionEngine,inputData,fetcherData,timer);

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.SECOND, fetcherData.getInterval());
            timer.schedule(fetcherAgent, calendar.getTime(), fetcherData.getInterval() * 1000);

            timers.add(timer);
        }
    }
}
