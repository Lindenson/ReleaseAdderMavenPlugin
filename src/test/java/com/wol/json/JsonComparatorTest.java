package com.wol.json;


import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.junit.jupiter.api.Test;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class JsonComparatorTest {

    @Test
    public void noFailIfFileAbsentJustEmptyResult() {
        Log logger = new DefaultLog(new ConsoleLogger());
        JsonComparator jsonComparator = new JsonComparator();
        JsonComparator.DiffSets diffSets = jsonComparator.differenceJSON(List.of(Paths.get("1")), List.of(Paths.get("1")), logger);
        Set<String> from = diffSets.from();
        Set<String> to = diffSets.to();
        assertEquals(0, from.size());
        assertEquals(0, to.size());
    }


    @Test
    public void noFailIfWrongJsonJustEmptyResult() {
        Log logger = new DefaultLog(new ConsoleLogger());
        JsonComparator jsonComparator = new JsonComparator();
        JsonComparator.DiffSets diffSets = jsonComparator
                .differenceJSON(List.of(Paths.get("src/main/resources/additional-spring-configuration-metadata.json")),
                        List.of(Paths.get("src/main/resources/template.adoc")), logger);
        Set<String> from = diffSets.from();
        Set<String> to = diffSets.to();
        assertEquals(0, from.size());
        assertEquals(0, to.size());
    }

}