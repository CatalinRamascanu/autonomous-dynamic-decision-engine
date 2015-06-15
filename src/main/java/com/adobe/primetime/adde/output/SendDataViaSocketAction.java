package com.adobe.primetime.adde.output;

import com.adobe.primetime.adde.DecisionEngine;
import com.adobe.primetime.adde.configuration.json.ActionArgumentsJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class SendDataViaSocketAction extends Action {
    private enum SourceType  {
        ARGUMENT,
        FILE
    }
    private static final Logger LOG = LoggerFactory.getLogger(SendDataViaSocketAction.class);
    private String destinationIpAddress;
    private int destinationPort;
    private SourceType dataSourceType;
    private String dataSource;

    public SendDataViaSocketAction(String actionID, ActionArgumentsJson args){
        this.actionID = actionID;

        if (args.getDestinationIpAddress() == null){
            throw new ActionException(
                    actionID + " - No 'destination-ip-address' field was passed as argument."
            );
        }
        else {
            destinationIpAddress = args.getDestinationIpAddress();
        }

        if (args.getDestinationPort() == null){
            throw new ActionException(
                    actionID + " - No 'destination-port' field was passed as argument."
            );
        }
        else {
            try {
                destinationPort = Integer.parseInt(args.getDestinationPort());
            }
            catch (NumberFormatException e){
                LOG.error(e.getMessage());
                throw new ActionException(
                        actionID + " - Failed to convert 'destination-port' to integer. NumberFormatException."
                );
            }
        }

        if (args.getDataSourceType() == null){
            throw new ActionException(
                    actionID + " - No 'data-source-type' field was passed as argument."
            );
        }
        else {
            dataSourceType = SourceType.valueOf(args.getDataSourceType().toUpperCase());
        }

        if (args.getDataSource() == null){
            throw new ActionException(
                    actionID + " - No 'data-source' field was passed as argument."
            );
        }
        else {
            dataSource = args.getDataSource();
        }
    }

    @Override
    public void executeAction(String ruleID, Map<String, Object> actorMap) {
        DecisionEngine engine = DecisionEngine.getInstance();

        Socket socket = null;
        try {
            engine.addLogToHistory("[ACTION] - '" + actionID + "' is opening a new socket to " + destinationIpAddress +":" + destinationPort);
            socket = new Socket(destinationIpAddress, destinationPort);
            if (dataSourceType == SourceType.ARGUMENT){
                engine.addLogToHistory("[ACTION] - '" + actionID + "' is sending data passed as argument...");
                OutputStreamWriter out = new OutputStreamWriter(
                        socket.getOutputStream(), StandardCharsets.UTF_8);
                out.write(dataSource);
                out.flush();
            }
            else{
               if (dataSourceType == SourceType.FILE){
                   engine.addLogToHistory("[ACTION] - '" + actionID + "' is opening file '" + dataSource + "'...");
                   File dataFile = new File(dataSource);
                   byte [] byteArray  = new byte [(int) dataFile.length()];
                   BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(dataFile));
                   bufferedInputStream.read(byteArray,0,byteArray.length);
                   OutputStream outputStream = socket.getOutputStream();
                   engine.addLogToHistory("[ACTION] - '" + actionID + "' is sending '" + dataSource + "'(" + byteArray.length + " bytes)...");
                   outputStream.write(byteArray,0,byteArray.length);
                   outputStream.flush();
               }
            }
            engine.addLogToHistory("[ACTION] - '" + actionID + "' has sent data.Closing socket...");
            socket.close();
        } catch (IOException e) {
            engine.addLogToHistory("[ACTION] - '" + actionID + "' generated an exception. Exception message: \n" + e.getMessage());
            e.printStackTrace();
        }
        finally {
            if (socket != null) try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        engine.addLogToHistory("[ACTION] - '" + actionID + "' finished executing.");
    }
}
