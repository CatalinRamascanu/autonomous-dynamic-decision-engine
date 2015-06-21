package com.adobe.primetime.adde.output;

import com.adobe.primetime.adde.DecisionEngine;
import com.adobe.primetime.adde.configuration.json.ActionArgumentsJson;
import com.adobe.primetime.adde.configuration.json.SmtpPropertyJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class SendEmailViaSmtpAction extends Action{
    private static final Logger LOG = LoggerFactory.getLogger(SendEmailViaSmtpAction.class);
    private List<SmtpPropertyJson> smtpPropertyList;
    private String username;
    private String password;
    private List<String> receiverList;
    private String fromAddress;
    private String subject;
    private String message;

    public SendEmailViaSmtpAction(String actionID, ActionArgumentsJson args){
        this.actionID = actionID;

        if (args.getSmtpPropertyList() == null){
            throw new ActionException(
                    actionID + " - No 'smtp-properties' field was passed as argument."
            );
        }
        else {
            smtpPropertyList = args.getSmtpPropertyList();
        }

        if (args.getUsername() != null){
            username = args.getUsername();
            if (args.getPassword() != null){
                password = args.getPassword();
            }
            else {
                throw new ActionException(
                        actionID + " - No 'password' field was passed as argument. 'username' and 'password' fields must be defined together."
                );
            }
        }
        else {
            if (args.getPassword() != null){
                throw new ActionException(
                        actionID + " - No 'username' field was passed as argument. 'username' and 'password' fields must be defined together."
                );
            }
        }

        if (args.getReceiverList() == null){
            throw new ActionException(
                    actionID + " - No 'receiver-list' field was passed as argument."
            );
        }
        else {
            receiverList = args.getReceiverList();
        }

        // From address is optional.
        if (args.getFromAddress() != null) {
            fromAddress = args.getFromAddress();
        }

        if (args.getSubject() == null){
            throw new ActionException(
                    actionID + " - No 'subject' field was passed as argument."
            );
        }
        else {
            subject = args.getSubject();
        }

        if (args.getMessage() == null){
            throw new ActionException(
                    actionID + " - No 'message' field was passed as argument."
            );
        }
        else {
            message = args.getMessage();
        }
    }

    @Override
    public void executeAction(String ruleID, Map<String, Object> actorMap) {
        DecisionEngine engine = DecisionEngine.getInstance();
        engine.addLogToHistory("[ACTION] - '" + actionID + "' is setting up email service... ");


        Properties properties = new Properties();
        for (SmtpPropertyJson property : smtpPropertyList){
            properties.put(property.getName(), property.getValue());
        }
        engine.addLogToHistory("[ACTION] - '" + actionID + "' added SMTP properties.");

        Session session;
        // Get the default Session object.
        if (username != null && password != null){
            engine.addLogToHistory("[ACTION] - '" + actionID + "' creating a session with password authentication...");
            session = Session.getInstance(properties,
                    new javax.mail.Authenticator() {
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(username, password);
                        }
                    });

        }
        else {
            engine.addLogToHistory("[ACTION] - '" + actionID + "' creating a default session...");
            session = Session.getDefaultInstance(properties);
        }
        engine.addLogToHistory("[ACTION] - '" + actionID + "' created session instance.");

        try{
            // Create a default MimeMessage object.
            MimeMessage mimeMsg = new MimeMessage(session);

            // Set From: header field of the header.
            if (fromAddress != null){
                mimeMsg.setFrom(new InternetAddress(fromAddress));
            }

            // Set To: header field of the header.
            for (String recipient : receiverList){
                mimeMsg.addRecipient(Message.RecipientType.TO,
                        new InternetAddress(recipient));
            }

            // Set Subject: header field
            mimeMsg.setSubject(subject);

            // Now set the actual message
            mimeMsg.setText(message);

            engine.addLogToHistory("[ACTION] - '" + actionID + "' is sending email to recipients...");
            // Send message
            Transport.send(mimeMsg);
            engine.addLogToHistory("[ACTION] - '" + actionID + "' sent all emails.");
        }catch (MessagingException mex) {
            engine.addLogToHistory("[ACTION] - '" + actionID + "' generated an exception. Exception message: \n" + mex.getMessage());
        }
        engine.addLogToHistory("[ACTION] - '" + actionID + "' finished executing.");
    }
}
