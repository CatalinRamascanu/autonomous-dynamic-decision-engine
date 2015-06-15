<!DOCTYPE html>
<html>

   <link rel="stylesheet" type="text/css" href="/resources/css/monitor.css">
   <link rel="stylesheet" type="text/css" href="/resources/css/jquery-ui.min.css">
   <link rel="stylesheet" type="text/css" href="/resources/css/jquery.json-viewer.css">
   <link rel="stylesheet" type="text/css" href="/resources/css/jquery.terminal.css">
   <script src="resources/js/jquery.js"></script>
   <script src="resources/js/jquery-ui.min.js"></script>
   <script src="resources/js/jquery.json-viewer.js"></script>
   <script src="resources/js/jquery.terminal-0.9.0_dev.js"></script>
   <script src="resources/js/monitor.js"></script>
   <script src="resources/js/jsoneditor.js"></script>
   <%@ page import="com.adobe.primetime.adde.*"%>
   <%@ page import="java.util.*"%>
   <%@ page import="com.google.api.client.json.jackson2.JacksonFactory"%>
   <%@ page import="com.google.api.client.json.JsonFactory"%>
   <%@ page import="java.io.IOException"%>
   <%@ page import="com.adobe.primetime.adde.rules.RuleData"%>
   <%@ page import="com.adobe.primetime.adde.input.InputData"%>
   <%@ page import="com.adobe.primetime.adde.output.Action"%>
   <%@ page import="com.adobe.primetime.adde.fetcher.FetcherData"%>
   <% DecisionEngine decisionEngine = DecisionEngine.getInstance();
   JsonFactory factory = new JacksonFactory(); %>
   
   <script>
   var inputDataList = [];
   </script>

   <head>
      <title>Monitor-ADDE</title>
   </head>
   <body>
      <h1>Autonomous and Dynamic Decision&nbsp;Engine</h1>
      <div class="w-row rowclass">
         <div class="w-col w-col-6 columnclass">
            <div class="titleclass">Engine Configuration</div>
            <div class="w-container configcontainer">
               <div id="tabs">
                 <ul>
                   <li><a href="#tabs-1">Input Data</a></li>
                   <li><a href="#tabs-2">Rules</a></li>
                   <li><a href="#tabs-3">Actions</a></li>
                   <li><a href="#tabs-4">Data Fetchers</a></li>
                   <li><a href="#tabs-5">Insert Data</a></li>
                 </ul>
                 <div id="tabs-1">
                  <div id="input-accordion">
                     <%
                       Map<String,InputData> inputMap = decisionEngine.getInputMap();
                       for (String inputId : inputMap.keySet()){
                       String inputJson = "";
                       try {
                           inputJson = factory.toPrettyString(inputMap.get(inputId).getInputJson());
                           } catch (IOException e) {
                              e.printStackTrace();
                           }
                       %>
                           <h3>(<%= inputId %>)</h3>
                           <div>
                              <pre id=<%= inputId + "-json-viewer"%> >
                                 <script type="text/javascript">
                                 rawInputJson = <%= inputJson %>;
                                 orderedJson = {};
                                 orderedJson["input-id"] = rawInputJson["input-id"];
                                 orderedJson["data"] = rawInputJson["data"];
                                    $(<%="'#" + inputId + "-json-viewer'"%>).jsonViewer(orderedJson);
                                    inputDataList.push(<%= inputJson %>);
                                 </script>
                              </pre>
                           </div>
                     <%
                       }
                     %> 
                  </div>
                 </div>
                 <div id="tabs-2">
                   <div id="rule-accordion">
                     <%
                       Map<String,RuleData> ruleMap = decisionEngine.getRuleMap();
                       for (String ruleId : ruleMap.keySet()){
                       String ruleJson = "";
                       try {
                           ruleJson = factory.toPrettyString(ruleMap.get(ruleId).getRuleJson());
                           } catch (IOException e) {
                              e.printStackTrace();
                           }
                       %>
                           <h3>(<%= ruleId %>)</h3>
                           <div>
                              <pre id=<%= ruleId + "-json-viewer"%> >
                                 <script type="text/javascript">
                                 rawRuleJson = <%= ruleJson %>;
                                 orderedJson = {};
                                 orderedJson["rule-id"] = rawRuleJson["rule-id"];
                                 orderedJson["input-domains"] = rawRuleJson["input-domains"];
                                 orderedJson["actors"] = rawRuleJson["actors"];
                                 orderedJson["condition"] = rawRuleJson["condition"];
                                 orderedJson["action"] = rawRuleJson["action"];
                                 $(function() {
                                    $(<%="'#" + ruleId + "-json-viewer'"%>).jsonViewer(orderedJson);
                                 });
                                 </script>
                              </pre>
                           </div>
                     <%
                       }
                     %>
                     
                  </div>
                 </div>
                 <div id="tabs-3">
                   <div id="action-accordion">
                     <%
                       Map<String,Action> actionMap = decisionEngine.getActionMap();
                       for (String actionId : actionMap.keySet()){
                       String actionJson = "";
                       try {
                           actionJson = factory.toPrettyString(actionMap.get(actionId).getActionJson());
                           } catch (IOException e) {
                              e.printStackTrace();
                           }
                       %>
                           <h3>(<%= actionId %>)</h3>
                           <div>
                              <pre id=<%= actionId + "-json-viewer"%> >
                                 <script type="text/javascript">
                                    $(<%="'#" + actionId + "-json-viewer'"%>).jsonViewer(<%= actionJson %>);
                                 </script>
                              </pre>
                           </div>
                     <%
                       }
                     %>
                  </div>
                 </div>
                 <div id="tabs-4">
                  <div id="fetcher-accordion">
                     <%
                       Map<String,FetcherData> fetcherMap = decisionEngine.getFetcherMap();
                       for (String fetcherId : fetcherMap.keySet()){
                       String fetcherJson = "";
                       try {
                           fetcherJson = factory.toPrettyString(fetcherMap.get(fetcherId).getFetcherJson());
                           } catch (IOException e) {
                              e.printStackTrace();
                           }
                       %>
                           <h3>(<%= fetcherId %>)</h3>
                           <div>
                              <pre id=<%= fetcherId + "-json-viewer"%> >
                                 <script type="text/javascript">
                                    $(<%="'#" + fetcherId + "-json-viewer'"%>).jsonViewer(<%= fetcherJson %>);
                                 </script>
                              </pre>
                           </div>
                     <%
                       }
                     %> 
                  </div>
                 </div>
                 <div id="tabs-5">
                    <select id="inputSelectList">
                    </select>
                    <div id='editor_holder'></div>
                    <button id='sendDataButton'>Send data</button>
                 </div>
               </div>
            </div>
         </div>
         <div class="w-col w-col-6 columnclass">
            <div class="titleclass">Engine Monitor</div>

            <div class="w-container monitorcontainer">
              <div id = "term_demo"></div>
            </div>
            <div class="buttonRow">
              <div class='container'>
                <div class='state-button-wrapper'>
                  <button id = "loggingStateButton"></button>
                </div>
              </div>
              <div class='container3'>
                  <p class='statusText' style="">Engine status: </p>
                  <p class='statusText' id = 'engineStatusText'></p>
              </div>
              <div class='container2'>
                <div class='state-button-wrapper2'>
                  <button id = "engineStateButton"></button>
                </div>
              </div>
         </div>
      </div>
   </body>
</html>