package com.adobe.primetime.adde.fetcher;

import com.google.api.client.util.Key;

public class FetcherData {
    private String fetcherID;
    private String receiverInputID;
    private String url;
    private long interval;
    private int numOfFetches;
    private FetcherParser fetcherParser;

    public void setFetcherID(String fetcherID) {
        this.fetcherID = fetcherID;
    }

    public void setReceiverInputID(String receiverInputID) {
        this.receiverInputID = receiverInputID;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setNumOfFetches(int numOfFetches) {
        this.numOfFetches = numOfFetches;
    }

    public void setInterval(long interval) {
        this.interval = interval;
    }

    public void setFetcherParser(FetcherParser fetcherParser) {
        this.fetcherParser = fetcherParser;
    }

    public String getFetcherID() {
        return fetcherID;
    }

    public String getReceiverInputID() {
        return receiverInputID;
    }

    public String getUrl() {
        return url;
    }

    public long getInterval() {
        return interval;
    }

    public int getNumOfFetches() {
        return numOfFetches;
    }

    public FetcherParser getFetcherParser() {
        return fetcherParser;
    }
}

