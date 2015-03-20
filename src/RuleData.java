import java.util.ArrayList;

/**
 * Created by ramascan on 18/03/15.
 */
public class RuleData {
    private String ruleID;
    private ArrayList<String> actors;
    private String condition;
    private ArrayList<String> decisions;

    public RuleData(){
        actors = new ArrayList<String>();
        decisions = new ArrayList<String>();
    }

    public RuleData(String ruleID, ArrayList<String> actors, String condition, ArrayList<String> decisions) {
        this.ruleID = ruleID;
        this.actors = actors;
        this.condition = condition;
        this.decisions = decisions;
    }

    public void setRuleID(String ruleID) {
        this.ruleID = ruleID;
    }

    public void setActors(ArrayList<String> actors) {
        this.actors = actors;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public void setDecisions(ArrayList<String> decisions) {
        this.decisions = decisions;
    }

    public String getRuleID() {
        return ruleID;
    }

    public ArrayList<String> getActors() {
        return actors;
    }

    public String getCondition() {
        return condition;
    }

    public ArrayList<String> getDecisions() {
        return decisions;
    }

    @Override
    public String toString() {
        return "RuleData{" +
                "ruleID='" + ruleID + '\'' +
                ", actors=" + actors +
                ", condition='" + condition + '\'' +
                ", decisions=" + decisions +
                '}';
    }
}
