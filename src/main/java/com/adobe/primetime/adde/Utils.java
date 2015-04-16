package com.adobe.primetime.adde;

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
}
