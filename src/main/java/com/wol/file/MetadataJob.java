package com.wol.file;


import com.wol.annotations.Mandatory;
import com.wol.file.dto.GitInfo;
import com.wol.file.dto.JarInfo;
import com.wol.json.JsonComparator;
import com.wol.reporter.ReporterAdoc;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;



public class MetadataJob {

    public static final String ERROR_MESSAGE = "%s release %s doesnt contain metadata. no report created";
    JarExtractor jarExtractor;
    GitExtractor gitExtractor;
    Log logger;
    public static final String UNKNOWN = "unknown";

    private MetadataJob(
            String fileName, String baseDirName, String techDirName,
            String releaseRegex, File target, MavenProject project, Log logger
    ) {
        jarExtractor = new JarExtractor(fileName, baseDirName, techDirName, target, project, logger);
        gitExtractor = new GitExtractor(fileName, baseDirName, techDirName, releaseRegex, logger);
        this.logger = logger;

    }

    public void doJob(JsonComparator comparator, ReporterAdoc reporter) {
        JarInfo jarInfo = jarExtractor.extractFiles();
        GitInfo gitInfo = gitExtractor.extractFiles();
        if (gitInfo.valid() && jarInfo.valid()) {
            JsonComparator.DiffSets namesDiffSet = comparator.differenceNames(jarInfo.files(), gitInfo.files(), logger);
            Map<String, String> defValuesDiffMap = comparator.differenceValues(jarInfo.files(), gitInfo.files(), logger);
            String current = Objects.requireNonNullElse(gitInfo.branch().lastRelease(), UNKNOWN);
            String before = Objects.requireNonNullElse(gitInfo.branch().beforeLast(), UNKNOWN);
            reporter.generate(namesDiffSet.current(), namesDiffSet.before(), defValuesDiffMap, current, before);
            cleanStage(gitInfo.files());
        }
        else errorMessageLog(jarInfo, gitInfo);
    }

    private void errorMessageLog(JarInfo jarInfo, GitInfo gitInfo) {
        if (!jarInfo.valid()) {
            String errorText = String.format(ERROR_MESSAGE, "current",
                    Objects.requireNonNullElse(gitInfo.branch().lastRelease(), ""));
            logger.info(errorText);
        }
        if (!gitInfo.valid()) {
            String errorText = String.format(ERROR_MESSAGE, "previous",
                    Objects.requireNonNullElse(gitInfo.branch().beforeLast(), ""));
            logger.info(errorText);
        }
    }

    private void cleanStage(List<Path> filesToClean) {
        // clean files extracted current git
        filesToClean.stream().forEach(it -> {
            try {
                Files.deleteIfExists(it);
            } catch (IOException e) {
                logger.error("error while deleting technical files");
            }
        });
    }



    public static FEBuilder builder(){
        return new FEBuilder();
    }

    public static class FEBuilder {
        private FEBuilder(){}

        public FEBuilder releaseRegex(String r) {
            this.relR = r;
            return this;
        }

        public FEBuilder baselDir(String r) {
            this.baseD = r;
            return this;
        }

        public FEBuilder releaseDir(String r) {
            this.techD = r;
            return this;
        }

        public FEBuilder logger(Log r) {
            this.log = r;
            return this;
        }

        public FEBuilder fileName(String r) {
            this.fileN = r;
            return this;
        }

        public FEBuilder project(MavenProject r) {
            this.project = r;
            return this;
        }

        public FEBuilder target(File r) {
            this.target = r;
            return this;
        }

        private String relR;
        private String baseD;
        private String techD;
        private Log log;
        private String fileN;
        private MavenProject project;
        private File target;

        static final String RELEASE_REGEX = "release-(?<number>(\\d+\\.*)+)[^\\d\\.]*.*";
        static final String TECH_DIR_NAME = "properties_history";

        public MetadataJob build(){
            if (fileN == null) throw new IllegalStateException("filename is null");
            if (baseD == null) throw new IllegalStateException("dirname is null");
            if (log == null)   throw new IllegalStateException("logger is null");
            if (project == null)   throw new IllegalStateException("project is null");
            if (target == null)   throw new IllegalStateException("target is null");
            if (techD == null) techD = TECH_DIR_NAME;
            if (relR  == null) relR = RELEASE_REGEX;

            return new MetadataJob(fileN, baseD, techD, relR, target, project, log);
        }

    }
}
