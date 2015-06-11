package com.adobe.primetime.adde.fetcher;

import com.adobe.primetime.adde.DecisionEngine;
import com.adobe.primetime.adde.Utils;
import com.adobe.primetime.adde.input.InputData;
import com.espertech.esper.client.EPRuntime;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class FetcherAgent extends TimerTask {
    private static final Logger LOG = LoggerFactory.getLogger(FetcherAgent.class);

    private boolean isRunning;
    private FetcherData fetcherData;
    private int executionCount;
    private InputData inputData;
    private Timer containerTimer;
    private DecisionEngine decisionEngine;

    public FetcherAgent(DecisionEngine decisionEngine, InputData inputData, FetcherData fetcherData, Timer containerTimer) {
        if (decisionEngine == null || inputData == null || fetcherData == null || containerTimer == null){
            throw new NullPointerException();
        }
        this.decisionEngine = decisionEngine;
        this.inputData = inputData;
        this.fetcherData = fetcherData;
        this.containerTimer = containerTimer;

        executionCount = 1;
        isRunning = true;
    }

    // INFO: default connection timeout and read timeout are 20s
    protected static final HttpRequestFactory REQUEST_FACTORY =
            new NetHttpTransport().createRequestFactory(new HttpRequestInitializer() {
                @Override
                public void initialize(HttpRequest request) {
                    request.setParser(new JsonObjectParser(new JacksonFactory()));
                }
            });

    @Override
    public void run() {
        String fetcherID = fetcherData.getFetcherID();

        LOG.info("IsRunning = " + isRunning);

        // TODO: Look into this. I am double checking (at the begining and at the end). Maybe another option?
        if (!isRunning ||
                (executionCount > fetcherData.getNumOfFetches()) && fetcherData.getNumOfFetches() >= 0){

            LOG.info("ID: '" + fetcherID + "' - Shutting down...");
            decisionEngine.addLogToHistory("[DATA-FETCHER] - '" + fetcherID + "' is shutting down...");
            containerTimer.cancel();

            // Call notify in order for engine to know that fetcher is dead.
            synchronized (this){
                this.notify();
            }

            isRunning = false;
            decisionEngine.addLogToHistory("[DATA-FETCHER] - '" + fetcherID + "' has stopped.");
            return;
        }


        LOG.info("ID: '" + fetcherID + "' - Will execute now with count = " + executionCount);
        decisionEngine.addLogToHistory("[DATA-FETCHER] - '" + fetcherID + "' will execute now with count = " + executionCount);

        //Fetching data
        HttpRequest request = null;
        String fetchedJson = "";
        try {
            request = REQUEST_FACTORY.buildGetRequest(new GenericUrl(fetcherData.getUrl()));
            fetchedJson = request.execute().parseAsString();

        } catch (IOException e) {
            LOG.error("ID: '" + fetcherID + "' - Failed to fetch data from URL: '" +
                    fetcherData.getUrl() +"'. ");
            decisionEngine.addLogToHistory("[DATA-FETCHER] - '" + fetcherID + "' failed to retrieve data from URL: '" +
                    fetcherData.getUrl() +"'. ");
            e.printStackTrace();
            return;
        }

        LOG.info("ID: '" + fetcherID +"' - Fetched the following data: \n" + fetchedJson);
        decisionEngine.addLogToHistory("[DATA-FETCHER] - '" + fetcherID + "' successfully retrieved data from URL: '" +
                fetcherData.getUrl() +"'. ");

        FetcherParser fetcherParser = fetcherData.getFetcherParser();
        if (fetcherParser != null){
            fetchedJson = fetcherParser.parseInputJson(fetchedJson);
        }

        Map<String,Object> typeMap = inputData.getTypeMap();

        JSONParser parser = new JSONParser();
        try {
            JSONArray dataArray = (JSONArray) parser.parse(fetchedJson);

            Iterator<JSONObject> it = dataArray.iterator();
            while(it.hasNext()){
                JSONObject dataObject = it.next();
                Map<String, Object> dataMap = new HashMap<>();

                for (Object objKey : dataObject.keySet()) {
                    String fieldName = (String) objKey;
                    if (!typeMap.containsKey(fieldName)) {
                        LOG.error("ID: '" + fetcherID + "' - Fetched JSON contains an input field name that was not" +
                                "defined in input " + fetcherData.getReceiverInputID());
                        decisionEngine.addLogToHistory("[DATA-FETCHER] - '" + fetcherID + "' retrieved data which contains an input field name that was not" +
                                "defined in input " + fetcherData.getReceiverInputID());
                        return;
                    }
                    Object fieldValue;
                    try {
                        fieldValue = Utils.castToType((String) dataObject.get(fieldName), typeMap.get(fieldName));
                    } catch (Exception e) {
                        LOG.error("ID: '" + fetcherID + "' - Invalid value '" + dataObject.get(fieldName) +
                                " for field " + fieldName + ". Can not be cast to appropriate type.");
                        decisionEngine.addLogToHistory("[DATA-FETCHER] - '" + fetcherID + "' retrieved an invalid value '" + dataObject.get(fieldName) +
                                " for field " + fieldName + ". Can not be cast to appropriate type.");
                        return;
                    }

                    dataMap.put(fieldName, fieldValue);
                }

                // Add data into engine
                decisionEngine.addInputData(inputData.getInputID(), dataMap);
            }
        } catch (ParseException e) {
            LOG.error("ID: '" + fetcherID + "' - Fetched data is not a valid JSON.");
            decisionEngine.addLogToHistory("[DATA-FETCHER] - '" + fetcherID + "' retrieved data which is not a valid JSON.");
            e.printStackTrace();
            return;
        }

        LOG.info("ID: '" + fetcherID + "' - Successfully " +
                "fetched and inserted data for " + fetcherData.getReceiverInputID() + "." +
                "Finished execution count = " + executionCount + " .");
        decisionEngine.addLogToHistory("[DATA-FETCHER] - '" + fetcherID + "' successfully " +
                "fetched and inserted data for '" + fetcherData.getReceiverInputID() + "'." +
                "Finished execution count = " + executionCount + ".");

        executionCount++;

        if (!isRunning ||
                (executionCount > fetcherData.getNumOfFetches()) && fetcherData.getNumOfFetches() >= 0){

            LOG.info("ID: '" + fetcherID + "' - Shutting down...");
            decisionEngine.addLogToHistory("[DATA-FETCHER] - '" + fetcherID + "' is shutting down...");
            containerTimer.cancel();

            // Call notify in order for engine to know that fetcher is dead.
            synchronized (this){
                this.notify();
            }

            isRunning = false;
            decisionEngine.addLogToHistory("[DATA-FETCHER] - '" + fetcherID + "' has stopped.");
        }
    }

    public void stop(){
        isRunning = false;
    }

    public boolean isRunning(){
        return isRunning;
    }

    public String getID(){
        return fetcherData.getFetcherID();
    }
}
