package com.adobe.primetime.adde;

import com.espertech.esper.client.EventBean;
import com.espertech.esper.event.map.MapEventBean;
import com.google.api.client.util.Throwables;
import org.apache.commons.beanutils.BeanUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class Utils {
    public static Object castToType(String str, Object type) throws Exception {
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
        throw new Exception();
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
                // TODO: Strange error here. It did not appear until now.
//                return (Map<String, Object>) BeanUtils.describe(input);
                return null;
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        }
    }

    public static final Function<EventBean, Map<String, Object>> EVENT_TO_MAP = new eventToMap();

    public static Map<String, Object> getActorMap(EventBean eventBean) {
        return EVENT_TO_MAP.apply(eventBean);
    }

    @SuppressWarnings("unchecked")
    public static <T> T instantiate(Class<T> clazz, List<Object> args)
            throws ReflectiveOperationException {

        Object[] argValues = args.toArray();
        Constructor<T>[] constructors = (Constructor<T>[]) clazz.getConstructors();

        // Find the constructor & instantiate
        for (Constructor<T> constructor : constructors) {
            Class<?>[] types = constructor.getParameterTypes();
            if (types.length == args.size()) {
                for (int i = 0; i < types.length; i++) {
                    if (!types[i].isAssignableFrom(argValues[i].getClass())) {
                        break;
                    }
                }
                return constructor.newInstance(argValues);
            }
        }

        throw new ReflectiveOperationException();
    }
}
