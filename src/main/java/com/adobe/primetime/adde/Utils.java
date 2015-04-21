package com.adobe.primetime.adde;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.event.map.MapEventBean;
import com.google.api.client.util.Throwables;
import org.apache.commons.beanutils.BeanUtils;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.Function;

public class Utils {
    public static Object castToType(String str, Object type){
        if (type == String.class){
            return str;
        }

        if (type == Integer.class){
            return Integer.parseInt(str);
        }

        if (type == Long.class){
            return Long.parseLong(str);
        }

        if (type == Float.class){
            return Float.parseFloat(str);
        }

        if (type == Double.class){
            return Double.parseDouble(str);
        }

        return null;
    }

    public static class eventToMap implements Function<EventBean, Map<String, Object>> {
        @Nullable
        @Override
        @SuppressWarnings("unchecked")
        public Map<String, Object> apply(@Nullable EventBean input) {
            if (input == null) {
                return null;
            }
            if (input instanceof MapEventBean) {
                return ((MapEventBean)input).getProperties();
            }
            try {
                return (Map<String, Object>) BeanUtils.describe(input);
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        }
    }

    public static final Function<EventBean, Map<String, Object>> EVENT_TO_MAP = new eventToMap();

    public static Map<String, Object> getActorMap(EventBean eventBean) {
        return EVENT_TO_MAP.apply(eventBean);
    }
}
