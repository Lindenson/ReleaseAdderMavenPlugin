package com.wol;

import com.wol.json.JsonComparator;
import com.wol.file.dto.GitInfo;
import com.wol.reporter.ReporterAdoc;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import com.wol.file.FileExtractor;



@Mojo( name = "add_version",  defaultPhase = LifecyclePhase.INSTALL)
public class AdderPlugin extends AbstractMojo
{

    public static final String REPORT_ADOC = "release-adder-report.adoc";
    private static final String METADATA_NAME = "spring-configuration-metadata.json";
    public static final String TEMPLATE_ADOC = "/template.adoc";
    public static final String UNKNOWN = "unknown";

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


    public void execute() throws MojoExecutionException
    {
        getLog().info( "Adding Release Version" );
        File basedir = project.getBasedir();

        FileExtractor fileExtractor = FileExtractor.builder()
                .baselDir(basedir.getAbsolutePath())
                .fileName(METADATA_NAME)
                .logger(getLog())
                .releaseRegex(regex)
                .releaseDir(folder)
                .build();

        List<Path> metadataFiles = fileExtractor.getMetadataFilesFromJar(target, project);

        GitInfo gitInfo = fileExtractor.getMetadataFilesFromGit();
        List<Path> gitFiles = gitInfo.files();

        // not to generate report if or git or current metadata is empty
        if (!gitInfo.valid()) return;
        if (metadataFiles.size() == 0) return;

        JsonComparator jsonComparator = new JsonComparator();
        JsonComparator.DiffSets diffSets = jsonComparator.differenceJSON(metadataFiles, gitFiles, getLog());

        String current = Optional.ofNullable(gitInfo.branch().lastRelease()).orElse(UNKNOWN);
        String before = Optional.ofNullable(gitInfo.branch().beforeLast()).orElse(UNKNOWN);

        ReporterAdoc reporterAdoc = new ReporterAdoc(TEMPLATE_ADOC, Paths.get(basedir.getPath(), REPORT_ADOC), getLog());
        reporterAdoc.generate(diffSets.from(), diffSets.to(), current, before);

        // clean files extracted from git
        gitFiles.stream().forEach(it -> it.toFile().delete());
    }
}
