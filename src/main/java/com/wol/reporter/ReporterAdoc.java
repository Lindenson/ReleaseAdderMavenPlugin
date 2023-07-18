package com.wol.reporter;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import org.apache.maven.plugin.logging.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ReporterAdoc {
    private List<String> template;
    private final Path destination;
    private final Log logger;
    private Grouper grouper;

    public ReporterAdoc(String templateFile, Path destination, Log logger) {
        this.destination = destination;
        this.logger = logger;
        this.grouper = new Grouper(logger);

        try (InputStream in = getClass().getResourceAsStream(templateFile);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in)))
        { template = reader.lines().toList();
        } catch (Exception e) {
            logger.error("error creating report");
            logger.error(e.getMessage());
            throw new IllegalStateException();
        }
    }

    public void generate(Set<String> added, Set<String> removed, String from, String to) {
        if (added == null || removed == null || from == null || to == null) return;

        logger.info(String.format("Creating report comparing releases between %s, to %s", from, to));

        try {
            AtomicReference<Object> resultRemoved = new AtomicReference<>(null);
            removed.stream().forEach( it -> resultRemoved.set(grouper.makeGroups(it, resultRemoved.get())));

            AtomicReference<Object> resultAdded = new AtomicReference<>(null);
            added.stream().forEach( it -> resultAdded.set(grouper.makeGroups(it,  resultAdded.get())));

            Map<String, List<Object>> resultMapAdded = (Map<String, List<Object>>) resultAdded.get();
            Map<String, List<Object>> resultMapRemoved = (Map<String, List<Object>>) resultRemoved.get();

            applyTemplate(from, resultMapAdded, resultMapRemoved);
        } catch (Exception e) {
            logger.error("error creating report");
        }
    }

    private void applyTemplate(String from, Map<String, List<Object>> resultMapAdded, Map<String, List<Object>> resultMapRemoved) throws IOException {
        StringBuilder stringBuilderAdded = grouper.printUngrouped(resultMapAdded, new StringBuilder(), 1);
        StringBuilder stringBuilderRemoved = grouper.printUngrouped(resultMapRemoved, new StringBuilder(), 1);

        Handlebars handlebars = new Handlebars();
        Template template = handlebars.compileInline(this.template.stream().collect(Collectors.joining("\n")));
        String report = template.apply(new TemplateDto(from, stringBuilderAdded.toString(), stringBuilderRemoved.toString()));
        Files.writeString(destination, report);
    }

    private record TemplateDto(String re, String ad, String ar){};
}
