package com.wol.reporter;

import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import static com.wol.Plugin.REPORT_ADOC;
import static com.wol.Plugin.TEMPLATE_ADOC;
import static com.wol.reporter.strategies.ReportStyles.styleFrom;
import static org.junit.jupiter.api.Assertions.*;

class ReporterAdocTest {

    private static final String SHOULD_BE_TREE = "****Properties versioning maven plugin version 2.0[discrete]== " +
            "[navy]#Spring Configuration Properties#[discrete]==== added to 2.0 and removed from 1.9****:defaults:=== " +
            "[navy]#Properties added:#* prop1** pr1** pr2=== [navy]#Properties removed:#* prop2** pr1** pr2ifdef::defaults[]=== " +
            "[navy]#Default values changed:# * prop3.pr3 &#x3D; 100endif::[]include::properties-mandatory.adoc[opts=optional]====  [teal]#";


    private static final String SHOULD_BE_LIST ="****Properties versioning maven plugin version 2.0[discrete]== " +
            "[navy]#Spring Configuration Properties#[discrete]==== added to 2.0 and removed from 1.9****:defaults:=== " +
            "[navy]#Properties added:# * prop1.pr1 * prop1.pr2=== [navy]#Properties removed:# * prop2.pr2 * prop2.pr1ifdef::defaults[]=== " +
            "[navy]#Default values changed:# * prop3.pr3 &#x3D; 100endif::[]include::properties-mandatory.adoc[opts=optional]====  [teal]#";


    private static final Path reportPath = Paths.get(REPORT_ADOC);

    @Test
    void failsIfTemplateIsMissing(){
        Log logger = new DefaultLog(new ConsoleLogger());
        Path path = Paths.get("");
        assertThrows(IllegalStateException.class, () -> new ReporterAdoc("123", path, styleFrom("LIST"), logger));
    }

    @Test
    void canGenerateTreeStyleReport() throws IOException {
        Log logger = new DefaultLog(new ConsoleLogger());
        Files.deleteIfExists(reportPath);
        Reporter reporter = new ReporterAdoc(TEMPLATE_ADOC, reportPath, styleFrom("TREE"), logger);
        reporter.generate(Set.of("prop1.pr1", "prop1.pr2"), Set.of("prop2.pr2", "prop2.pr1"), Map.of("prop3.pr3", "100"), "2.0", "1.9");
        String report = Files.readAllLines(Paths.get(REPORT_ADOC)).stream().collect(Collectors.joining(""));
        report = report.replaceAll("Report generated at \\w{2,4} \\d{1,2}, \\d{4}#", "");
        System.out.println(report);
        assertEquals(SHOULD_BE_TREE, report);
    }

    @Test
    void canGenerateListStyleReport() throws IOException {
        Log logger = new DefaultLog(new ConsoleLogger());
        Files.deleteIfExists(reportPath);
        Reporter reporter = new ReporterAdoc(TEMPLATE_ADOC, reportPath, styleFrom(null), logger);
        reporter.generate(Set.of("prop1.pr1", "prop1.pr2"), Set.of("prop2.pr2", "prop2.pr1"), Map.of("prop3.pr3", "100"), "2.0", "1.9");
        String report = Files.readAllLines(Paths.get(REPORT_ADOC)).stream().collect(Collectors.joining(""));
        report = report.replaceAll("Report generated at \\w{2,4} \\d{1,2}, \\d{4}#", "");
        System.out.println(report);
        assertEquals(SHOULD_BE_LIST, report);
    }
}