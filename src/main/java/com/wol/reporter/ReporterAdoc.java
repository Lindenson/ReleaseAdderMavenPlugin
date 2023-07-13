package com.wol.reporter;

import org.apache.maven.plugin.logging.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class ReporterAdoc {
    private List<String> template;
    private final Path destination;
    private final Log logger;

    private final Function<String, String> templater;

    public ReporterAdoc(String templateFile, Path destination, Log logger) {
        this.destination = destination;
        this.logger = logger;
        this.templater = it -> it + "\n";

        try (InputStream in = getClass().getResourceAsStream(templateFile);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in)))
        { template = reader.lines().toList();
        } catch (Exception e) {
            logger.error("error creating report");
            logger.error(e.getMessage());
            throw new IllegalStateException();
        }
    }

    public void generate(Set<String> added, Set<String> remover, String from, String to) {
        if (added == null || remover == null || from == null || to == null) return;

        logger.info("Creating report");
        try {
            Iterator<String> iteratorA = added.iterator();
            Iterator<String> iteratorB = remover.iterator();
            StringBuilder sb = new StringBuilder();
            useTemplate(sb);

            while (iteratorA.hasNext() || iteratorB.hasNext()) {
                if (iteratorA.hasNext()) {
                    sb.append("|" + iteratorA.next() + "|" + from);
                } else {
                    sb.append("||");
                }
                if (iteratorB.hasNext()) {
                    sb.append("|" + iteratorB.next() + "|" + to);
                } else {
                    sb.append("||");
                }
            }
            Files.writeString(destination, sb.toString());
        } catch (IOException e) {
            logger.error("error creating report");
        }
    }

        private void useTemplate (StringBuilder sb){
            template.stream()
                    .map(templater)
                    .forEach(sb::append);
        }

}
