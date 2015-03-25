package com.adobe.primetime.adde.configuration;

import com.adobe.primetime.adde.input.DataType;
import com.adobe.primetime.adde.input.InputData;
import com.adobe.primetime.adde.rules.RuleData;
import com.adobe.primetime.adde.output.Action;
import com.adobe.primetime.adde.output.PrintMessageAction;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by ramascan on 18/03/15.
 * This class is responsible with parsing the json configuration file.
 * The result of the parsing is represented by the fields of type Set.
 */
public class ConfigParser {
    private String fileName;

    private Set<InputData> inputSet = new HashSet();
    private Set<RuleData> ruleSet = new HashSet();
    private Set<Action> actionSet = new HashSet();

    public ConfigParser(String fileName) {
        this.fileName = fileName;
    }

    public Set<InputData> getInputSet() {
        return inputSet;
    }

    public Set<RuleData> getRuleSet() {
        return ruleSet;
    }

    public Set<Action> getActionSet() {
        return actionSet;
    }

    public void parseFile(){
        JSONParser parser = new JSONParser();
        try {

            JSONObject configObject = (JSONObject) parser.parse(new FileReader(fileName));

            // Input data
            JSONArray inputArray = (JSONArray) configObject.get("input-data");
            Iterator<JSONObject> inputIterator = inputArray.iterator();
            while (inputIterator.hasNext()) {
                JSONObject inputObject = inputIterator.next();

                InputData input = new InputData();
                input.setInputID((String) inputObject.get("input-id"));

                JSONObject dataObject = (JSONObject) inputObject.get("data-types");
                for (Object key: dataObject.keySet()){
                    DataType dataType = new DataType();
                    dataType.setName(key.toString());

                    dataType.setType(getTypeObject((String) dataObject.get(key)));

                    input.addDataType(dataType);
                }

                inputSet.add(input);
            }

            // Rule data
            JSONArray ruleArray = (JSONArray) configObject.get("rules");
            Iterator<JSONObject> ruleIterator = ruleArray.iterator();
            while (ruleIterator.hasNext()) {
                JSONObject ruleObject = (JSONObject) ruleIterator.next();

                //ID
                String ruleID = (String) ruleObject.get("rule-id");

                // Actors
                ArrayList<String> actors = new ArrayList<String>();

                JSONArray actorsArray = (JSONArray) ruleObject.get("actors");
                Iterator<String> actorsIterator = actorsArray.iterator();
                while (actorsIterator.hasNext()){
                    actors.add(actorsIterator.next());
                }

                // Condition
                String condition = (String) ruleObject.get("condition");

                // Decisions
                ArrayList<String> actions = new ArrayList<String>();

                JSONArray decisionsArray = (JSONArray) ruleObject.get("actions");
                Iterator<String> decisionsIterator = decisionsArray.iterator();
                while (decisionsIterator.hasNext()){
                    actions.add(decisionsIterator.next());
                }

                // RuleData
                ruleSet.add(new RuleData(ruleID, actors, condition, actions));
            }

            // Actions
            JSONArray actionArray = (JSONArray) configObject.get("actions");
            Iterator<JSONObject> actionIterator = actionArray.iterator();
            while (actionIterator.hasNext()) {
                JSONObject actionObject = actionIterator.next();

                //ID
                String actionID = (String) actionObject.get("action-id");

                //Action type
                String actionType = (String) actionObject.get("action-type");

                if (actionType.equals("print-message")){
                    PrintMessageAction action = new PrintMessageAction();
                    action.setActionID(actionID);

                    String targetType = (String) actionObject.get("target");
                    if (targetType != null){
                        action.setTargetType(targetType);
                    }
                    else{
                        //TODO: Target-type field is not specified.
                    }

                    String message = (String) actionObject.get("message");
                    if (message != null){
                        action.setMessage(message);
                    }
                    else{
                        //TODO: Message field is not specified.
                    }

                    actionSet.add(action);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Object getTypeObject(String value){
        switch (value){
            case "string":
                return String.class;
            case "int":
                return int.class;
            case "long":
                return long.class;
            case "float":
                return float.class;
            case "double":
                return double.class;
            default:
                return null;
        }
    }
}