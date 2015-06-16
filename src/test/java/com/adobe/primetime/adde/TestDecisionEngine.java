package com.adobe.primetime.adde;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;


/**
 * DecisionEngine integration tests.
 */
public class TestDecisionEngine {
  private static final Logger LOG = LoggerFactory.getLogger(TestDecisionEngine.class);


  @Test
  public void testStartStop() {
    // ...
    // also test isRunning
  }

  @Test
  public void testLogHistory() {
    // ...
    // use addToLogHistory
    // validate with getLogHistoryForUser
  }

  @Test
  public void testAddNewRule() {
    // ...
    // programmatically add new rule
    // inject events and test rule firing
  }

  @Test
  public void testAddRuleListener() {
    // ...
    // programmatically add new rule listener
    // inject events or mock rule firing and verify that the listener was triggered
  }

  // ..

}
