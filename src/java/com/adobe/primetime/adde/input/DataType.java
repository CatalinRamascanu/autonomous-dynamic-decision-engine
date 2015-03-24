package com.adobe.primetime.adde.input;

/**
 * Created by ramascan on 20/03/15.
 * Represents an entry in the data-fields from the configuration file.
 */
public class DataType {
    private String name;
    private Object type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getType() {
        return type;
    }

    public void setType(Object type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DataType)) return false;

        DataType dataType = (DataType) o;

        if (!name.equals(dataType.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
