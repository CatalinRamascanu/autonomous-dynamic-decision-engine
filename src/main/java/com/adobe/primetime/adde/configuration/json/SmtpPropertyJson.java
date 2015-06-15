package com.adobe.primetime.adde.configuration.json;

import com.google.api.client.util.Key;

public class SmtpPropertyJson {
    @Key("prop-name")
    private String name;

    @Key("prop-value")
    private String value;

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }
}
