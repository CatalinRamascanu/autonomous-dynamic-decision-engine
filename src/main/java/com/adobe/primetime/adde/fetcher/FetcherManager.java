package com.adobe.primetime.adde.fetcher;

import com.adobe.primetime.adde.DecisionEngine;
import com.adobe.primetime.adde.input.InputData;
import com.espertech.esper.client.EPRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.midi.Soundbank;
import java.util.*;

public class FetcherManager {
    private static final Logger LOG = LoggerFactory.getLogger(FetcherManager.class);
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
            decisionEngine.addLogToHistory("[CONFIG] - Starting data-fetcher with ID = '"+ fetcherID + "'...");
            FetcherData fetcherData = fetcherMap.get(fetcherID);
            InputData inputData = inputMap.get(fetcherData.getReceiverInputID());
            Timer timer = new Timer();

            FetcherAgent fetcherAgent = new FetcherAgent(decisionEngine,inputData,fetcherData,timer);

            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.SECOND, fetcherData.getInterval());
            timer.schedule(fetcherAgent, calendar.getTime(), fetcherData.getInterval() * 1000);

            fetchers.add(fetcherAgent);

            decisionEngine.addLogToHistory("[CONFIG] - Data-fetcher with ID = '"+ fetcherID + "' initialized.");
        }
    }

    public void stopFetchers(){
        int wait_timeout = 500;

        for (FetcherAgent fetcher : fetchers){
            if (fetcher.isRunning()) {
                LOG.info("Fetcher '" + fetcher.getID() + "' is still running.Telling it stop...");
                fetcher.stop();

                LOG.info("Wating for fetcher '" + fetcher.getID() + "' to stop...");
                synchronized (fetcher){
                    try {
                        fetcher.wait(wait_timeout);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        LOG.info("All data-fetchers were stopped.");
    }
}
