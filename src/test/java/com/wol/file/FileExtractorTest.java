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
    public void buildersFailsIfNotEnoughProperties() {
        FileExtractor.FEBuilder builder = FileExtractor.builder();
        assertThrows(IllegalStateException.class, () -> builder.build());
    }

    @Test
    public void buildersFailsIfWrongRegex() {
        FileExtractor.FEBuilder builder = FileExtractor.builder();
        builder.releaseRegex("//////////\\\\\\\\\\");
        assertThrows(IllegalStateException.class, () -> builder.build());
    }

    @Test
    public void buildersFailsIfWrongDir() {
        FileExtractor.FEBuilder builder = FileExtractor.builder();
        builder.baselDir("213235");
        assertThrows(IllegalStateException.class, () -> builder.build());
    }

    @Test
    public void notFailsIfJarFileIsWrongJustGivesEmptyResult() {
        FileExtractor fileExtractor = FileExtractor.builder()
                .baselDir("")
                .fileName("123")
                .logger(logger)
                .build();
        MavenProject mavenProject = new MavenProject();
        Artifact artifact = new DefaultArtifact("1", "1", "1", "1", "1", "1", null);
        artifact.setFile(Paths.get("additional-spring-configuration-metadata.json").toFile());
        mavenProject.setArtifact(artifact);
        List<Path> metadataFilesFromJar = fileExtractor.getMetadataFilesFromJar(Paths.get("src/main/resources").toFile(), mavenProject);
        assertEquals(0, metadataFilesFromJar.size());
    }

    @Test
    public void notFailsIfGitIsWrongJustGivesEmptyResult() {
        FileExtractor fileExtractor = FileExtractor.builder()
                .baselDir("234")
                .fileName("123")
                .logger(logger)
                .build();

        GitInfo metadataFilesFromGit = fileExtractor.getMetadataFilesFromGit();
        assertEquals(0, metadataFilesFromGit.files().size());
    }

    @Test
    public void orderBranches() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        TreeMap<Integer, Ref> testMap = new TreeMap<>();
        testMap.put(12121, new SymbolicRef(null, null));
        testMap.put(1, new SymbolicRef(null, null));
        testMap.put(12, new SymbolicRef(null, null));
        testMap.put(121, new SymbolicRef(null, null));
        testMap.put(1212, new SymbolicRef(null, null));

        TreeMap<Integer, Ref> branches = GitUtils.expandBranchNamesAndReorderBranches(testMap, logger);

        assertTrue(branches.size() == 5);
        assertTrue(branches.firstKey().equals(10000));
        assertTrue(branches.lastKey().equals(12121));
    }

    @Test
    public void lastAndBeforeBranches() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
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
    public void lastAndBeforeForSingle() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        TreeMap<Integer, Ref> testMap = new TreeMap<>();
        testMap.put(12121, new SymbolicRef("12121", null));

        BranchInfo branchInfo = GitUtils.currentAndPreviousReleaseInfo(testMap);
        assertEquals("12121", branchInfo.lastRelease());
        assertEquals(null, branchInfo.beforeLast());

    }

    @Test
    public void lastAndBeforeForEmpty() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        TreeMap<Integer, Ref> testMap = new TreeMap<>();

        BranchInfo branchInfo = GitUtils.currentAndPreviousReleaseInfo(testMap);
        assertEquals(null, branchInfo.lastRelease());
        assertEquals(null, branchInfo.beforeLast());

    }

    @Test
    public void orderProblematicBranchesLeadsToColision() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        TreeMap<Integer, Ref> testMap = new TreeMap<>();
        testMap.put(111, new SymbolicRef(null, null));
        testMap.put(1110, new SymbolicRef(null, null));


        TreeMap<Integer, Ref> branches = GitUtils.expandBranchNamesAndReorderBranches(testMap, logger);

        assertEquals(1, branches.size());
    }

    @Test
    public void releaseNamesAParsed() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        var pattern= Pattern.compile("release-(?<number>(\\d+\\.*)+)[^\\d\\.]*.*");
        TreeMap<Integer, String> testMap = new TreeMap<>();
        testMap.put(11, "release-1.1");
        testMap.put(121, "release-1.2.1-alpha");
        testMap.put(112, "release-1.12betta12");

        testMap.entrySet().stream().forEach(it -> {
            int number = GitUtils.releaseNameToNumber(pattern, it.getValue(), logger);
            if (!it.getKey().equals(number)) throw new AssertionError(String.format("%s not equals %s", it.getKey(), number));
        });
    }
}