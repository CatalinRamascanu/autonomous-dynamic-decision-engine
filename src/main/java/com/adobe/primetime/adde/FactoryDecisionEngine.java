package com.adobe.primetime.adde;

public class FactoryDecisionEngine {
    // Singleton
    private static DecisionEngine instance = null;
    private FactoryDecisionEngine() {
        // Exists only to defeat instantiation.
    }
    public static DecisionEngine getSingletonInstance() {
        if(instance == null) {
            instance = new DecisionEngine();
        }
        return instance;
    }
}
