package com.adobe.primetime.adde.rules;

import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.EventBean;
import com.espertech.esper.client.UpdateListener;
import com.espertech.esper.client.soda.*;
import com.adobe.primetime.adde.input.DataType;
import com.adobe.primetime.adde.input.InputData;
import com.adobe.primetime.adde.output.Action;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * Created by ramascan on 18/03/15.
 * RuleManager is responsible with the processing of rules defined in the configuration file.
 * It converts the rules to a proper format, accepted by ESPER Rule Engine.
 */
public class RuleManager {
    private Set<RuleData> ruleSet;
    private Set<InputData> inputSet;
    private Set<Action> actionSet;

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

    public void setInputSet(Set<InputData> inputSet) {
        this.inputSet = inputSet;
    }

    public void setActionSet(Set<Action> actionSet) {
        this.actionSet = actionSet;
    }

    public void addRulesToEngine(EPServiceProvider epService){
        for (RuleData ruleData : ruleSet){
            EPStatementObjectModel model = new EPStatementObjectModel();

            // Select clause
            SelectClause selectClause = SelectClause.create();
            for (String actor : ruleData.getActors()){
                selectClause.add(actor);
            }
            model.setSelectClause(selectClause);

            // From clause
            FromClause fromClause = FromClause.create();
            fromClause.add(FilterStream.create("adobeInput"));
            model.setFromClause(fromClause);

            // Where clause
            String postFixCondition = convertToPostFixFormat(ruleData.getCondition());
            Expression whereClauseExpr = createEplExpression(postFixCondition);
            model.setWhereClause(whereClauseExpr);

            // Create statement
            EPStatement stmt = epService.getEPAdministrator().create(model);

            // Attach actions to statement
            for (String actionID : ruleData.getActions()){
                for (Action action : actionSet){
                    if (actionID.equals(action.getActionID())){
                        stmt.addListener(action);
                    }
                }
            }
        }
    }


    // The condition field from the configuration file needs to be
    // converted into postFix format, in order to create the where clause.
    private String convertToPostFixFormat(String expr){
        StringBuilder postFix = new StringBuilder();
        Stack<String> operatorStack = new Stack<String>();

        String[] tokens = expr.split(" ");

        for (String token : tokens){
            // Parenthesis
            // If left parenthesis, push it on to the stack
            if (token.equals("(")){
                operatorStack.push(token);
                continue;
            }
            // If right parenthesis, pop the stack and print the operators until you see a left parenthesis.
            if (token.equals(")")){

                if (operatorStack.isEmpty()){
                    //TODO: Throw exception
                }

                String operator = operatorStack.pop();
                while (!operator.equals("(")){
                    if (operatorStack.isEmpty()){
                        //TODO: Throw exception
                    }

                    postFix.append(operator);
                    postFix.append(' ');

                    operator = operatorStack.pop();
                }

                continue;
            }

            // Operator
            // These operators will always have higher or equal precedence then the top of the stack.
            // We will always them push them to the stack.
            if (token.equals(">") || token.equals("<") ||
                    token.equals(">=") || token.equals("<=") ||
                    token.equals("=") || token.equals("!=")){
                operatorStack.push(token);
                continue;
            }

            // And/Or have lower precedence than the other operators.
            // We should check if there are any higher operators in the stack, so we can pop them.
            if (token.equals("&&") || token.equals("||")){

                if (operatorStack.isEmpty()){
                    operatorStack.push(token);
                    continue;
                }

                String operator = operatorStack.pop();

                if (operator.equals("&&") || operator.equals("||")
                        || operator.equals("(")){
                    // TODO: Invalid format.
                    // There should always be an operator (>,<,= etc.) in the stack
                    // when meeting and/or.
                }

                postFix.append(operator);
                postFix.append(' ');

                operatorStack.push(token);
                continue;
            }

            //TODO: Check if token is a valid operand.
            postFix.append(token);
            postFix.append(" ");
        }

        while(!operatorStack.isEmpty()){
            String operator = operatorStack.pop();

            if (operator.equals("(")){
                // TODO: Invalid format.
            }

            postFix.append(operator);
            postFix.append(" ");
        }

        return postFix.toString();
    }


