package com.wol.reporter;

import java.util.List;

public class AdocSimpleList {

    private AdocSimpleList() {}
    public static String prettyPrint(List<String> result) {
        StringBuilder stringBuilder = new StringBuilder();
        result.stream().forEach(s -> {
            stringBuilder.append(" * ");
            stringBuilder.append(s);
            stringBuilder.append("\n");
        });
        return stringBuilder.toString();
    }

}
