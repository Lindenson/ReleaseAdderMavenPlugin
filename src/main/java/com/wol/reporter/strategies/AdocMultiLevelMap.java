package com.wol.reporter.strategies;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class AdocMultiLevelMap implements ReportStyles<Set<String>> {

    private String root = UUID.randomUUID().toString();

    @Override
    public StringBuilder prettyPrint(Set<String> source) {
        AtomicReference<Object> accumulator = new AtomicReference<>(null);
        source.stream().forEach( it -> accumulator.set(makeMultiLevelMap(it, accumulator.get())));
        StringBuilder stringBuilder = new StringBuilder();
        printUngrouped((Map<String, List<Object>>) accumulator.get(), stringBuilder, 0);
        return stringBuilder;
    }

    private Object makeMultiLevelMap(String source, Object result) {
        return makeMapGroups(root + '.' + source, (Map<String, List<Object>>) result);
    }

    private Object makeMapGroups(String source, Map<String, List<Object>> result) {
        result = Objects.requireNonNullElse(result, new TreeMap<>());
        int i = source.indexOf('.');
            if (i > 0) {
                String left = source.substring(0, i);
                List<Object> listProps = result.computeIfAbsent(left, ix -> new ArrayList<>());
                List<Object> newLeaves = listProps.stream()
                        .filter(it -> !(it instanceof Map))
                        .collect(Collectors.toList());

                List<Object> newNodes = listProps.stream()
                        .filter(Map.class::isInstance)
                        .map(Map.class::cast)
                        .map(it -> {
                            Object res = makeMapGroups(source.substring(i + 1), it);
                            if (res instanceof String) {
                                newLeaves.add(res);
                                return it;
                            }
                            return res;
                        })
                        .collect(Collectors.toList());

                if (newNodes.isEmpty()) newNodes.add(makeMapGroups(source.substring(i + 1), null));

                newNodes.addAll(newLeaves);
                result.put(left, newNodes);
                return result;
            }
        return source;
    }

    private void printUngrouped(Map<String, List<Object>> result, StringBuilder stringBuilder, int level) {
        if (Objects.isNull(result)) return;
        result.entrySet().stream().forEach(it -> {
             if (level > 0) stringBuilder.append(String.format("%s %s%n", "*".repeat(level), it.getKey()));
             List<Object> list = it.getValue();
             list.sort(Comparator.comparing(Object::toString));
             list.stream().forEach(ix -> {
                 if (ix instanceof String) stringBuilder.append(String.format("%s %s%n", "*".repeat(level + 1), ix));
                 else printUngrouped((Map<String, List<Object>>)ix, stringBuilder, level + 1);
             });
        });
    }
}
