package com.adobe.primetime.adde.configuration.json;

import com.google.api.client.util.Key;

import java.util.List;

public class InputJson {
    @Key("input-id")
    private String inputID;

    @Key("data")
    private List<InputDataJson> data;

    public String getInputID() {
        return inputID;
    }

    public List<InputDataJson> getData() {
        return data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InputJson)) return false;

        InputJson inputJson = (InputJson) o;

        if (inputID != null ? !inputID.equals(inputJson.inputID) : inputJson.inputID != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return inputID != null ? inputID.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "{" +
                "inputID='" + inputID + '\'' +
                ", data=" + data +
                '}';
    }
}
