package com.wol.reporter;

import java.util.Map;

public class AdocSimpleList {
    public static String prettyPrint(Map<String, String> result) {
        StringBuilder stringBuilder = new StringBuilder();
        result.keySet().stream().forEach(s -> {
            stringBuilder.append(" * ");
            stringBuilder.append(s);
            stringBuilder.append(" = ");
            stringBuilder.append(result.get(s));
            stringBuilder.append("\n");
        });
        return stringBuilder.toString();
    }

}
