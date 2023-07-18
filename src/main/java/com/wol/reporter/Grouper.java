package com.wol.reporter;

import com.github.jknack.handlebars.Handlebars;
import org.apache.maven.plugin.logging.Log;

import java.util.*;
import java.util.stream.Collectors;

public class Grouper {

    private Log logger;
    public Grouper(Log logger){
        this.logger = logger;
    }

    public Object makeGroups(String source, Object result) {
        if (result instanceof String) {
            logger.error("should not be the 1st level leaf");
            return new TreeMap<>();
        }
        return makeMapGroups(source, (Map<String, List<Object>>) result);
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
                        .filter(it -> it instanceof Map)
                        .map(Map.class::cast)
                        .map(it -> {
                            Object res = makeMapGroups(source.substring(i + 1), it);
                            if (res instanceof String) {
                                newLeaves.add(res);
                                return it;
                            };
                            return res;
                        })
                        .collect(Collectors.toList());

                if (newNodes.size() == 0) newNodes.add(makeMapGroups(source.substring(i + 1), null));

                newNodes.addAll(newLeaves);
                result.put(left, newNodes);
                return result;
            }
        return source;
    }

    public StringBuilder printUngrouped(Map<String, List<Object>> result, StringBuilder stringBuilder, int level) {
         result.entrySet().stream().forEach(it -> {
             stringBuilder.append(String.format("%s %s\n", "*".repeat(level), it.getKey()));
             List<Object> list = it.getValue();
             list.sort(Comparator.comparing(it2 -> it2.toString()));
             list.stream().forEach(ix -> {
                 if (ix instanceof String) stringBuilder.append(String.format("%s %s\n", "*".repeat(level + 1), ix));
                 else printUngrouped((Map<String, List<Object>>)ix, stringBuilder, level + 1);
             });
         });
        return stringBuilder;
    }
}
