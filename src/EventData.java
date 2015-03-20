import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by ramascan on 17/03/15.
 */
public class EventData {
    private HashSet<String> nameSet;
    private Map<String,String> dataMap;

    public Map<String, String> getDataMap() {
        return dataMap;
    }

    public EventData(){
        dataMap = new HashMap<String, String>();
        nameSet = new HashSet<String>();
    }

    public void addInputNameSet(HashSet<String> nameSet){
        this.nameSet = nameSet;
    }

    public void addInput(String dataName, String dataValue){
        if (dataMap.containsKey(dataName)){
            dataMap.put(dataName,dataValue);
        }
        else{
            if (nameSet.contains(dataName)){
                dataMap.put(dataName,dataValue);
            }
            else{
                // TODO: Throw error here
            }
        }
    }

    public String get(String key) {
        return (dataMap == null) ? null : dataMap.get(key);
    }
}
