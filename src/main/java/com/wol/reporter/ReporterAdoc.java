package com.wol.reporter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.maven.plugin.logging.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class ReporterAdoc {
    private List<String> template;
    private final Path destination;
    private final Log logger;

    private final Function<String, String> applyTemplate;

    public ReporterAdoc(String templateFile, Path destination, Log logger) {
        this.destination = destination;
        this.logger = logger;
        this.applyTemplate = it -> it + "\n";

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

        AtomicReference<Object> resultRemoved = new AtomicReference<>(null);
        Grouper grouper = new Grouper(logger);
        removed.stream().forEach( it -> resultRemoved.set(grouper.makeGroups(it, (Map<String, List<Object>>) resultRemoved.get())));

        AtomicReference<Object> resultAdded = new AtomicReference<>(null);
        added.stream().forEach( it -> resultAdded.set(grouper.makeGroups(it, (Map<String, List<Object>>) resultAdded.get())));

        try {
            logger.info("=".repeat(100));
            logger.info("Removed: " + new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(resultRemoved.get()));
            logger.info("=".repeat(100));
            logger.info("Added: " + new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(resultAdded.get()));
            logger.info("=".repeat(100));
        } catch (JsonProcessingException e) {
           // nothings
        }

        try {
            Iterator<String> iA = added.iterator();
            Iterator<String> iB = removed.iterator();
            StringBuilder sb = new StringBuilder();
            useTemplate(sb);

            while (iA.hasNext() || iB.hasNext()) {
                boolean appended = false;
                sb.append("|");
                if (iA.hasNext()) {
                    sb.append(escapeSpecial(iA.next()));
                    appended = true;
                }
                sb.append("|");
                if (iB.hasNext()) {
                    appended = true;
                    sb.append(escapeSpecial(iB.next()));
                }
                sb.append("|");
                if (appended) sb.append(from);
            }
            Files.writeString(destination, sb.toString());
        } catch (IOException e) {
            logger.error("error creating report");
        }
    }

        private void useTemplate (StringBuilder sb){
            template.stream()
                    .map(applyTemplate)
                    .forEach(sb::append);
        }

        private String escapeSpecial (String source){
            return source.replace("|", "\\|");
        }

}
