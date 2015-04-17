package com.adobe.primetime.adde.fetcher;

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

    private FetcherData fetcherData;
    private int executionCount = 0;
    private InputData inputData;
    private Timer containerTimer;
    private EPRuntime epRuntime;

    public FetcherAgent(EPRuntime epRuntime, InputData inputData, FetcherData fetcherData, Timer containerTimer) {
        if (epRuntime == null || inputData == null || fetcherData == null || containerTimer == null){
            throw new NullPointerException();
        }
        this.epRuntime = epRuntime;
        this.inputData = inputData;
        this.fetcherData = fetcherData;
        this.containerTimer = containerTimer;
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
        if (executionCount < fetcherData.getNumOfFetches()){
            LOG.info("ID: '" + fetcherID + "' - Will execute now with count = " + executionCount);

            //Fetching data
            HttpRequest request = null;
            String fetchedJson = "";
            try {
                request = REQUEST_FACTORY.buildGetRequest(new GenericUrl(fetcherData.getUrl()));
                fetchedJson = request.execute().parseAsString();
            } catch (IOException e) {
                LOG.error("ID: '" + fetcherID + "' - Failed to fetch data from URL: '" +
                        fetcherData.getUrl() +"'. ");
                e.printStackTrace();
                return;
            }

            LOG.info("ID: '" + fetcherID +"' - Fetched the following data: \n" + fetchedJson);

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

                    for (Object objKey : dataObject.keySet()){
                        String fieldName = (String) objKey;
                        if (!typeMap.containsKey(fieldName)){
                            LOG.error("ID: '" + fetcherID + "' - Fetched JSON contains an input field name that was not" +
                                            "defined in input " + fetcherData.getReceiverInputID());
                            return;
                        }
                        Object fieldValue = Utils.castToType((String) dataObject.get(fieldName), typeMap.get(fieldName));
                        if (fieldValue == null){
                            LOG.error("ID: '" + fetcherID + "' - Invalid value '" + dataObject.get(fieldName) +
                                            " for field " + fieldName + ". Can not be cast to apropriate type.");
                            return;
                        }

                        dataMap.put(fieldName,fieldValue);
                    }

                    epRuntime.sendEvent(dataMap, inputData.getInputID());
                }
            } catch (ParseException e) {
                LOG.error("ID: '" + fetcherID + "' - Fetched data is not a valid JSON.");
                e.printStackTrace();
                return;
            }

            LOG.info("ID: '" + fetcherID + "' - Successfully " +
                    "fetched and inserted data for " + fetcherData.getReceiverInputID() + "." +
                    "Finished execution count = " + executionCount + " .");

            executionCount++;
        }
        else {
            LOG.info("ID: '" + fetcherID + "' - shutting down...");
            containerTimer.cancel();
        }
    }

}
