package com.adobe.primetime.adde.output;

/**
 * Users can implement this interface to define their own custom condition listener.
 */
public interface ConditionListener {
    // This method should return a unique ID for the listener.
    // It will be used to check if another listener has been already added.
    String getListenerID();

    // This is called when the condition of a rule becomes true.
    void onConditionTrue();
}
