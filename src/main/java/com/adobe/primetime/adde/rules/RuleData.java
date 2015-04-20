package com.adobe.primetime.adde.rules;

import com.adobe.primetime.adde.Utils;
import com.adobe.primetime.adde.configuration.json.RuleJson;
import com.adobe.primetime.adde.input.InputData;
import com.espertech.esper.client.EPServiceProvider;
import com.espertech.esper.client.EPStatement;
import com.espertech.esper.client.soda.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Represents a rule in a proper format that is accepted by ESPER Rule Engine.
 * It uses the fields from RuleJson/RuleModel in order to properly convert to the new format.
 */
public class RuleData {
    private static final Logger LOG = LoggerFactory.getLogger(RuleData.class);

    private RuleJson ruleJson;
    private RuleModel ruleModel;
    private SelectClause selectClause;
    private FromClause fromClause;
    private Expression whereClauseExpr;
    private Map<String,InputData> inputMap;

    public RuleData(Map<String,InputData> inputMap, RuleJson ruleJson){
        this.inputMap = inputMap;
        this.ruleJson = ruleJson;

        createFromClause(ruleJson.getInputDomains());
        createSelectClause(ruleJson.getActors(),ruleJson.getInputDomains());
        createWhereClause(ruleJson.getCondition());
    }

    public RuleData(Map<String,InputData> inputMap, RuleModel ruleModel){
        this.inputMap = inputMap;
        this.ruleModel = ruleModel;

        createFromClause(ruleModel.getInputDomains());
        createSelectClause(ruleModel.getActors(),ruleModel.getInputDomains());
        createWhereClause(ruleModel.getCondition());
    }


    public String getRuleID(){
        if (ruleJson != null){
            return ruleJson.getRuleID();
        }
        else if (ruleModel != null){
            return ruleModel.getRuleID();
        }

        return null;
    }

    public List<String> getInputDomains() {
        if (ruleJson != null){
            return ruleJson.getInputDomains();
        }
        else if (ruleModel != null){
            return ruleModel.getInputDomains();
        }

        return null;
    }

    public List<String> getActions() {
        if (ruleJson != null){
            return ruleJson.getActions();
        }
        else if (ruleModel != null){
            return ruleModel.getActions();
        }

        return null;
    }

    public EPStatement createAndAddStatementToEsper(EPServiceProvider epService){
        EPStatementObjectModel model = new EPStatementObjectModel();
        model.setSelectClause(selectClause);
        model.setFromClause(fromClause);
        model.setWhereClause(whereClauseExpr);
        EPStatement statement = epService.getEPAdministrator().create(model,getRuleID());
        LOG.info("Created query: {}", statement.getText());
        return statement;
    }

    private void createFromClause(List<String>inputDomains) {
        if (inputDomains.size() == 0){
            throw new RuleException(
                    getRuleID() + ": No input domains were inserted."
            );
        }

        // Sanity check inputDomains
        for (String inputDomain : inputDomains){
            if (!inputMap.containsKey(inputDomain)){
                throw new RuleException(
                        getRuleID() + ": There is no input with ID: '" + inputDomain + "'."
                );
            }
        }
        // From clause
        fromClause = FromClause.create();
        for (String inputDomain : inputDomains){
            fromClause.add(FilterStream.create(inputDomain));
        }
    }

    private void createSelectClause(List<String> actors, List<String>inputDomains) {
        // Actors sanity check
        for (String actor : actors){
            for (String inputDomain : inputDomains){
                if (!inputMap.get(inputDomain).getTypeMap().containsKey(actor)){
                    throw new RuleException(
                            getRuleID() + ": Actor '" + actor + "' does not exist. It must be defined as a data field in " +
                                    "an input domain."
                    );
                }
            }
        }

        // Select clause
        selectClause = SelectClause.create();
        for (String actor : actors){
            selectClause.add(actor);
        }
    }

    private void createWhereClause(String condition) {
        String postFixCondition = convertToPostFixFormat(condition);
        whereClauseExpr = createEplExpression(postFixCondition);
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
                    throw new RuleException(
                            getRuleID() + ": Failed to parse condition field. Invalid format."
                    );
                }

                String operator = operatorStack.pop();
                while (!operator.equals("(")){
                    if (operatorStack.isEmpty()){
                        throw new RuleException(
                                getRuleID() + ": Failed to parse condition field. Invalid format."
                        );
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
                    // There should always be an operator (>,<,= etc.) in the stack
                    // when meeting and/or.
                    throw new RuleException(
                            getRuleID() + ": Failed to parse condition field. Invalid format."
                    );
                }

                postFix.append(operator);
                postFix.append(' ');

                operatorStack.push(token);
                continue;
            }

            //Check if token is a valid operand.
            if (!isNumeric(token)){
                for (String inputDomain : getInputDomains()){
                    if (!inputMap.get(inputDomain).getTypeMap().containsKey(token)){
                        throw new RuleException(
                                getRuleID() + ": Operand '" + token + "' is invalid. It is not a number or a data field in an" +
                                        "input domain."
                        );
                    }
                }
            }


            postFix.append(token);
            postFix.append(" ");
        }

        while(!operatorStack.isEmpty()){
            String operator = operatorStack.pop();

            if (operator.equals("(")){
                throw new RuleException(
                        getRuleID() + ": Failed to parse condition field. Invalid format."
                );
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
                        throw new RuleException(
                                getRuleID() + ": Invalid condition expression. Types are different for '" + op1 +"' and '"
                                        + op2 + "'."
                        );
                    }
                }
                else{
                    if (propertyType1 == null && isNumeric(op1)){
                        Object valueOp1 = Utils.castToType(op1, propertyType2);

                        if (valueOp1 == null){
                            throw new RuleException(
                                    getRuleID() + ": Invalid condition expression. Operator '" + op1 +"' "
                                           + "' can not be cast to required property type."
                            );
                        }
                        else{
                            expressionStack.push(getExprForPropWithValue(token,op2,valueOp1));
                        }
                    }
                    else{
                        if (propertyType2 == null && isNumeric(op2)){
                            Object valueOp2 = Utils.castToType(op2, propertyType1);
                            if (valueOp2 == null){
                                throw new RuleException(
                                        getRuleID() + ": Invalid condition expression. Operator '" + op2 +"' "
                                                + "' can not be cast to required property type."
                                );
                            }
                            else{
                                expressionStack.push(getExprForPropWithValue(token,op1,valueOp2));
                            }
                        }
                        else{
                            throw new RuleException(
                                    getRuleID() + ": Invalid condition expression. Both operators '" + op1 +"' and '"
                                            + op2 + "' are not properties or numeric values."
                            );
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
            throw new RuleException(
                    getRuleID() + ": Failed to convert condition string to proper format. Internal error."
            );
        }

        return (Expression) expressionStack.pop();
    }

    // The method checks if str is a property. If yes, it will return the property type else null.
    private Object getPropertyType(String str){
        for (String inputDomain : getInputDomains()){
            InputData input = inputMap.get(inputDomain);
            if (input.getTypeMap().containsKey(str)){
                return input.getTypeMap().get(str);
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
}
