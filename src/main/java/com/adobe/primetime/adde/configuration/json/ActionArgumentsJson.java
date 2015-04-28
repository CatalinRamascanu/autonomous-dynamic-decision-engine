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
    @Key("actors-to-return")
    private List<String> actorsToReturn;

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

    @Nullable
    public List<String> getActorsToReturn() {
        return actorsToReturn;
    }
}
