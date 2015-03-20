import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.espertech.esper.client.soda.EPStatementObjectModel;
import com.espertech.esper.client.soda.FilterStream;
import com.espertech.esper.client.soda.FromClause;
import com.espertech.esper.client.soda.SelectClause;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by ramascan on 18/03/15.
 */
public class RuleManager {
    private Set<RuleData> ruleSet;

    public static final String MAP_NAME = "dataMap";

    public RuleManager() {
        ruleSet = new HashSet<RuleData>();
    }

    public RuleManager(Set<RuleData> ruleSet) {
        this.ruleSet = ruleSet;
    }

    public void setRuleSet(Set<RuleData> ruleSet) {
        this.ruleSet = ruleSet;
    }

    public ArrayList<String> addRulesToEngine(EPServiceProvider epService){
        ArrayList<String> statements = new ArrayList<String>();

        for (RuleData ruleData : ruleSet){
            StringBuilder statement = new StringBuilder();

            EPStatementObjectModel model = new EPStatementObjectModel();

            // Select statement
            SelectClause selectClause = SelectClause.create();
            for (String actor : ruleData.getActors()){
                selectClause.add(actor);
            }
            model.setSelectClause(selectClause);

            // From statement
            FromClause fromClause = FromClause.create();
            fromClause.add(FilterStream.create("adobeInput"));
            model.setFromClause(fromClause);

            EPStatement stmt = epService.getEPAdministrator().create(model);
            stmt.addListener(new CEPListener());
        }

        return statements;
    }

    public static class CEPListener implements UpdateListener {

        public void update(EventBean[] newData, EventBean[] oldData) {
            System.out.println("Event received: " + newData[0].getUnderlying());
        }
    }

    /*
    private String convertConditionToEqlFormat(String condition){
        StringBuilder newCondition = new StringBuilder();

        for (String word : condition.split(" ")){
            if (!isNumeric(word) && !isOperator(word)) {
                newCondition.append(MAP_NAME + "('" + word + "') ");
            }
            else{
                newCondition.append(word + " ");
            }
        }

        return newCondition.toString();
    }

    // Match a number with optional '-' and decimal.
    private static boolean isNumeric(String str)
    {
        return str.matches("-?\\d+(\\.\\d+)?");
    }

    private static boolean isOperator(String str)
    {
        return str.matches("<|>|<=|>=|&&|\\|\\||=");
    }
    */
}
