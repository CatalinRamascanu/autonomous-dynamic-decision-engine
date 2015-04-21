package com.adobe.primetime.adde.fetcher;

import com.adobe.primetime.adde.DecisionEngine;
import com.adobe.primetime.adde.input.InputData;
import com.espertech.esper.client.EPRuntime;

import javax.sound.midi.Soundbank;
import java.util.*;

public class FetcherManager {
    private Map<String,FetcherData> fetcherMap;
    private Map<String,InputData> inputMap;
    private ArrayList<FetcherAgent> fetchers = new ArrayList<>();
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

            fetchers.add(fetcherAgent);
        }
    }

    public void stopFetchers(){

        // Tell fetchers to stop
        for (FetcherAgent fetcher : fetchers){
            if (fetcher.isRunning()) {
                fetcher.stop();

                // Wait for fetcher to close
                synchronized (fetcher){
                    try {
                        fetcher.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
