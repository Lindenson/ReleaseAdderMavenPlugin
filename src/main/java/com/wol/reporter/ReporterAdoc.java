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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class ReporterAdoc {
    public static final String PLUGIN_VERSION = "2.0";
    private List<String> templateStrings;
    private final Path destination;
    private final Log logger;
    private AdocMultyLevelList grouper;
    private AtomicReference<Object> resultAdded = new AtomicReference<>(null);
    private AtomicReference<Object> resultRemoved = new AtomicReference<>(null);


    public ReporterAdoc(String templateFile, Path destination, Log logger) {
        this.destination = destination;
        this.logger = logger;
        this.grouper = new AdocMultyLevelList();

        try (InputStream in = getClass().getResourceAsStream(templateFile);
             BufferedReader reader = new BufferedReader(new InputStreamReader(in)))
        { templateStrings = reader.lines().toList();
        } catch (Exception e) {
            logger.error("error creating report");
            logger.error(e.getMessage());
            throw new IllegalStateException();
        }
    }

    public void generate(Set<String> added, Set<String> removed, Map<String, String> defaultsChanged, String current, String before) {
        if (added == null || removed == null || current == null || before == null || defaultsChanged == null) return;

        logger.info(String.format("Creating report comparing releases between %s, before %s", current, before));

        try {
            removed.stream().forEach( it -> resultRemoved.set(grouper.makeMultiLevelList(it, resultRemoved.get())));
            added.stream().forEach( it -> resultAdded.set(grouper.makeMultiLevelList(it,  resultAdded.get())));
            applyTemplate(current,
                          before,
                          (Map<String, List<Object>>) resultAdded.get(),
                          (Map<String, List<Object>>) resultRemoved.get(),
                          defaultsChanged
            );
        } catch (Exception e) {
            logger.error("error creating report");
            logger.error(e.getMessage());
        }
    }

    private void applyTemplate(String current, String before,
                                Map<String, List<Object>> propsAdded,
                                Map<String, List<Object>> propsRemoved,
                                Map<String, String> defValuesChanged
                              ) throws IOException
    {
        StringBuilder added = grouper.prettyPrintMultilevelList(propsAdded);
        StringBuilder removed = grouper.prettyPrintMultilevelList(propsRemoved);

        Handlebars handlebars = new Handlebars();
        Template template = handlebars.compileInline(this.templateStrings.stream().collect(Collectors.joining("\n")));
        Files.writeString(destination, template.apply(new TemplateDto(
                PLUGIN_VERSION,
                current,
                before,
                added.toString(),
                removed.toString(),
                AdocSimpleList.prettyPrint(defValuesChanged),
                LocalDateTime.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)))
        ));
    }


    private record TemplateDto(String version,
                               String current,
                               String before,
                               String added,
                               String removed,
                               String defaults,
                               String date){}
}