    private Expression createEplExpression(String postFixString){
        Stack<Object> expressionStack = new Stack<Object>();
        String[] tokens = postFixString.split(" ");

        for (String token : tokens){
            if (token.equals("&&")){
                Expression expr2 = (Expression) expressionStack.pop();
                Expression expr1 = (Expression) expressionStack.pop();

                expressionStack.push(Expressions.and()
                        .add(expr1)
                        .add(expr2));
                continue;
            }

            if (token.equals("||")){
                Expression expr2 = (Expression) expressionStack.pop();
                Expression expr1 = (Expression) expressionStack.pop();

                expressionStack.push(Expressions.or()
                        .add(expr1)
                        .add(expr2));
                continue;
            }

            if (token.equals("=") || token.equals(">") ||
                    token.equals("<") || token.equals(">=") ||
                    token.equals("<=")){
                String op2 = (String) expressionStack.pop();
                String op1 = (String) expressionStack.pop();

                Object propertyType1 = getPropertyType(op1);
                Object propertyType2 = getPropertyType(op2);

                if (propertyType1 != null && propertyType2 != null){
                    if (propertyType1 == propertyType2){
                        expressionStack.push(getExprForProperties(token,op1,op2));
                    }
                    else {
                        //TODO: Invalid expression. Types are different
                    }
                }
                else{
                    if (propertyType1 == null && isNumeric(op1)){
                        Object valueOp1 = castToType(op1,propertyType2);

                        if (valueOp1 == null){
                            //TODO: Invalid operator. Can not be cast to required property type.
                        }
                        else{
                            expressionStack.push(getExprForPropWithValue(token,op2,valueOp1));
                        }
                    }
                    else{
                        if (propertyType2 == null && isNumeric(op2)){
                            Object valueOp2 = castToType(op2,propertyType1);

                            if (valueOp2 == null){
                                //TODO: Invalid operator. Can not be cast to required property type.
                            }
                            else{
                                expressionStack.push(getExprForPropWithValue(token,op1,valueOp2));
                            }
                        }
                        else{
                            // TODO: Invalid operators. Both are not properties or numeric values.
                        }
                    }
                }

                continue;
            }

            // If no match until now, it means token it's a simple operand;
            // Push it to the stack, as it is.
            expressionStack.push(token);
        }

        // After processing all tokens, in the stack should be only one expression.
        // If not, then postFixString is invalid.
        if (expressionStack.size() != 1){
            // TODO: Throw exception
        }

        return (Expression) expressionStack.pop();
    }

    // The method checks if str is a property. If yes, it will return the property type else null.
    private Object getPropertyType(String str){
        for (InputData input : inputSet){
            // Iterate data types
            for (DataType dataType : input.getTypeSet()){
                if (str.equals(dataType.getName())){
                    return dataType.getType();
                }
            }
        }

        return null;
    }
    // It matches a number with optional '-' and decimal.
    private static boolean isNumeric(String str)
    {
        return str.matches("-?\\d+(\\.\\d+)?");
    }

    // It provides expressions where both operands are properties
    private Expression getExprForProperties(String operation, String prop1, String prop2){
        switch (operation){
            case "=":
                return Expressions.eqProperty(prop1, prop2);
            case ">":
                return Expressions.gtProperty(prop1, prop2);
            case "<":
                return Expressions.ltProperty(prop1, prop2);
            case ">=":
                return Expressions.geProperty(prop1, prop2);
            case "<=":
                return Expressions.leProperty(prop1, prop2);
            default:
                return null;
        }
    }

    // It provides expressions where one operand is a property and the other is a value
    private Expression getExprForPropWithValue(String operation, String prop, Object value){
        switch (operation){
            case "=":
                return Expressions.eq(prop, value);
            case ">":
                return Expressions.gt(prop, value);
            case "<":
                return Expressions.lt(prop, value);
            case ">=":
                return Expressions.ge(prop, value);
            case "<=":
                return Expressions.le(prop, value);
            default:
                return null;
        }
    }

    private Object castToType(String str, Object type){
        if (type == String.class){
            return str;
        }

        if (type == int.class){
            return Integer.parseInt(str);
        }

        if (type == long.class){
            return Long.parseLong(str);
        }

        if (type == float.class){
            return Float.parseFloat(str);
        }

        if (type == double.class){
            return Double.parseDouble(str);
        }

        return null;
    }
}
