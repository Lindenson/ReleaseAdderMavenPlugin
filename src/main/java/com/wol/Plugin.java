package com.wol;

import com.wol.json.JsonComparator;
import com.wol.reporter.Reporter;
import com.wol.reporter.ReporterAdoc;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import java.io.File;
import java.nio.file.Paths;

import com.wol.file.MetadataJob;

import static com.wol.reporter.strategies.ReportStyles.styleFrom;


@Mojo( name = "add_version",  defaultPhase = LifecyclePhase.INSTALL)
public class Plugin extends AbstractMojo
{

    public static final String REPORT_ADOC = "properties-versioning-report.adoc";
    private static final String METADATA_NAME = "spring-configuration-metadata.json";
    public static final String TEMPLATE_ADOC = "/template.adoc";

    @Parameter( defaultValue = "${session}", readonly = true )
    private MavenSession session;

    @Parameter( defaultValue = "${project}", readonly = true )
    private MavenProject project;

    @Parameter( defaultValue = "${project.build.directory}", readonly = true )
    private File target;

    @Parameter( property = "regex")
    private String regex;

    @Parameter( property = "folder")
    private String folder;

    @Parameter( property = "style")
    private String style;


    public void execute() throws MojoExecutionException
    {
        getLog().info( "Adding Release Version" );
        String baseDir = project.getBasedir().getAbsolutePath();

        MetadataJob metadataJob = MetadataJob.builder()
                .project(project)
                .target(target)
                .baselDir(baseDir)
                .fileName(METADATA_NAME)
                .releaseRegex(regex)
                .releaseDir(folder)
                .logger(getLog())
                .build();

        JsonComparator jsonComparator = new JsonComparator();
        Reporter reporter = new ReporterAdoc(TEMPLATE_ADOC, Paths.get(baseDir, REPORT_ADOC), styleFrom(style), getLog());

        metadataJob.doJob(jsonComparator, reporter);
    }
}
