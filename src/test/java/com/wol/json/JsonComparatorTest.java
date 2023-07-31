package com.wol.json;


import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class JsonComparatorTest {

    @Test
    void noFailIfFileAbsentJustEmptyResult() {
        Log logger = new DefaultLog(new ConsoleLogger());
        JsonComparator jsonComparator = new JsonComparator();
        JsonComparator.DiffSets diffSets = jsonComparator.differenceNames(List.of(Paths.get("1")), List.of(Paths.get("1")), logger);
        Set<String> from = diffSets.current();
        Set<String> to = diffSets.before();
        assertEquals(0, from.size());
        assertEquals(0, to.size());
    }


    @Test
    void noFailIfWrongJsonJustEmptyResult() {
        Log logger = new DefaultLog(new ConsoleLogger());
        JsonComparator jsonComparator = new JsonComparator();
        JsonComparator.DiffSets diffSets = jsonComparator
                .differenceNames(List.of(Paths.get("src/test/resources/1/spring-configuration-metadata.json")),
                        List.of(Paths.get("src/main/resources/template.adoc")), logger);
        Set<String> from = diffSets.current();
        Set<String> to = diffSets.before();
        assertEquals(0, from.size());
        assertEquals(0, to.size());
    }

    @Test
    void comparePropertiesNamesCorrect() {
        Log logger = new DefaultLog(new ConsoleLogger());
        JsonComparator jsonComparator = new JsonComparator();
        List<Path> list1 = List.of(Paths.get("src/test/resources/1/additional-spring-configuration-metadata.json"),
                Paths.get("src/test/resources/1/spring-configuration-metadata.json"));
        List<Path> list2 = List.of(Paths.get("src/test/resources/2/additional-spring-configuration-metadata.json"),
                Paths.get("src/test/resources/2/spring-configuration-metadata.json"));

        JsonComparator.DiffSets diffSets = jsonComparator
                .differenceNames(list1, list2, logger);
        Set<String> from = diffSets.current();
        Set<String> to = diffSets.before();
        System.out.println(from);
        System.out.println(to);
        assertEquals("server, server.var3.subvar1, server.var3.subvar2, server.var4.subvar1, server.var4.subvar2",
                from.stream().collect(Collectors.joining(", ")));
        assertEquals("server.host, server.var4.subvar3, server.var4.subvar4, server.var5.subvar1, server.var5.subvar2",
                to.stream().collect(Collectors.joining(", ")));

    }

    @Test
    void comparePropertiesDefaultsCorrect() {
        Log logger = new DefaultLog(new ConsoleLogger());
        JsonComparator jsonComparator = new JsonComparator();
        List<Path> list1 = List.of(Paths.get("src/test/resources/1/additional-spring-configuration-metadata.json"),
                Paths.get("src/test/resources/1/spring-configuration-metadata.json"));
        List<Path> list2 = List.of(Paths.get("src/test/resources/2/additional-spring-configuration-metadata.json"),
                Paths.get("src/test/resources/2/spring-configuration-metadata.json"));
        Map<String, String> res = jsonComparator.differenceValues(list1, list2, logger);
        assertEquals("11000", res.get("server.var1.subvar1"));
        assertEquals("10000", res.get("server.var1.subvar2"));

    }

}