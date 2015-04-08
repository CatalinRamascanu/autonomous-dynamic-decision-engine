package com.adobe.primetime.adde.fetcher;

import com.adobe.primetime.adde.input.InputData;
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

import java.io.IOException;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ramascan on 08/04/15.
 */
public class FetcherAgent extends TimerTask {
    private FetcherData fetcherData;
    private int executionCount = 0;
    private InputData inputData;
    private Timer containerTimer;

    public FetcherAgent(InputData inputData, FetcherData fetcherData, Timer containerTimer) {
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
        if (containerTimer == null){
            // TODO: Throw exeception for null timer;
        }

        if (executionCount < fetcherData.getNumOfFetches()){
            System.out.println("Fetcher with ID: " + fetcherData.getFetcherID() + " will execute now...");
            executionCount++;

            //Fetching data
            HttpRequest request = null;
            String fetchedJson = "";
            try {
                request = REQUEST_FACTORY.buildGetRequest(new GenericUrl(fetcherData.getUrl()));
                fetchedJson = request.execute().parseAsString();
            } catch (IOException e) {
                e.printStackTrace();
            }

            // TODO: Check if it is valid JSON
            System.out.println("We fetched: " + fetchedJson);

            FetcherParser fetcherParser = fetcherData.getFetcherParser();
            if (fetcherParser != null){
                fetchedJson = fetcherParser.parseInputJson(fetchedJson);
                // TODO: Check if new fetchedJSON is valid.
            }

            JSONParser parser = new JSONParser();
            try {
                JSONArray dataArray = (JSONArray) parser.parse(fetchedJson);

                Iterator<JSONObject> it = dataArray.iterator();
                while(it.hasNext()){
                    JSONObject dataObject = it.next();

                    for (Object objKey : dataObject.keySet()){
                        String fieldName = (String) objKey;
                        String fieldValue = (String) dataObject.get(fieldName);

                        System.out.println(fieldName + " : " + fieldValue);
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }

        }
        else {
            System.out.println("Fetcher with ID: " + fetcherData.getFetcherID() + " is shutting down.");
            containerTimer.cancel();
        }
    }

}
