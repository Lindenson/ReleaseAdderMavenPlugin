package com.wol.reporter;

import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class GrouperTest {

    Log logger = new DefaultLog(new ConsoleLogger());

    @Test
    void makeMapGroups() {
        AtomicReference<Object> result = new AtomicReference<>(null);
        Grouper grouper = new Grouper(logger);
        List<String> strings = List.of( "source.set1.subset1",
                                        "source.set2.subset1.susubset33",
                                        "source",
                                        "source.set2",
                                        "source.set2.subset1",
                                        "source.set2.subset2",
                                        "source.set2.subset3",
                                        "source.set3.subset4",
                                        "source.set1.subset1",
                                        "source.set1",
                                        "source1.set1.subset1",
                                        "source.set2.subset1.susubset22");
        strings.stream().forEach( it -> result.set(grouper.makeGroups(it, result.get())));
        System.out.println(result.get());
        assertEquals(2, ((Map<?, ?>) result.get()).size());
    }
}
