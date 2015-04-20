package com.adobe.primetime.adde.output;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReturnAction extends Action {
    private static final Logger LOG = LoggerFactory.getLogger(ReturnAction.class);

    private Object returnValue = null;

    @Override
    public void executeAction(Object returnValue /*, ... */) {
        // TODO: figure out parameter(s) type(s)
        synchronized(this) {
            this.notifyAll();
        }
    }

    public Object getReturnValue() throws InterruptedException {
        // TODO: figure out return type

        // thoughts on thread safety?
        // is this the best implementation choice?
        // https://docs.oracle.com/javase/tutorial/essential/concurrency/guardmeth.html
        synchronized(this) {
            Object aux;
            if (returnValue != null) {
                aux = returnValue;
                returnValue = null;
                return returnValue;
            }
            this.wait();
            aux = returnValue;
            returnValue = null;
            return returnValue;
        }
    }
}
