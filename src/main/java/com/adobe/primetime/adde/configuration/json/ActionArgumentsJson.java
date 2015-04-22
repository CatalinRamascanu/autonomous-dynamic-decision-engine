package com.adobe.primetime.adde.configuration.json;

import com.google.api.client.util.Key;

import javax.annotation.Nullable;
import java.util.List;

public class ActionArgumentsJson {
    @Nullable
    @Key("message")
    private String message;

    @Nullable
    @Key("target")
    private String target;

    @Nullable
    @Key("constructor-args")
    private List<Object> constructorArguments;

    @Nullable
    public String getMessage() {
        return message;
    }

    @Nullable
    public String getTarget() {
        return target;
    }

    @Nullable
    public List<Object> getConstructorArguments() {
        return constructorArguments;
    }
}
