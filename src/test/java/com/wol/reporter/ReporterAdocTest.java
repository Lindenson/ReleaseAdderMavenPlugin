package com.wol.reporter;

import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;
import static com.wol.Plugin.REPORT_ADOC;
import static com.wol.Plugin.TEMPLATE_ADOC;
import static org.junit.jupiter.api.Assertions.*;

class ReporterAdocTest {

    private static final String SHOULD_BE = "****Properties versioning maven plugin version 1.1[discrete]== [navy]" +
            "#Spring Configuration Properties#[discrete]==== added to 2 and removed from 1****=== [navy]" +
            "#Properties added#* prop1** pr1=== [navy]" +
            "#Properties removed#* prop2** p2====  [teal]#Report generated at";

    @Test
    void failsIfTemplateIsMissing(){
        Log logger = new DefaultLog(new ConsoleLogger());
        Path path = Paths.get("");
        assertThrows(IllegalStateException.class, () -> new ReporterAdoc("123", path, logger));
    }

    @Test
    void canGenerateAPartialReport() throws IOException {
        Log logger = new DefaultLog(new ConsoleLogger());
        ReporterAdoc reporterAdoc = new ReporterAdoc(TEMPLATE_ADOC, Paths.get("", REPORT_ADOC), logger);
        reporterAdoc.generate(Set.of("prop1.pr1"), Set.of("prop2.p2"), "2", "1");
        String report = Files.readAllLines(Paths.get(REPORT_ADOC)).stream().collect(Collectors.joining(""));
        assertTrue(report.contains(SHOULD_BE));
    }
}