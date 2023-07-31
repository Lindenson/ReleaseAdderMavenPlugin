package com.wol.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.maven.plugin.logging.Log;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class JsonComparator {

    public static final String PROPERTIES = "properties";
    public static final String NAME = "name";
    public static final String DEFAULT_VALUE = "defaultValue";
    private final ObjectMapper mapper = new ObjectMapper();


    private Set<String> parseJSONFileForNames(Path path) throws IOException {

        Set<String> propertyStings = new TreeSet<>();
        JsonNode jsonNode = mapper.readTree(path.toFile());
        JsonNode properties = jsonNode.get(PROPERTIES);
        if (Objects.isNull(properties)) return propertyStings;
        Iterator<JsonNode> elements = properties.elements();
        while (elements.hasNext()) {
            JsonNode next = elements.next();
            if (Objects.isNull(next.get(NAME))) continue;
            String name = next.get(NAME).asText();
            propertyStings.add(name);
        }
        return propertyStings;
    }

    private Map<String, String> parseJSONFileForDefaultValues(Path path) throws IOException {

        Map<String, String> defaults = new TreeMap<>();
        JsonNode jsonNode = mapper.readTree(path.toFile());
        JsonNode properties = jsonNode.get(PROPERTIES);
        if (Objects.isNull(properties)) return defaults;
        Iterator<JsonNode> elements = properties.elements();
        while (elements.hasNext()) {
            JsonNode next = elements.next();
            if (Objects.isNull(next.get(DEFAULT_VALUE))) continue;
            if (Objects.isNull(next.get(NAME))) continue;
            String dValue = next.get(DEFAULT_VALUE).asText();
            String name = next.get(NAME).asText();
            defaults.put(name, dValue);
        }
        return defaults;
    }

    public DiffSets differenceNames(List<Path> p1, List<Path> p2, Log logger) {
        logger.info("Comparing properties names");
        Set<String> strings1 = new TreeSet<>();
        Set<String> strings2 = new TreeSet<>();
        Set<String> strings3 = new TreeSet<>();
        try {
            for (Path p : p1)  strings1.addAll(parseJSONFileForNames(p));
            for (Path p : p2)  strings2.addAll(parseJSONFileForNames(p));
            strings3.addAll(strings1);
            strings3.removeAll(strings2);
            strings2.removeAll(strings1);
        }
        catch (Exception e) {
            logger.error("error while comparing JSONs");
            logger.error(e.getMessage());
            return new DiffSets(new TreeSet<>(), new TreeSet<>());
        }
        return new DiffSets(strings3, strings2);

    }


    public Map<String, String> differenceValues(List<Path> p1, List<Path> p2, Log logger) {
        logger.info("Comparing properties default values");
        Map<String, String> map1 = new TreeMap<>();
        Map<String, String> map2 = new TreeMap<>();
        Map<String, String> map3 = new TreeMap<>();
        try {
            for (Path p : p1)  map1.putAll(parseJSONFileForDefaultValues(p));
            for (Path p : p2)  map2.putAll(parseJSONFileForDefaultValues(p));
            Set<String> keys1 = new TreeSet<>();
            keys1.addAll(map1.keySet());
            keys1.retainAll(map2.keySet());
            for (String k : keys1) {
                if (Objects.isNull(map1.get(k))) continue;
                if (!map1.get(k).equals(map2.get(k))) map3.put(k, map1.get(k));
            }
        }
        catch (Exception e) {
            logger.error("error while comparing JSONs");
            logger.error(e.getMessage());
        }
        return map3;

    }


    public record DiffSets(Set<String> current, Set<String> before){}
}

