package com.wol.file;


import com.wol.file.dto.JarInfo;
import com.wol.utils.FileUtils;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JarExtractor implements MetadataExtractor{

    private File target;
    private MavenProject project;
    private final String fileName;
    private final String baseDir;
    private final File techDir;
    private Log logger;

    protected JarExtractor(
            String fileName,
            String baseDirName,
            String techDirName,
            File target,
            MavenProject project,
            Log logger
    ) {
        this.baseDir = baseDirName;
        this.logger = logger;
        this.fileName = fileName;
        this.project = project;
        this.target = target;

        try {
            this.techDir = Paths.get(baseDir, techDirName).toFile();
            FileUtils.deleteFolder(techDir);
            Files.createDirectories(techDir.toPath());
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            throw new IllegalStateException();
        }

    }

    @Override
    public JarInfo extractFiles() {
        logger.info("Extracting files from JAR");
        List<Path> list = new ArrayList<>();
        try {
            if (target == null || project == null) return new JarInfo(list);

            String absolutePath = target.getAbsolutePath();
            String artifactName = project.getArtifact().getFile().getName();
            if (absolutePath == null || artifactName == null) return new JarInfo(list);

            Path artifactPath = Paths.get(absolutePath, artifactName);
            if (artifactPath == null) return null;

            try (JarFile jar = new JarFile(artifactPath.toFile());){
                Enumeration<JarEntry> enumEntries = jar.entries();
                while (enumEntries.hasMoreElements()) {
                    JarEntry file = enumEntries.nextElement();
                    if (file.getName().contains(fileName)) {
                        String newName = Paths.get(file.getName()).toFile().getName();
                        Path newPath = Paths.get(techDir.getAbsolutePath(), newName);

                        try (InputStream is = jar.getInputStream(file);
                             FileOutputStream fos = new FileOutputStream(newPath.toFile());) {
                            while (is.available() > 0) fos.write(is.read());
                        }
                        list.add(newPath);
                    }
                }
            }

        } catch (Exception e) {
            logger.error("error in File extractor");
            logger.error(e.getMessage());
        }
        return new JarInfo(list);
    }
}
