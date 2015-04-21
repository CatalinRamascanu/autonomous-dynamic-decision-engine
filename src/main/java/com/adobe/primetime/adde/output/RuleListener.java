package com.adobe.primetime.adde.output;

/**
 * Users can extend this class to define their own custom listener for rules.
 * They will require to implement getListenerID() and executeAction().
 */

public abstract class RuleListener extends Action {

    // This method should return a unique ID for the listener.
    // It will be used to check if another listener has been already added.
    public abstract String getListenerID();
}
