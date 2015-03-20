import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by ramascan on 18/03/15.
 */
public class RuleManager {
    private HashSet<RuleData> ruleList;

    public static final String MAP_NAME = "dataMap";

    public RuleManager() {
        ruleList = new HashSet<RuleData>();
    }

    public RuleManager(HashSet<RuleData> ruleList) {
        this.ruleList = ruleList;
    }

    public void setRuleList(HashSet<RuleData> ruleList) {
        this.ruleList = ruleList;
    }

    public ArrayList<String> generateStatements(){
        ArrayList<String> statements = new ArrayList<String>();

        for (RuleData ruleData : ruleList){
            StringBuilder statement = new StringBuilder();

            // Select statement
            statement.append("select ");
            for (String actor : ruleData.getActors()){
                statement.append(MAP_NAME + "('" + actor +"')" + ", ");
            }
            statement.deleteCharAt(statement.length() - 2);

            // From statement
            statement.append("from EventData ");


            statements.add(statement.toString());
        }

        return statements;
    }

}
