import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by ramascan on 18/03/15.
 */
public class ConfigParser {
    private String fileName;

    private HashSet<String> inputNameList = new HashSet<String>();
    private HashSet<RuleData> ruleList = new HashSet<RuleData>();

    public ConfigParser(String fileName) {
        this.fileName = fileName;
    }

    public void parseFile(){
        JSONParser parser = new JSONParser();

        try {

            JSONObject configObject = (JSONObject) parser.parse(new FileReader(fileName));

            // Input data
            JSONArray inputArray = (JSONArray) configObject.get("input-data");
            Iterator<String> iterator = inputArray.iterator();
            while (iterator.hasNext()) {
                inputNameList.add(iterator.next());
            }
            System.out.println(inputNameList);

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
                ArrayList<String> decisions = new ArrayList<String>();

                JSONArray decisionsArray = (JSONArray) ruleObject.get("actions");
                Iterator<String> decisionsIterator = decisionsArray.iterator();
                while (decisionsIterator.hasNext()){
                    decisions.add(decisionsIterator.next());
                }

                // RuleData
                ruleList.add(new RuleData(ruleID,actors,condition,decisions));
            }
            System.out.println(ruleList);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public HashSet<String> getInputNameList() {
        return inputNameList;
    }

    public HashSet<RuleData> getRuleList() {
        return ruleList;
    }
}
