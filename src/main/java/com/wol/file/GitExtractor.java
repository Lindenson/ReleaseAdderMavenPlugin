package com.wol.file;

import com.wol.file.dto.BranchInfo;
import com.wol.file.dto.GitInfo;
import com.wol.utils.FileUtils;
import com.wol.utils.GitUtils;
import org.apache.maven.plugin.logging.Log;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class GitExtractor implements MetadataExtractor{
    public static final String GIT_PREFIX = "git__";
    public static final String DOT_GIT = ".git";
    private final String file_name;
    private final String base_dir;
    private final File tech_dir;
    private final Pattern pattern;
    private Log logger;
    private Git git;
    private CompletableFuture<GitInfo> gitInfo;



    protected GitExtractor(
            String fileName, String baseDirName, String techDirName,
            String releaseRegex, Log logger
    ) {
        this.base_dir = baseDirName;
        this.logger = logger;
        this.file_name = fileName;

        try {
            this.pattern = Pattern.compile(releaseRegex);
            this.tech_dir = Paths.get(base_dir, techDirName).toFile();

            FileUtils.deleteFolder(tech_dir);
            Files.createDirectories(tech_dir.toPath());

            git = new Git(new FileRepositoryBuilder()
                    .setGitDir(Paths.get(base_dir, DOT_GIT).toFile())
                    .build());

            //start async job
            gitInfo = CompletableFuture.supplyAsync(this::getGitInfo);
        } catch (Exception ex) {
            logger.error(ex.getMessage());
            throw new IllegalStateException();
        }

    }

    @Override
    public GitInfo extractFiles() {
        return gitInfo.join();
    }

    private GitInfo getGitInfo() {
        logger.info("Extracting files from Git");
        List<Path> list = new ArrayList();
        BranchInfo branchInfo = new BranchInfo(null, null, null);
        RevWalk rw = null;
        try {
            TreeMap<Integer, Ref> branches = GitUtils.expandBranchNamesAndReorderBranches(getBranches(git), logger);
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
        branches.call().stream().forEach( ref -> {
                String name = GitUtils.refNameToBranchName(ref.getName());
                int releaseNumber = GitUtils.releaseNameToNumber(pattern, name, logger);
                //only regex filtered branches
                if (releaseNumber > 0) {
                    if (branchNames.get(releaseNumber)!=null) {
                        logger.error("release names collides! correct names or regex");
                    }
                    else branchNames.put(releaseNumber, ref);
                }
        });
        return branchNames;
    }


    private Integer currentBranchNumber(Git git) throws IOException {
        Repository repository = git.getRepository();
        String branch = repository.getBranch();
        int releaseNumber = GitUtils.releaseNameToNumber(pattern, branch, logger);
        return releaseNumber;
    }


}
