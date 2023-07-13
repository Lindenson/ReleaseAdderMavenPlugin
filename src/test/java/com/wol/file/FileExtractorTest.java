package com.wol.file;

import com.wol.file.dto.BranchInfo;
import com.wol.file.dto.GitInfo;
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
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

class FileExtractorTest {
    @Test
    public void buildersFailsIfNoProperties() {
        FileExtractor.FEBuilder builder = FileExtractor.builder();
        assertThrows(IllegalStateException.class, () -> builder.build());
    }

    @Test
    public void buildersFailsIfWrongPatter() {
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
        Log logger = new DefaultLog(new ConsoleLogger());
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
        Log logger = new DefaultLog(new ConsoleLogger());
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
        Log logger = new DefaultLog(new ConsoleLogger());
        FileExtractor fileExtractor = FileExtractor.builder()
                .baselDir("234")
                .fileName("123")
                .logger(logger)
                .build();

        TreeMap<Integer, Ref> testMap = new TreeMap<>();
        testMap.put(12121, new SymbolicRef(null, null));
        testMap.put(1, new SymbolicRef(null, null));
        testMap.put(12, new SymbolicRef(null, null));
        testMap.put(121, new SymbolicRef(null, null));
        testMap.put(1212, new SymbolicRef(null, null));

        Method method = FileExtractor.class.getDeclaredMethod("orderBranches", TreeMap.class);
        method.setAccessible(true);
        NavigableMap<Integer, Ref> testResult = (NavigableMap<Integer, Ref> ) method.invoke(fileExtractor, testMap);


        assertTrue(testResult.size() == 5);
        assertTrue(testResult.firstKey().equals(10000));
        assertTrue(testResult.lastKey().equals(12121));
    }

    @Test
    public void lastAndBeforeBranches() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Log logger = new DefaultLog(new ConsoleLogger());
        FileExtractor fileExtractor = FileExtractor.builder()
                .baselDir("234")
                .fileName("123")
                .logger(logger)
                .build();

        TreeMap<Integer, Ref> testMap = new TreeMap<>();
        testMap.put(12121, new SymbolicRef("12121", null));
        testMap.put(10000, new SymbolicRef("10000", null));
        testMap.put(12100, new SymbolicRef("12100", null));
        testMap.put(12110, new SymbolicRef("12110", null));
        testMap.put(12112, new SymbolicRef("12112", null));

        Method method = FileExtractor.class.getDeclaredMethod("lastAndPreviousRelease", TreeMap.class);
        method.setAccessible(true);
        BranchInfo testResult = (BranchInfo) method.invoke(fileExtractor, testMap);

        assertEquals("12121", testResult.lastRelease());
        assertEquals("12112", testResult.beforeLast());
        
    }


    @Test
    public void lastAndBeforeForSingle() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Log logger = new DefaultLog(new ConsoleLogger());
        FileExtractor fileExtractor = FileExtractor.builder()
                .baselDir("234")
                .fileName("123")
                .logger(logger)
                .build();

        TreeMap<Integer, Ref> testMap = new TreeMap<>();
        testMap.put(12121, new SymbolicRef("12121", null));

        Method method = FileExtractor.class.getDeclaredMethod("lastAndPreviousRelease", TreeMap.class);
        method.setAccessible(true);
        BranchInfo testResult = (BranchInfo) method.invoke(fileExtractor, testMap);

        assertEquals("12121", testResult.lastRelease());
        assertEquals(null, testResult.beforeLast());

    }

    @Test
    public void lastAndBeforeForEmpty() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Log logger = new DefaultLog(new ConsoleLogger());
        FileExtractor fileExtractor = FileExtractor.builder()
                .baselDir("234")
                .fileName("123")
                .logger(logger)
                .build();

        TreeMap<Integer, Ref> testMap = new TreeMap<>();

        Method method = FileExtractor.class.getDeclaredMethod("lastAndPreviousRelease", TreeMap.class);
        method.setAccessible(true);
        BranchInfo testResult = (BranchInfo) method.invoke(fileExtractor, testMap);

        assertEquals(null, testResult.lastRelease());
        assertEquals(null, testResult.beforeLast());

    }
}