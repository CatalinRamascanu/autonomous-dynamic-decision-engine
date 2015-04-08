package com.adobe.primetime.adde.configuration.json;

import com.google.api.client.util.Key;

import javax.annotation.Nullable;

/**
 * Created by ramascan on 07/04/15.
 */
public class FetcherJson {
    @Key("fetcher-id")
    private String fetcherID;

    @Key("receiver-input-id")
    private String receiverInputID;

    @Key("url")
    private String url;

    @Key("interval")
    private String interval;

    @Nullable
    @Key("num-of-fetches")
    private String numOfFetches;

    @Nullable
    @Key("fetcher-parser")
    private String fetcherParser;

    public String getFetcherID() {
        return fetcherID;
    }

    public String getReceiverInputID() {
        return receiverInputID;
    }

    public String getUrl() {
        return url;
    }

    public String getInterval() {
        return interval;
    }

    @Nullable
    public String getNumOfFetches() {
        return numOfFetches;
    }

    @Nullable
    public String getFetcherParser() {
        return fetcherParser;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FetcherJson)) return false;

        FetcherJson that = (FetcherJson) o;

        if (!fetcherID.equals(that.fetcherID)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return fetcherID.hashCode();
    }
}
