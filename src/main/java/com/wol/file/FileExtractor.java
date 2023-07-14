package com.wol.file;


import com.wol.file.dto.BranchInfo;
import com.wol.file.dto.GitInfo;
import com.wol.utils.FileUtils;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.TreeWalk;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;
import com.wol.utils.GitUtils;


public class FileExtractor {


    public static final String GIT_PREFIX = "git__";
    public static final String DOT_GIT = ".git";
    private final String file_name;
    private final String base_dir;
    private final File tech_dir;
    public final String releaseSuffix;
    private final Pattern pattern;
    private Log logger;
    private Git git;




    private FileExtractor(
            String fileName, String baseDirName, String techDirName,
            String releaseSuffix, String releaseRegex, Log logger
    ) {
        this.base_dir = baseDirName;
        this.logger = logger;
        this.releaseSuffix = releaseSuffix;
        this.file_name = fileName;

        try {
            this.pattern = Pattern.compile(releaseRegex);
            this.tech_dir = Paths.get(base_dir, techDirName).toFile();
            FileUtils.deleteFolder(tech_dir);
            Files.createDirectories(tech_dir.toPath());
            git = new Git(new FileRepositoryBuilder()
                    .setGitDir(Paths.get(base_dir, DOT_GIT).toFile())
                    .build());

        } catch (Exception ex) {
            logger.error(ex.getMessage());
            throw new IllegalStateException();
        }

    }

    public List<Path> getMetadataFilesFromJar(File target, MavenProject project) {
        logger.info("Extracting files from JAR");
        List<Path> list = new ArrayList();
        try {
            if (target == null || project == null) return list;

            String absolutePath = target.getAbsolutePath();
            String artifact_name = project.getArtifact().getFile().getName();
            if (absolutePath == null || artifact_name == null) return list;

            Path artifact_path = Paths.get(absolutePath, artifact_name);
            if (artifact_path == null) return null;

            try (JarFile jar = new JarFile(artifact_path.toFile());){
                Enumeration enumEntries = jar.entries();
                while (enumEntries.hasMoreElements()) {
                    JarEntry file = (JarEntry) enumEntries.nextElement();
                    if (file.getName().contains(file_name)) {
                        String new_name = Paths.get(file.getName()).toFile().getName();
                        Path new_path = Paths.get(tech_dir.getAbsolutePath(), new_name);

                        try (InputStream is = jar.getInputStream(file);
                             FileOutputStream fos = new FileOutputStream(new_path.toFile());) {
                            while (is.available() > 0) fos.write(is.read());
                        }
                        list.add(new_path);
                    }
                }
            }

        } catch (Exception e) {
            logger.error("error in File extractor");
            logger.error(e.getMessage());
        }
        finally {
            return list;
        }
    }


    public GitInfo getMetadataFilesFromGit() {
        logger.info("Extracting files from Git");
        List<Path> list = new ArrayList();
        BranchInfo branchInfo = new BranchInfo(null, null, null);
        RevWalk rw = null;
        try {
            TreeMap<Integer, Ref> branches = GitUtils.expandBranchNamesAndReorderBranches(getBranches(git));
            TreeMap<Integer, Ref> orderedBranches = GitUtils.dropBranchesLaterThenCurrentBranches(branches, currentBranchNumber(git));        

            branchInfo = GitUtils.currentAndPreviousReleaseInfo(orderedBranches);
            Ref release_before = branchInfo.beforeRef();
            if (release_before == null) return new GitInfo(branchInfo, list);;

            rw = new RevWalk(git.getRepository());
            TreeWalk tw = new TreeWalk(git.getRepository());
            String lastCommitOfPreviousRelease = release_before.getName();
            RevCommit commitToCheck = rw.parseCommit(git.getRepository().resolve(lastCommitOfPreviousRelease));

            tw.addTree(commitToCheck.getTree());
            tw.setRecursive(true);
            extractGitFiles(list, tw);
        } catch (Exception e) {
            logger.error("error in File extractor");
            logger.error(e.getMessage());
        }
        finally {
            if (rw != null) rw.dispose();
            return new GitInfo(branchInfo, list);
        }
    }

    private void extractGitFiles(List<Path> list, TreeWalk tw) throws IOException {
        while (tw.next()) {
            if (tw.getPathString().contains(file_name) && !tw.isSubtree()) {
                ObjectLoader loader = git.getRepository().open(tw.getObjectId(0));
                String newName = GIT_PREFIX + tw.getNameString();
                Path new_path = Paths.get(tech_dir.getAbsolutePath(), newName);
                try (FileOutputStream fos = new FileOutputStream(new_path.toFile())) {
                    loader.copyTo(fos);
                    list.add(new_path);
                }
            }
        }
    }

    private TreeMap<Integer, Ref> getBranches(Git git) throws GitAPIException {
        TreeMap<Integer, Ref> branchNames = new TreeMap<>();
        ListBranchCommand branches = git.branchList();
        Iterator<Ref> iterator = branches.call().iterator();
        for (Iterator<Ref> it = iterator; it.hasNext(); ) {
            Ref ref = it.next();
            String name = GitUtils.refNameToBranchName(ref.getName());
            int releaseNumber = GitUtils.releaseNameToNumber(pattern, name, releaseSuffix);
            //only regex filtered branches
            if (releaseNumber > 0) branchNames.put(releaseNumber, ref);
        }
        return branchNames;
    }


    private Integer currentBranchNumber(Git git) throws IOException {
        Repository repository = git.getRepository();
        String branch = repository.getBranch();
        int releaseNumber = GitUtils.releaseNameToNumber(pattern, branch, releaseSuffix);
        return releaseNumber;
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

        public FEBuilder releaseSuffix(String r) {
            this.relS = r;
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

        private static String relS;
        private static String relR;
        private String baseD;
        private String techD;
        private Log log;
        private String fileN;

        static final String RELEASE = "release-";
        static final String RELEASE_REGEX = "release-(\\d+\\.*)*";
        static final String TECH_DIR_NAME = "properties_history";

        public FileExtractor build(){
            if (log == null)   throw new IllegalStateException();
            if (baseD == null) throw new IllegalStateException();
            if (log == null)   throw new IllegalStateException();
            if (techD == null)  techD = TECH_DIR_NAME;
            if (relR  == null)  relR = RELEASE_REGEX;
            if (relS  == null)  relS = RELEASE;

            FileExtractor fileExtractor = new FileExtractor(fileN, baseD, techD, relS, relR, log);
            return fileExtractor;
        }

    }
}
