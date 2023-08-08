package com.wol.reporter.strategies;
import java.util.Map;

public class AdocSimpleMap implements ReportStyles<Map<String, String>> {
    public StringBuilder prettyPrint(Map<String, String> result) {
        StringBuilder stringBuilder = new StringBuilder();
        result.keySet().stream().forEach(s -> {
            stringBuilder.append(" * ");
            stringBuilder.append(s);
            stringBuilder.append(" = ");
            stringBuilder.append(result.get(s));
            stringBuilder.append("\n");
        });
        return stringBuilder;
    }

}
