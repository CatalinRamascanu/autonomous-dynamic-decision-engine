package com.adobe.primetime.adde.output;

/**
 * Created by ramascan on 03/04/15.
 * Users can implement this interface to define their own custom condition listener.
 */
public interface ConditionListener {
    // This method should return a unique ID for the listener.
    // It will be used to check if another listener has been already added.
    String getListenerID();

    // This is called when the condition of a rule becomes true.
    void onConditionTrue();
}
