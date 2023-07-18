package com.wol.reporter;

import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;
import static com.wol.AdderPlugin.REPORT_ADOC;
import static com.wol.AdderPlugin.TEMPLATE_ADOC;
import static org.junit.jupiter.api.Assertions.*;

class ReporterAdocTest {

    @Test
    public void failsIfTemplateIsMissing(){
        Log logger = new DefaultLog(new ConsoleLogger());;
        assertThrows(IllegalStateException.class, () -> new ReporterAdoc("123", Paths.get(""), logger));
    }

    @Test
    public void canGenerateAPartialReport() throws IOException {
        Log logger = new DefaultLog(new ConsoleLogger());;
        ReporterAdoc reporterAdoc = new ReporterAdoc(TEMPLATE_ADOC, Paths.get("", REPORT_ADOC), logger);
        reporterAdoc.generate(Set.of("prop1.pr1"), Set.of("prop2.p2"), "1", "2");
        String strings2 = Files.readAllLines(Paths.get("release-adder-report.adoc")).stream().collect(Collectors.joining(""));
        String strings1 = "= Release Adder Plugin Report== Created for release 1== Properties added* prop1** pr1== Properties removed* prop2** p2";
        assertEquals(strings1, strings2);
    }

}