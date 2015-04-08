package com.adobe.primetime.adde.fetcher;

import com.adobe.primetime.adde.input.InputData;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Timer;

/**
 * Created by ramascan on 07/04/15.
 */
public class FetcherManager {
    private Map<String,FetcherData> fetcherMap;
    private Map<String,InputData> inputMap;
    private ArrayList<Timer> timers = new ArrayList<>();

    public void setFetcherMap(Map<String, FetcherData> fetcherMap, Map<String,InputData> inputMap) {
        this.fetcherMap = fetcherMap;
        this.inputMap = inputMap;
    }

    public void startFetchers(){
        for (String fetcherID : fetcherMap.keySet()){

            FetcherData fetcherData = fetcherMap.get(fetcherID);
            InputData inputData = inputMap.get(fetcherData.getReceiverInputID());
            Timer timer = new Timer();

            FetcherAgent fetcherAgent = new FetcherAgent(inputData,fetcherData,timer);
            timer.schedule(fetcherAgent,new Date(), fetcherData.getInterval() * 1000);

            timers.add(timer);
        }
    }
}
