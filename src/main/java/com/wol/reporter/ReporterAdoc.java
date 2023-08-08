package com.wol.reporter;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;
import com.wol.reporter.strategies.AdocMultiLevelMap;
import com.wol.reporter.strategies.AdocSimpleSet;
import com.wol.reporter.strategies.AdocSimpleMap;
import com.wol.reporter.strategies.ReportStyles;
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
import java.util.stream.Collectors;

public class ReporterAdoc implements Reporter {
    public static final String PLUGIN_VERSION = "2.0";
    private final List<String> templateStrings;
    private final Path destination;
    private final Log logger;
    private final String timeNow;
    private ReportStyles<Set<String>> reportAddedRemovedStrategy;
    private ReportStyles<Map<String, String>> reportDefaultStrategy;



    public ReporterAdoc(String templateFile, Path destination, ReportStyles.Style style, Log logger) {
        this.destination = destination;
        this.logger = logger;
        this.timeNow = LocalDateTime.now().format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM));

        try (InputStream in = getClass().getResourceAsStream(templateFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            templateStrings = reader.lines().toList();
            this.reportAddedRemovedStrategy = switch (style) {
                case TREE ->  new AdocMultiLevelMap();
                default -> new AdocSimpleSet();

            };
            this.reportDefaultStrategy = new AdocSimpleMap();
        } catch (Exception e) {
            logger.error("error creating report");
            logger.error(e.getMessage());
            throw new IllegalStateException();
        }
    }

    @Override
    public void generate(Set<String> added,
                         Set<String> removed,
                         Map<String, String> defaultsChanged,
                         String currentBranch,
                         String beforeBranch) {
        if (added == null || removed == null || currentBranch == null || beforeBranch == null || defaultsChanged == null) return;
        logger.info(String.format("Creating report comparing releases between %s, before %s", currentBranch, beforeBranch));
        try {
            applyTemplate(currentBranch, beforeBranch, added, removed, defaultsChanged);
        } catch (Exception e) {
            logger.error("error creating report");
            logger.error(e.getMessage());
        }
    }

    private void applyTemplate(String currentBranch, String beforeBranch,
                                Set<String> propsAdded,
                                Set<String> propsRemoved,
                                Map<String, String> defValuesChanged
                              ) throws IOException
    {
        String added = reportAddedRemovedStrategy.prettyPrint(propsAdded).toString();
        String removed = reportAddedRemovedStrategy.prettyPrint(propsRemoved).toString();

        Handlebars handlebars = new Handlebars();
        Template template = handlebars.compileInline(this.templateStrings.stream().collect(Collectors.joining("\n")));

        String defaults = reportDefaultStrategy.prettyPrint(defValuesChanged).toString();
        String attributes = defValuesChanged.isEmpty()? "" : ":defaults:";

        Files.writeString(destination, template.apply(new TemplateDto(
                PLUGIN_VERSION,
                currentBranch,
                beforeBranch,
                added,
                removed,
                defaults,
                timeNow,
                attributes)
        ));
    }


    private record TemplateDto(String version,
                               String current,
                               String before,
                               String added,
                               String removed,
                               String defaults,
                               String date,
                               String attributes
    ){}
}
