package com.wol.file;

import com.wol.file.dto.BranchInfo;
import com.wol.file.dto.GitInfo;
import com.wol.utils.GitUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.monitor.logging.DefaultLog;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.SymbolicRef;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class FileExtractorTest {

    Log logger = new DefaultLog(new ConsoleLogger());

    @Test
    void buildersFailsIfNotEnoughProperties() {
        MetadataJob.FEBuilder builder = MetadataJob.builder();
        assertThrows(IllegalStateException.class, () -> builder.build());
    }

    @Test
    void buildersFailsIfWrongRegex() {
        MetadataJob.FEBuilder builder = MetadataJob.builder();
        builder.releaseRegex("//////////\\\\\\\\\\");
        assertThrows(IllegalStateException.class, () -> builder.build());
    }

    @Test
    void buildersFailsIfWrongDir() {
        MetadataJob.FEBuilder builder = MetadataJob.builder();
        builder.baselDir("213235");
        assertThrows(IllegalStateException.class, () -> builder.build());
    }

    @Test
    void notFailsIfJarFileIsWrongJustGivesEmptyResult() {
        Artifact artifact = new DefaultArtifact("1", "1", "1", "1", "1", "1", null);
        artifact.setFile(Paths.get("additional-spring-configuration-metadata.json").toFile());
        MavenProject mavenProject = new MavenProject();
        mavenProject.setArtifact(artifact);

        MetadataJob metadataJob = MetadataJob.builder()
                .baselDir("")
                .fileName("123")
                .project(mavenProject)
                .target(new File("1234"))
                .logger(logger)
                .build();

        List<Path> metadataFilesFromJar = metadataJob.jarExtractor.extractFiles().files();
        assertEquals(0, metadataFilesFromJar.size());
    }

    @Test
    void notFailsIfGitIsWrongJustGivesEmptyResult() {
        MetadataJob metadataJob = MetadataJob.builder()
                .baselDir("")
                .fileName("123")
                .logger(logger)
                .target(new File("1234"))
                .project(new MavenProject())
                .build();

        GitInfo metadataFilesFromGit = metadataJob.gitExtractor.extractFiles();
        assertEquals(0, metadataFilesFromGit.files().size());
    }

    @Test
    void orderBranches() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        TreeMap<Integer, Ref> testMap = new TreeMap<>();
        testMap.put(12121, new SymbolicRef(null, null));
        testMap.put(1, new SymbolicRef(null, null));
        testMap.put(12, new SymbolicRef(null, null));
        testMap.put(121, new SymbolicRef(null, null));
        testMap.put(1212, new SymbolicRef(null, null));

        TreeMap<Integer, Ref> branches = GitUtils.expandBranchNamesAndReorderBranches(testMap, logger);

        assertEquals(5, branches.size());
        assertEquals(10000, branches.firstKey());
        assertEquals(12121, branches.lastKey());
    }

    @Test
    void lastAndBeforeBranches() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        TreeMap<Integer, Ref> testMap = new TreeMap<>();
        testMap.put(12121, new SymbolicRef("12121", null));
        testMap.put(10000, new SymbolicRef("10000", null));
        testMap.put(12100, new SymbolicRef("12100", null));
        testMap.put(12110, new SymbolicRef("12110", null));
        testMap.put(12112, new SymbolicRef("12112", null));

        BranchInfo branchInfo = GitUtils.currentAndPreviousReleaseInfo(testMap);
        assertEquals("12121", branchInfo.lastRelease());
        assertEquals("12112", branchInfo.beforeLast());
        
    }


    @Test
    void lastAndBeforeForSingle() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        TreeMap<Integer, Ref> testMap = new TreeMap<>();
        testMap.put(12121, new SymbolicRef("12121", null));

        BranchInfo branchInfo = GitUtils.currentAndPreviousReleaseInfo(testMap);
        assertEquals("12121", branchInfo.lastRelease());
        assertEquals(null, branchInfo.beforeLast());

    }

    @Test
    void lastAndBeforeForEmpty() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        TreeMap<Integer, Ref> testMap = new TreeMap<>();

        BranchInfo branchInfo = GitUtils.currentAndPreviousReleaseInfo(testMap);
        assertEquals(null, branchInfo.lastRelease());
        assertEquals(null, branchInfo.beforeLast());

    }

    @Test
    void orderProblematicBranchesLeadsToColision() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        TreeMap<Integer, Ref> testMap = new TreeMap<>();
        testMap.put(111, new SymbolicRef(null, null));
        testMap.put(1110, new SymbolicRef(null, null));


        TreeMap<Integer, Ref> branches = GitUtils.expandBranchNamesAndReorderBranches(testMap, logger);

        assertEquals(1, branches.size());
    }

    @Test
    void releaseNamesAParsed() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        var pattern= Pattern.compile("release-(?<number>(\\d+\\.*)+)[^\\d\\.]*.*");
        TreeMap<Integer, String> testMap = new TreeMap<>();
        testMap.put(11, "release-1.1");
        testMap.put(121, "release-1.2.1-alpha");
        testMap.put(112, "release-1.12betta12");

        testMap.entrySet().stream().forEach(it -> {
            int number = GitUtils.releaseNameToNumber(pattern, it.getValue(), logger);
            assertEquals(number, it.getKey());
        });
    }
}