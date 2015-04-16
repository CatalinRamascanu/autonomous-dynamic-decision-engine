package com.adobe.primetime.adde.fetcher;

import com.adobe.primetime.adde.input.InputData;
import com.espertech.esper.client.EPRuntime;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Timer;

public class FetcherManager {
    private Map<String,FetcherData> fetcherMap;
    private Map<String,InputData> inputMap;
    private ArrayList<Timer> timers = new ArrayList<>();
    private EPRuntime epRuntime;

    public FetcherManager(Map<String, FetcherData> fetcherMap, Map<String, InputData> inputMap, EPRuntime epRuntime) {
        this.fetcherMap = fetcherMap;
        this.inputMap = inputMap;
        this.epRuntime = epRuntime;
    }

    public void startFetchers(){
        for (String fetcherID : fetcherMap.keySet()){

            FetcherData fetcherData = fetcherMap.get(fetcherID);
            InputData inputData = inputMap.get(fetcherData.getReceiverInputID());
            Timer timer = new Timer();

            FetcherAgent fetcherAgent = new FetcherAgent(epRuntime,inputData,fetcherData,timer);
            timer.schedule(fetcherAgent,new Date(), fetcherData.getInterval() * 1000);

            timers.add(timer);
        }
    }
}
