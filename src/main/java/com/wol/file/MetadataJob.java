package com.wol.file;


import com.wol.file.dto.GitInfo;
import com.wol.file.dto.JarInfo;
import com.wol.json.JsonComparator;
import com.wol.reporter.ReporterAdoc;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;



public class MetadataJob {

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
            JsonComparator.DiffSets diffSets = comparator.differenceJSON(jarInfo.files(), gitInfo.files(), logger);
            String current = Objects.requireNonNullElse(gitInfo.branch().lastRelease(), UNKNOWN);
            String before = Objects.requireNonNullElse(gitInfo.branch().beforeLast(), UNKNOWN);
            reporter.generate(diffSets.from(), diffSets.to(), current, before);
            cleanStage(gitInfo.files());
        }
    }

    private void cleanStage(List<Path> filesToClean) {
        // clean files extracted from git
        filesToClean.stream().forEach(it -> it.toFile().delete());
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

        private static String relR;
        private String baseD;
        private String techD;
        private Log log;
        private String fileN;
        private MavenProject project;
        private File target;

        static final String RELEASE_REGEX = "release-(?<number>(\\d+\\.*)+)[^\\d\\.]*.*";
        static final String TECH_DIR_NAME = "properties_history";

        public MetadataJob build(){
            if (log == null)   throw new IllegalStateException();
            if (baseD == null) throw new IllegalStateException();
            if (log == null)   throw new IllegalStateException();
            if (techD == null)  techD = TECH_DIR_NAME;
            if (relR  == null)  relR = RELEASE_REGEX;

            MetadataJob fileExtractor = new MetadataJob(fileN, baseD, techD, relR, target, project, log);
            return fileExtractor;
        }

    }
}
