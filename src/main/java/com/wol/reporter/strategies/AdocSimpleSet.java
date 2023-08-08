package com.wol.reporter.strategies;

import java.util.Set;

public class AdocSimpleSet implements ReportStyles<Set<String>> {
    public StringBuilder prettyPrint(Set<String> result) {
        StringBuilder stringBuilder = new StringBuilder();
        result.stream().forEach(s -> {
            stringBuilder.append(" * ");
            stringBuilder.append(s);
            stringBuilder.append("\n");
        });
        return stringBuilder;
    }

}
