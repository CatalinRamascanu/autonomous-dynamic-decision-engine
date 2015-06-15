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
    @Key("file-name")
    private String fileName;

    @Nullable
    @Key("constructor-args")
    private List<Object> constructorArguments;

    @Nullable
    @Key("actors-to-return")
    private List<String> actorsToReturn;

    @Nullable
    @Key("destination-ip-address")
    private String destinationIpAddress;

    @Nullable
    @Key("destination-port")
    private String destinationPort;

    @Nullable
    @Key("data-source-type")
    private String dataSourceType;

    @Nullable
    @Key("data-source")
    private String dataSource;

    @Nullable
    @Key("smtp-properties")
    private List<SmtpPropertyJson> smtpPropertyList;

    @Nullable
    @Key("username")
    private String username;

    @Nullable
    @Key("password")
    private String password;

    @Nullable
    @Key("receiver-list")
    private List<String> receiverList;

    @Nullable
    @Key("from-address")
    private String fromAddress;

    @Nullable
    @Key("subject")
    private String subject;

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

    @Nullable
    public String getDestinationIpAddress() {
        return destinationIpAddress;
    }

    @Nullable
    public String getDestinationPort() {
        return destinationPort;
    }

    @Nullable
    public String getDataSourceType() {
        return dataSourceType;
    }

    @Nullable
    public String getDataSource() {
        return dataSource;
    }

    @Nullable
    public List<SmtpPropertyJson> getSmtpPropertyList() {
        return smtpPropertyList;
    }

    @Nullable
    public String getUsername() {
        return username;
    }

    @Nullable
    public String getPassword() {
        return password;
    }

    @Nullable
    public List<String> getReceiverList() {
        return receiverList;
    }

    @Nullable
    public String getFromAddress() {
        return fromAddress;
    }

    @Nullable
    public String getSubject() {
        return subject;
    }

    @Nullable
    public String getFileName() {
        return fileName;
    }
}
