package com.adobe.primetime.adde.configuration.json;

import com.google.api.client.util.Key;

/**
 * Created by ramascan on 27/03/15.
 */
public class InputDataJson {
    @Key("name")
    private String name;

    @Key("type")
    private String type;

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "InputData{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
