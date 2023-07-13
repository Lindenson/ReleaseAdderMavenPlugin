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
    private final ObjectMapper mapper = new ObjectMapper();


    private Set<String> parseJSONFile(Log logger, Path path) throws IOException {

        Set<String> propertyStings = new TreeSet<>();
        try {
            JsonNode jsonNode = mapper.readTree(path.toFile());
            JsonNode properties = jsonNode.get(PROPERTIES);
            Iterator<JsonNode> elements = properties.elements();
            while (elements.hasNext()) {
                JsonNode next = elements.next();
                String name = next.get(NAME).asText();
                propertyStings.add(name);
            }
            return propertyStings;
        } catch (IOException e) {
            logger.error("error in JsonComparator");
            logger.error(e.getMessage());
            throw e;
        }
    }

    public DiffSets differenceJSON(List<Path> p1, List<Path> p2, Log logger) {
        logger.info("Comparing JSON");
        Set<String> strings1 = new TreeSet<>();
        Set<String> strings2 = new TreeSet<>();
        Set<String> strings3 = new TreeSet<>();
        try {
            for (Path p : p1)  strings1.addAll(parseJSONFile(logger, p));
            for (Path p : p2)  strings2.addAll(parseJSONFile(logger, p));
            strings3.addAll(strings1);
            strings3.removeAll(strings2);
            strings2.removeAll(strings1);
        }
        catch (IOException e) {
            return new DiffSets(new TreeSet<>(), new TreeSet<>());
        }
        return new DiffSets(strings3, strings2);

    }
    public record DiffSets(Set<String> from, Set<String> to){}
}

