package com.adobe.primetime.adde.input;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by ramascan on 20/03/15.
 * Contains the input defined in the configuration file.
 */
public class InputData {
    private String inputID;
    private Map<String,Object> typeMap = new HashMap();

    public String getInputID() {
        return inputID;
    }

    public void setInputID(String inputID) {
        this.inputID = inputID;
    }

    public Map<String,Object> getTypeMap() {
        return typeMap;
    }

    public void addDataType(String dataName, Object dataType){
        typeMap.put(dataName,dataType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o instanceof String){
            System.out.println(o);
            return inputID.equals((String) o);
        }

        if (!(o instanceof InputData)) return false;

        InputData inputData = (InputData) o;

        if (!inputID.equals(inputData.inputID)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return inputID.hashCode();
    }
}
