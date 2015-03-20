import java.util.HashSet;
import java.util.Set;

/**
 * Created by ramascan on 20/03/15.
 */
public class InputData {
    private String inputID;
    private Set<DataType> typeSet = new HashSet<DataType>();

    public String getInputID() {
        return inputID;
    }

    public void setInputID(String inputID) {
        this.inputID = inputID;
    }

    public Set<DataType> getTypeSet() {
        return typeSet;
    }

    public void addDataType(DataType dataType){
        typeSet.add(dataType);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
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
